package com.example.test.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.api.PostApi
import com.example.test.api.RetrofitInstance
import com.example.test.model.post.Comment
import com.example.test.model.post.Post
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PostViewerViewModel : ViewModel() {
    private val apiService = RetrofitInstance.retrofit.create(PostApi::class.java)

    var _post: Post? = null

    val post = MutableSharedFlow<Post?>()
    val comments = MutableSharedFlow<List<Comment>>()

    fun fetch(post: Post) {
        if (_post != null) return
        _post = post

        viewModelScope.launch {
            this@PostViewerViewModel.post.emit(post)

            while (true) {
                try {
                    val post = getPost(_post!!.id)
                    this@PostViewerViewModel.post.emit(post)
                } catch (_: Exception) {
                }

                delay(5000)
            }
        }

        viewModelScope.launch {
            while (true) {
                val comments = getComments(post.id)
                if (comments != null) {
                    this@PostViewerViewModel.comments.emit(comments)
                }

                delay(5000)
            }
        }
    }

    suspend fun refresh() {
        if (_post == null) return

        viewModelScope.launch {
            listOf(
                async {
                    try {
                        val post = getPost(_post!!.id)
                        this@PostViewerViewModel.post.emit(post)
                    } catch (_: Exception) {
                    }
                },
                async {
                    val comments = getComments(_post!!.id)
                    if (comments != null) {
                        this@PostViewerViewModel.comments.emit(comments)
                    }
                }
            ).awaitAll()
        }
    }

    suspend fun deletePost(): String? {
        if (_post == null) return null

        val result = suspendCoroutine<String?> {
            apiService.removePost(_post!!.id).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    it.resumeWith(Result.success(response.body()))
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    it.resumeWith(Result.failure(t))
                }
            })
        }

        // refresh()
        post.emit(null)
        return result
    }

    suspend fun createComment(content: String): String? {
        if (_post == null) return null

        val json = JSONObject().apply {
            put("content", content)
        }.toString()

        val result = suspendCoroutine<String?> {
            apiService.createComment(_post!!.id, json).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    it.resumeWith(Result.success(response.body()))
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    it.resumeWith(Result.failure(t))
                }
            })
        }

        refresh()
        return result
    }

    suspend fun updateComment(commentId: Int, content: String): String? {
        if (_post == null) return null

        val json = JSONObject().apply {
            put("content", content)
        }.toString()

        val result = suspendCoroutine<String?> {
            apiService.updateComment(commentId, json).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    it.resumeWith(Result.success(response.body()))
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    it.resumeWith(Result.failure(t))
                }
            })
        }

        refresh()
        return result
    }

    suspend fun deleteComment(commentId: Int): String? {
        if (_post == null) return null

        val result = suspendCoroutine<String?> {
            apiService.removeComment(commentId).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    it.resumeWith(Result.success(response.body()))
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    it.resumeWith(Result.failure(t))
                }
            })
        }

        refresh()
        return result
    }

    private suspend fun getPost(postId: Int) = suspendCoroutine<Post?> {
        apiService.getPost(postId).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                it.resumeWith(Result.success(response.body()))
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                t.printStackTrace()
                it.resumeWithException(t)
            }
        })
    }

    private suspend fun getComments(postId: Int) = suspendCoroutine<List<Comment>?> {
        apiService.getComments(postId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                it.resumeWith(Result.success(response.body()))
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                it.resumeWith(Result.success(null))
            }
        })
    }
}