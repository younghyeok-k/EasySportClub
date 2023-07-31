package com.example.test.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.example.Content
import com.example.test.api.ReservationApi
import com.example.test.api.RetrofitInstance
import com.example.test.model.reservation.CenterReservationStatusResponse
import com.example.test.model.reservation.RequestReservation
import com.example.test.model.reservation.ResponseReservation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.suspendCoroutine

class ReservationViewModel : ViewModel() {
    private val apiService = RetrofitInstance.retrofit.create(ReservationApi::class.java)
    val _reservationStatus = MutableSharedFlow<CenterReservationStatusResponse>()
    val reservationStatus: MutableSharedFlow<CenterReservationStatusResponse> get() = _reservationStatus
    fun fetch(content: Content,Rdate:String) {
        viewModelScope.launch {
//            while (true) {
            try {
                val status = getReservationStatus(content,Rdate)
                if (status != null) {
                    _reservationStatus.emit(status)
                }
            } catch (_: Exception) {
            }

            delay(1000)
//            }
        }
    }

    private suspend fun getReservationStatus(content: Content,Rdate:String): CenterReservationStatusResponse? {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return suspendCoroutine {
            apiService.getCenterReservation(
                content.centerId!!.toString(),
                Rdate
            )
                .enqueue(object : Callback<CenterReservationStatusResponse> {
                    override fun onResponse(
                        call: Call<CenterReservationStatusResponse>,
                        response: Response<CenterReservationStatusResponse>
                    ) {
                        it.resumeWith(Result.success(response.body()))
                    }

                    override fun onFailure(
                        call: Call<CenterReservationStatusResponse>,
                        t: Throwable
                    ) {
                        it.resumeWith(Result.failure(t))
                    }
                })
        }
    }

    suspend fun reservation(
        content: Content,
        date: String,
        times: List<String>,
        headcount:String
    ): ResponseReservation? {
        val request = RequestReservation().apply {
            reservingDate = date
            reservingTimes = times
            headCount = headcount
        }

        val result = suspendCoroutine {
            apiService.reserve(
                content.centerId!!.toString(),
                request
            )
                .enqueue(object : Callback<ResponseReservation> {
                    override fun onResponse(
                        call: Call<ResponseReservation>,
                        response: Response<ResponseReservation>
                    ) {
                        it.resumeWith(Result.success(response.body()))
                    }

                    override fun onFailure(
                        call: Call<ResponseReservation>,
                        t: Throwable
                    ) {
                        it.resumeWith(Result.failure(t))
                    }
                })
        }

//        try {
//            val status = getReservationStatus(content)
//            if (status != null) {
//                reservationStatus.emit(status)
//            }
//        } catch (_: Exception) {
//        }

        return result
    }
}