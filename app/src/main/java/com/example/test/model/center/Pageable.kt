package com.example.example

import com.google.gson.annotations.SerializedName


data class Pageable(

    @SerializedName("sort") var sort: Sort? = Sort(),
    @SerializedName("offset") var offset: Int? = null,
    @SerializedName("pageSize") var pageSize: Int? = null,
    @SerializedName("pageNumber") var pageNumber: Int? = null,
    @SerializedName("paged") var paged: Boolean? = null,
    @SerializedName("unpaged") var unpaged: Boolean? = null

)