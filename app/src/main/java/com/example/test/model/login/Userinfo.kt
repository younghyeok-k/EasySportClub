package com.example.example

import com.google.gson.annotations.SerializedName


data class Userinfo(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("username") var username: String? = null,
    @SerializedName("password") var password: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("role") var role: String? = null,
    @SerializedName("updateAt") var updateAt: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("provider") var provider: String? = null,
    @SerializedName("providerId") var providerId: String? = null,
    @SerializedName("nickname") var nickname: String? = null

)