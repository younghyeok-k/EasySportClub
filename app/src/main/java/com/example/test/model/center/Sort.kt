package com.example.example

import com.google.gson.annotations.SerializedName


data class Sort(

    @SerializedName("empty") var empty: Boolean? = null,
    @SerializedName("unsorted") var unsorted: Boolean? = null,
    @SerializedName("sorted") var sorted: Boolean? = null

)