package com.clerami.universe.data.remote.retrofit

import com.clerami.universe.data.RegisterUser

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


data class GuestResponse(val success: Boolean, val message: String)
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val userId: String
)

interface ApiService {


    @POST("api/signup")
    fun signUp(@Body user: RegisterUser): Call<GuestResponse>


    @POST("api/guest")
    fun guestLogin(): Call<GuestResponse>


    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

}






