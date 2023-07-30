package com.example.test.ui.my_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.api.ReservationApi
import com.example.test.api.RetrofitInstance
import com.example.test.model.reservation.MyReservationsResponse
import com.example.test.model.reservation.RContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MyReservationsViewModel : ViewModel() {
    private val api = RetrofitInstance.retrofit.create(ReservationApi::class.java)

    val reservations = MutableStateFlow<List<RContent>>(listOf())

    private var page = 0;
    var isLast = false
        private set
    val isLoading = MutableStateFlow<Boolean>(false)

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        page = 0
        isLast = false
        loadMore()
    }

    suspend fun loadMore() {
        if (isLoading.value) return
        if (isLast) return

        isLoading.value = true

        val response = suspendCoroutine<MyReservationsResponse> {
            api.getMyReservations(20, page)
                .enqueue(object : Callback<MyReservationsResponse> {
                    override fun onResponse(
                        call: Call<MyReservationsResponse>,
                        response: Response<MyReservationsResponse>
                    ) {
                        it.resumeWith(Result.success(response.body()!!))
                    }

                    override fun onFailure(call: Call<MyReservationsResponse>, t: Throwable) {
                        t.printStackTrace()

                        it.resumeWith(Result.success(MyReservationsResponse().apply {
                            last = true
                        }))
                    }
                })
        }

        isLoading.value = false

        val result = if (page == 0) {
            response.content
        } else {
            reservations.value + response.content
        }

        page += 1
        isLast = response.last
        reservations.emit(result)
    }

    suspend fun cancelReservation(reservation: RContent): String? {
        val result = suspendCoroutine<String?> {
            api.cancelReservation(reservation.centerId!!, reservation.reservationId!!)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        it.resume(response.body())
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        it.resumeWithException(t)
                    }
                })
        }

        val reservations =
            this.reservations.value.filter { it.reservationId != reservation.reservationId }
        this.reservations.emit(ArrayList(reservations))

        return result
    }
}