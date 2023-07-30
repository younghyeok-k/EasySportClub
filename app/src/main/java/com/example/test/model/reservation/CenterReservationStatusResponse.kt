package com.example.test.model.reservation

import com.google.gson.annotations.SerializedName

data class CenterReservationStatusResponse(
    @SerializedName("date") var date: String? = null,
    @SerializedName("openTime") var openTime: String? = null,
    @SerializedName("closeTime") var closeTime: String? = null,
    @SerializedName("reservedTimes") var reservedTimes: ArrayList<String> = arrayListOf()
)