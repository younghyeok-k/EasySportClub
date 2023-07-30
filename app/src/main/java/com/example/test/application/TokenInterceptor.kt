package com.example.test.application

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    private lateinit var sharedManager: SharedManager
    override fun intercept(chain: Interceptor.Chain): Response {
        sharedManager = SharedManager.getInstance()
        val originalRequest = chain.request()

        // 응답 받기 전에 헤더 값을 확인
        val response = chain.proceed(originalRequest)
        val bearerToken = response.header("Authorization")

        // 토큰 값을 추출하여 저장
        if (!bearerToken.isNullOrEmpty() && bearerToken.startsWith("Bearer ")) {
            val token = bearerToken.substringAfter("Bearer ").trim()
            saveBearerToken(token)
            Log.d("bear", token)
        }

        return response
    }

    private fun saveBearerToken(token: String) {
        // 토큰 값을 저장하는 로직을 구현
        sharedManager.saveBearerToken(token)
    }
}
