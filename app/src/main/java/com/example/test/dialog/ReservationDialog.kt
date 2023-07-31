package com.example.test.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.example.Content
import com.example.test.R
import com.example.test.databinding.DialogReservationBinding
import com.example.test.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ReservationDialog : DialogFragment() {
    companion object {
        private const val ARG_CONTENT = "ARG_CONTENT"
        const val TAG = "ReservationDialog"

        fun show(fragmentManager: FragmentManager, content: Content) {
            ReservationDialog().apply {
                arguments = bundleOf(ARG_CONTENT to content)
            }.show(fragmentManager, TAG)
        }
    }

    private var content: Content? = null

    private var count = 1
    private var _binding: DialogReservationBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ReservationViewModel>()
    private val selectedTimes = MutableLiveData<HashSet<String>>()
    //private val Rdate:Date()
    private var Rdate:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        content = arguments?.getParcelable(ARG_CONTENT)

        if (savedInstanceState != null) {
            content = savedInstanceState.getParcelable(ARG_CONTENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ARG_CONTENT, content)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReservationBinding.inflate(inflater, container, false)
        binding.root[0].updateLayoutParams<FrameLayout.LayoutParams> {
            width =
                (inflater.context.resources.displayMetrics.widthPixels - inflater.context.resources.displayMetrics.density * 32).toInt()
        }

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        Rdate=dateFormat.format(Date())
        viewModel.fetch(content!!,Rdate)

        with(binding) {
            headerCloseButton.setOnClickListener { dismiss() }
            nameTextView.text = "시설명 : ${content?.name}"
            addressTextView.text = "주소 : ${content?.address}"
            operatingTimeTextView.text = "이용가능시간 : ${content?.openTime} ~ ${content?.closeTime}"
            priceTextView.text = String.format(Locale.US, "가격 : %,d", content?.price ?: 0)
            val TextDateFormat = SimpleDateFormat("MM월 dd일(EEEE)", Locale.KOREAN)

            datee.text= TextDateFormat.format(Date())
            headCount.text=count.toString()
            incrementButton.setOnClickListener {
                if (count < 100) {
                    count++
                    headCount.text = count.toString()
                }
            }
            decrementButton.setOnClickListener {
                if (count > 1) {
                    count--
                    headCount.text = count.toString()
                }
            }

            val openTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, content!!.openTime!!.split(":")[0].toInt())
                set(Calendar.MINUTE, content!!.openTime!!.split(":")[1].toInt())
                set(Calendar.MILLISECOND, 0)
            }

            val closeTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, content!!.closeTime!!.split(":")[0].toInt())
                set(Calendar.MINUTE, content!!.closeTime!!.split(":")[1].toInt())
                set(Calendar.MILLISECOND, 0)
            }

            val timeFormat = SimpleDateFormat("H:mm", Locale.US)

            while (openTime.before(closeTime)) {
                val button = layoutInflater.inflate(
                    R.layout.widget_reservation_button,
                    binding.flowLayout,
                    false
                )
                    .apply {
                        tag = Calendar.getInstance().apply {
                            timeInMillis = openTime.timeInMillis
                        }

                        (this as MaterialButton).text = timeFormat.format(openTime.time)
                        isEnabled = false
                    }

                button.setOnClickListener {
                    val calendar = it.tag as Calendar
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

                    it.isSelected = !it.isSelected

                    val times = HashSet(selectedTimes.value ?: hashSetOf())
                    if (it.isSelected) {
                        times.add(timeFormat.format(calendar.time))
                    } else {
                        times.remove(timeFormat.format(calendar.time))
                    }

                    selectedTimes.value = times
                }

                binding.flowLayout.addView(button)

                openTime.add(Calendar.MINUTE, 30)

                datePickerBtn.setOnClickListener {
                    val today = MaterialDatePicker.todayInUtcMilliseconds()
                    val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Date Picker")
                        .setSelection(today)
                        .build() // 오늘 날짜 설정
//버튼 예약 만드렁야함
                    // 다이얼로그를 표시하는 Activity 또는 FragmentActivity를 가져옴
                    val activity = context as MainActivity

                    activity?.supportFragmentManager?.let { fragmentManager ->
                        materialDatePicker.show(fragmentManager, "DATE_PICKER")

                        materialDatePicker.addOnPositiveButtonClickListener { selection ->
                            val simpleDateFormat = SimpleDateFormat("MM월 dd일(EEEE)", Locale.KOREAN)
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val date = Date()
                            date.time = selection

                            val dateString = simpleDateFormat.format(date)
                            Rdate= dateFormat.format(date)
                            datee.text = dateString
                            viewModel.fetch(content!!,Rdate)


                        }
                    }


                }
            }
            reserveButton.setOnClickListener {
                reservation()
            }


        }

        lifecycleScope.launch {
            viewModel.reservationStatus.collectLatest { response ->
                binding.flowLayout.children.forEach {
                    val calendar = it.tag as Calendar

                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    it.isEnabled = response.reservedTimes.none {
                        it.split(":").let {
                            it[0].toInt() == hour && it[1].toInt() == minute
                        }
                    }

                    if (!it.isEnabled) {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

                        val times = HashSet(selectedTimes.value ?: hashSetOf())
                        times.remove(timeFormat.format(calendar.time))

                        selectedTimes.value = times
                    }
                }
            }
        }

        selectedTimes.observe(viewLifecycleOwner) {
            binding.reserveButton.isEnabled = it.isNotEmpty()
        }
    }

    private fun reservation() {


        AlertDialog.Builder(binding.root.context)
            .setIcon(R.drawable.baseline_event_available_24)
            .setTitle("Reservation")
            .setMessage("선택하신 시간에 예약 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response =
                            viewModel.reservation(content!!, Rdate, selectedTimes.value!!.toList(),count.toString())
                        Log.d("Log date",Rdate)
                        Toast.makeText(binding.root.context, "예약되었습니다.", Toast.LENGTH_SHORT).show()

                        dismiss()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            binding.root.context,
                            "오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}