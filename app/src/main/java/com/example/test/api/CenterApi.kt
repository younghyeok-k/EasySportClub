package com.example.test.api


import com.example.example.CenterResponse
import retrofit2.Call
import retrofit2.http.GET

interface CenterApi {
    @GET("center/all")
    fun getall(): Call<CenterResponse>
}