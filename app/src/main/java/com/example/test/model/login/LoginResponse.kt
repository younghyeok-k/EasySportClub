package com.example.example

import com.google.gson.annotations.SerializedName


data class LoginResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("msg") var msg: String? = null,
    @SerializedName("data") var data: Userinfo? = Userinfo()

)