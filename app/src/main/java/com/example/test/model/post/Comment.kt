package com.example.test.model.post

import com.example.test.model.User

data class Comment(
    val id: Int = -1,
    val content: String? = null,
    val createdAt: String? = null,
    val user: User = User(),
)