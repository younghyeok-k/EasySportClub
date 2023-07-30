package com.example.test.model.post

import com.example.example.Pageable
import com.example.example.Sort

data class PostsResponse(
    val content: List<Post> = listOf(),
    val pageable: Pageable = Pageable(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    var last: Boolean = false,
    val number: Int = 0,
    val sort: Sort = Sort(),
    val size: Int = 0,
    val numberOfElements: Int = 0,
    val first: Boolean = false,
    val empty: Boolean = false,
)