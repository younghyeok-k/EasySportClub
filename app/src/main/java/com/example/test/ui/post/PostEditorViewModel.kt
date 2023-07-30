package com.example.test.ui.post

import androidx.lifecycle.ViewModel
import com.example.test.api.PostApi
import com.example.test.api.RetrofitInstance
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.suspendCoroutine

class PostEditorViewModel : ViewModel() {
    private val apiService = RetrofitInstance.retrofit.create(PostApi::class.java)

    suspend fun create(title: String, content: String): String {
        val json = JSONObject().apply {
            put("title", title)
            put("content", content)
        }.toString()

        return suspendCoroutine<String> {
            apiService.createPost(json).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    it.resumeWith(Result.success(response.body() ?: "null"))
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    it.resumeWith(Result.failure(t))
                }
            })
        }
    }

    suspend fun update(postId: Int, title: String, content: String): String {
        val json = JSONObject().apply {
            put("title", title)
            put("content", content)
        }.toString()

        return suspendCoroutine<String> {
            apiService.updatePost(postId, json).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    it.resumeWith(Result.success(response.body() ?: "null"))
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    it.resumeWith(Result.failure(t))
                }
            })
        }
    }
}