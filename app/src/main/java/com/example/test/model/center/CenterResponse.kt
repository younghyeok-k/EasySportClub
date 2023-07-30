package com.example.example

import com.google.gson.annotations.SerializedName


data class CenterResponse(

    @SerializedName("content") var content: ArrayList<Content> = arrayListOf(),
    @SerializedName("pageable") var pageable: Pageable? = Pageable(),
    @SerializedName("last") var last: Boolean? = null,
    @SerializedName("totalElements") var totalElements: Int? = null,
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("size") var size: Int? = null,
    @SerializedName("number") var number: Int? = null,
    @SerializedName("sort") var sort: Sort? = Sort(),
    @SerializedName("first") var first: Boolean? = null,
    @SerializedName("numberOfElements") var numberOfElements: Int? = null,
    @SerializedName("empty") var empty: Boolean? = null

)