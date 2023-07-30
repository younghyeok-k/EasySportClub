package com.example.test.model.reservation

import com.google.gson.annotations.SerializedName


data class ResponseReservation(

    @SerializedName("reservationId") var reservationId: Int? = null,
    @SerializedName("centerId") var centerId: Int? = null,
    @SerializedName("centerName") var centerName: String? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("openTime") var openTime: String? = null,
    @SerializedName("CloseTime") var CloseTime: String? = null,
    @SerializedName("pricePerHalfHout") var pricePerHalfHout: Int? = null,
    @SerializedName("reservedTimes") var reservedTimes: ArrayList<String> = arrayListOf()
)