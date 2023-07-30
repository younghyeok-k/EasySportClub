package com.example.test.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.example.Content
import com.example.test.R
import com.example.test.api.ReservationApi
import com.example.test.api.RetrofitInstance
import com.example.test.model.reservation.RequestReservation
import com.example.test.model.reservation.ResponseReservation
import com.example.test.ui.main.MainActivity
import com.google.android.material.datepicker.MaterialDatePicker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CustomDialog(private val context: Context) {


    fun showDialog(houseModel: Content) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.booking)

        val address = dialog.findViewById<TextView>(R.id.address)
        val name = dialog.findViewById<TextView>(R.id.name)
        val price = dialog.findViewById<TextView>(R.id.price)
        val img = dialog.findViewById<ImageView>(R.id.img1)
        val datee = dialog.findViewById<TextView>(R.id.date)
//        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val back = dialog.findViewById<ImageView>(R.id.backi)
        val reservationbtn = dialog.findViewById<Button>(R.id.reservationbtn)
        // 오늘 날짜
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val datepicker = dialog.findViewById<Button>(R.id.date_picker_btn)
        datepicker.setOnClickListener {
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
                    val date = Date()
                    date.time = selection

                    val dateString = simpleDateFormat.format(date)

                    datee.text = dateString


                }
                back.setOnClickListener {
                    dialog.dismiss()
                }
            }

            back.setOnClickListener {
                dialog.dismiss()
            }
            reservationbtn.setOnClickListener {
                dialog.dismiss()
                val reservation = RequestReservation()
                reservation.reservingDate = "2022-05-06"
                reservation.reservingTimes = mutableListOf<String>().apply {
                    add("09:00")
                    add("10:00")
                }
                reservation.headCount = "4"
                getReservationAPI("6", reservation)
            }
        }


        address.text = houseModel.address
        name.text = houseModel.name
        price.text = houseModel.price.toString()

        Glide
            .with(img.context)
            .load(houseModel.imgUrl)
            .into(img)


        dialog.show()
    }

    private fun getReservationAPI(centerid: String, user: RequestReservation) {

        RetrofitInstance.retrofit.create(ReservationApi::class.java)
            .reserve(centerid, user)
            .enqueue(object : Callback<ResponseReservation> {
                override fun onResponse(
                    call: Call<ResponseReservation>,
                    response: Response<ResponseReservation>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("getReservationAPI", "success");
                        response.body()?.let { dto ->

                            Log.d("getReservationAPIcenterName", dto.centerName.toString())

                        }

                    }
                }

                override fun onFailure(call: Call<ResponseReservation>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
    }
}