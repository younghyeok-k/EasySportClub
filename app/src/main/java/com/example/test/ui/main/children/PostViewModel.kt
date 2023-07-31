package com.example.test.ui.main.children

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.api.PostApi
import com.example.test.api.RetrofitInstance
import com.example.test.model.post.Post
import com.example.test.model.post.PostsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.suspendCoroutine

class PostViewModel : ViewModel() {
    private val apiService = RetrofitInstance.retrofit.create(PostApi::class.java)

    private val query = MutableStateFlow("")
    val posts = MutableStateFlow<List<Post>>(listOf())


    private var page = 0;
    var isLast = false
        private set
    val isLoading = MutableStateFlow<Boolean>(false)

    init {
        viewModelScope.launch {
            query.debounce(100).collectLatest {
                page = 0
                isLast = false
                loadMore()
            }
        }
    }

    fun setQuery(query: String) {
        this.query.value = query
    }

    suspend fun refresh() {
        page = 0
        isLast = false
        loadMore()
    }

    suspend fun loadMore() {
        if (isLoading.value) return
        if (isLast) return

        isLoading.value = true

        val response = suspendCoroutine<PostsResponse> {
            apiService.searchPosts("title", query.value, 20, page)
                .enqueue(object : Callback<PostsResponse> {
                    override fun onResponse(
                        call: Call<PostsResponse>,
                        response: Response<PostsResponse>
                    ) {
//                        if (response.isSuccessful && response.body() != null) {
//                            // Process the response
//                            it.resumeWith(Result.success(response.body()!!))
//                        } else {
//                            // Handle unsuccessful response
//                        }

                        it.resumeWith(Result.success(response.body()!!))

                    }

                    override fun onFailure(call: Call<PostsResponse>, t: Throwable) {
                        t.printStackTrace()

                        it.resumeWith(Result.success(PostsResponse().apply {
                            last = true
                        }))
                    }
                })
        }

        isLoading.value = false

        val result = if (page == 0) {
            response.content
        } else {
            posts.value + response.content
        }

        page += 1
        isLast = response.last
        posts.emit(result)
    }
}