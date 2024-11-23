package com.clerami.universe.data.remote.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class User(val email: String, val password: String)
data class GuestResponse(val success: Boolean, val message: String)

interface ApiService {
    @POST("/signup")
    fun signUp(@Body user: User): Call<GuestResponse>

    @POST("/guest")
    fun guestLogin(): Call<GuestResponse>

    @POST("/login")
    fun login(@Body user: User): Call<GuestResponse>
}
