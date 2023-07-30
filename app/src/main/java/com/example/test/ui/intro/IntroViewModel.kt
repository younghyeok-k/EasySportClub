package com.example.test.ui.intro

import androidx.lifecycle.ViewModel
import com.example.example.LoginResponse
import com.example.test.api.AuthApi
import com.example.test.api.RetrofitInstance
import com.example.test.application.SharedManager
import com.example.test.model.User
import com.example.test.model.joinPost
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class IntroViewModel : ViewModel() {
    private val api = RetrofitInstance.getInstance().create(AuthApi::class.java)

    suspend fun login(name: String, password: String) {
        suspendCoroutine<Unit> {
            val json = JSONObject().apply {
                put("username", name)
                put("password", password)
            }.toString()

            api.getLogin(json).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    it.resume(Unit)
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    it.resume(Unit)
                }
            })
        }

        if (SharedManager.getBearerToken().isBlank()) {
            throw Exception("Failed to sign in")
        }

        val user = suspendCoroutine<User?> {
            api.getMyUser().enqueue(object : Callback<User> {
                override fun onResponse(
                    call: Call<User>,
                    response: Response<User>
                ) {
                    it.resume(response.body())
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    it.resumeWithException(t)
                }
            })
        } ?: throw Exception("Failed to sign in")

        SharedManager.saveCurrentUser(user)
    }

    suspend fun join(name: String, password: String) = suspendCoroutine {
        val json = JSONObject().apply {
            put("username", name)
            put("password", password)
        }.toString()

        api.getJoin(json).enqueue(object : Callback<joinPost> {
            override fun onResponse(
                call: Call<joinPost>,
                response: Response<joinPost>
            ) {
                it.resume(response.body())
            }

            override fun onFailure(call: Call<joinPost>, t: Throwable) {
                it.resumeWithException(t)
            }
        })
    }
}