package com.example.test.api

import com.example.test.model.reservation.CenterReservationStatusResponse
import com.example.test.model.reservation.MyReservationsResponse
import com.example.test.model.reservation.RequestReservation
import com.example.test.model.reservation.ResponseReservation
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReservationApi {
    @POST("centerReservation/{centerId}/reservation")
    fun reserve(
        @Path("centerId") centerId: String,
        @Body reservationInfo: RequestReservation
    ): Call<ResponseReservation>

    @GET("centerReservation/{centerId}/reservation")
    fun getCenterReservation(
        @Path("centerId") centerId: String,
        @Query("date") date: String
    ): Call<CenterReservationStatusResponse>

    @GET("centerReservation/reservations")
    fun getMyReservations(
        @Query("size") size: Int,
        @Query("page") page: Int,
    ): Call<MyReservationsResponse>

    @DELETE("centerReservation/{centerId}/reservation/{reservationId}")
    fun cancelReservation(
        @Path("centerId") centerId: Int,
        @Path("reservationId") reservationId: Int
    ): Call<String>
}
