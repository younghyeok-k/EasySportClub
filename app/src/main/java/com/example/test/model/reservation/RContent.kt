package com.example.test.model.reservation

import com.google.gson.annotations.SerializedName


data class RContent(
    @SerializedName("reservationId") var reservationId: Int? = null,
    @SerializedName("centerId") var centerId: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("imgUrl") var imgUrl: String? = null,
    @SerializedName("status") var price: String? = null
)