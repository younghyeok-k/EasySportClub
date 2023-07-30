package com.example.test.api


import com.example.example.LoginResponse
import com.example.test.model.User
import com.example.test.model.joinPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("login")
    fun getLogin(
        @Body user: String,
//   @Header("authorization") accessToken:String
    ): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("join")
    fun getJoin(
        @Body user: String
    ): Call<joinPost>

    @GET("user/success")
    fun getMyUser(): Call<User>
}
//data class LoginRequest(
//    var username : String,
//    var password : String
//)