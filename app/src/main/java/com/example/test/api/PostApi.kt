package com.example.test.api


import com.example.test.model.post.Comment
import com.example.test.model.post.Post
import com.example.test.model.post.PostsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {
    @GET("post/readAll")
    fun getPosts(
        @Query("size") size: Int,
        @Query("page") page: Int
    ): Call<PostsResponse>

    @GET("post/read/{postId}")
    fun getPost(
        @Path("postId") postId: Int
    ): Call<Post>

    @GET("post/search")
    fun searchPosts(
        @Query("searchType") searchType: String,
        @Query("keyword") query: String,
        @Query("size") size: Int,
        @Query("page") page: Int
    ): Call<PostsResponse>

    @Headers("Content-Type: application/json")
    @POST("post/create")
    fun createPost(@Body message: String): Call<String>

    @Headers("Content-Type: application/json")
    @PATCH("post/update/{postId}")
    fun updatePost(
        @Path("postId") postId: Int,
        @Body message: String
    ): Call<String>

    @DELETE("post/delete/{postId}")
    fun removePost(
        @Path("postId") postId: Int,
    ): Call<String>

    @GET("comment/readAll/{postId}")
    fun getComments(
        @Path("postId") postId: Int,
    ): Call<List<Comment>>

    @Headers("Content-Type: application/json")
    @POST("comment/create/{postId}")
    fun createComment(
        @Path("postId") postId: Int,
        @Body message: String
    ): Call<String>

    @Headers("Content-Type: application/json")
    @PATCH("comment/update/{commentId}")
    fun updateComment(
        @Path("commentId") commentId: Int,
        @Body message: String
    ): Call<String>

    @DELETE("comment/delete/{commentId}")
    fun removeComment(
        @Path("commentId") commentId: Int,
    ): Call<String>
}