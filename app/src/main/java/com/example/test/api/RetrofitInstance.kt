package com.example.test.api

import android.util.Log
import com.example.test.application.SharedManager
import com.example.test.application.TokenInterceptor

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private lateinit var sharedManager: SharedManager

    //192.168.45.212
    //192.168.12.249
    //172.21.7.211
//    private val BASE_URL = "http://13.125.250.57/" // gnu
    private val BASE_URL = "http://13.125.250.57" // gnu

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .addInterceptor(interceptor)
        .addInterceptor(TokenInterceptor()) // Bearer 토큰 추출 및 요청 헤더에 추가
        .addInterceptor(BearerTokenInterceptor())
        .build()

    val retrofit: Retrofit by lazy {
        sharedManager = SharedManager.getInstance() // SharedManager 초기화
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getInstance(): Retrofit {
        return retrofit
    }

    private class BearerTokenInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // Bearer 토큰 값 가져오기
            val bearerToken = sharedManager.getBearerToken()

            // Bearer 토큰이 존재하는 경우 요청 헤더에 추가
            if (!bearerToken.isNullOrEmpty()) {
                val modifiedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $bearerToken")
                    .build()
                return chain.proceed(modifiedRequest)
            }
            Log.d("BearerToken", bearerToken)
            return chain.proceed(originalRequest)
        }
    }
}
