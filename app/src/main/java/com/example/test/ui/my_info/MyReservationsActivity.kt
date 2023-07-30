package com.example.test.ui.my_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.databinding.ActivityMyReservationsBinding
import com.example.test.databinding.ItemMyReservationBinding
import com.example.test.model.reservation.RContent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyReservationsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMyReservationsBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MyReservationsViewModel>()

    private val adapter = ReservationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() = with(binding) {
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                    lifecycleScope.launch {
                        viewModel.loadMore()
                    }
                }
            }
        })
        recyclerView.adapter = adapter.apply {
            onCancelButtonClicked = {
                showCancelDialog(it)
            }
        }

        refreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.refresh()
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest {
                binding.refreshLayout.isRefreshing = it
            }
        }

        lifecycleScope.launch {
            viewModel.reservations.collectLatest {
                adapter.submitList(it)
            }
        }
    }

    private fun showCancelDialog(reservation: RContent) {
        AlertDialog.Builder(binding.root.context)
            .setIcon(R.drawable.baseline_cancel_24)
            .setMessage("해당 예약을 취소하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                lifecycleScope.launch {
                    try {
                        viewModel.cancelReservation(reservation)
                        Toast.makeText(this@MyReservationsActivity, "취소되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                    } catch (_: Exception) {
                        Toast.makeText(
                            this@MyReservationsActivity,
                            "오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private class ReservationAdapter :
        ListAdapter<RContent, ReservationAdapter.ReservationItemViewHolder>(object :
            DiffUtil.ItemCallback<RContent>() {
            override fun areItemsTheSame(oldItem: RContent, newItem: RContent): Boolean {
                return oldItem.reservationId == newItem.reservationId
            }

            override fun areContentsTheSame(oldItem: RContent, newItem: RContent): Boolean {
                return true
            }
        }) {
        var onCancelButtonClicked: ((RContent) -> Unit)? = null

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ReservationItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemMyReservationBinding.inflate(inflater, parent, false)
            return ReservationItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ReservationItemViewHolder, position: Int) {
            val reservation = currentList[position]

            with(holder.binding) {
                Glide.with(imageView)
                    .load(reservation.imgUrl)
                    .into(imageView)

                nameTextView.text = reservation.name
                addressTextView.text = reservation.address

                cancelButton.setOnClickListener {
                    onCancelButtonClicked?.invoke(reservation)
                }
            }
        }

        class ReservationItemViewHolder(val binding: ItemMyReservationBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}