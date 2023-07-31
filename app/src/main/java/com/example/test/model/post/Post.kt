package com.example.test.model.post

import android.os.Parcelable
import com.example.test.model.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: Int = -1,
    val title: String = "",
    val content: String = "",
    val createdAt: String? = null,
    val commentSize: Long = 0,
    val user: User = User(),
) : Parcelable
