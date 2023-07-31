package com.example.test.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int = -1,
    val username: String = "",
    val password: String? = null,
    val email: String? = null,
    val role: String = "",
    val updateAt: String? = null,
    val createdAt: String? = null,
    val provider: String? = null,
    val providerId: String? = null,
    val nickname: String? = null
) : Parcelable
