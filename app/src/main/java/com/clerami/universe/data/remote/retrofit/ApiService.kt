package com.clerami.universe.data.remote.retrofit

import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.CommentRequest
import com.clerami.universe.data.remote.response.CommentResponse
import com.clerami.universe.data.remote.response.CommentVoteRequest
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.CreateTopicResponse
import com.clerami.universe.data.remote.response.DeleteResponse
import com.clerami.universe.data.remote.response.GuestResponse
import com.clerami.universe.data.remote.response.LoginRequest
import com.clerami.universe.data.remote.response.LoginResponse
import com.clerami.universe.data.remote.response.RegisterRequest
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.data.remote.response.TopicsResponse
import com.clerami.universe.data.remote.response.UpdateResponse
import com.clerami.universe.data.remote.response.UpdateTopicRequest
import com.clerami.universe.data.remote.response.UpdateUser
import com.clerami.universe.data.remote.response.UpdateUserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header

import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @POST("api/signup")
    fun signUp(@Body user: RegisterRequest): Call<GuestResponse>


    @POST("api/guest")
    fun guestLogin(): Call<GuestResponse>

    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>


    @POST("api/topics")
    fun createTopic(
        @Header("Authorization") token: String,
        @Body request: CreateTopicRequest
    ):Call<CreateTopicResponse>

    @GET("api/topics/{topicId}/recommend")
    fun getRecommended(
        @Path("topicId") topicId: String
    ): Call<Topic>


    @GET("api/topics")
    fun getAllTopics(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Call<TopicsResponse>

    @GET("topics")
    fun getTopics(): Call<List<Topic>>

    @GET("api/topics/{topicId}")
    fun getTopicById(@Path("topicId") topicId: String): Call<Topic>

    @PUT("api/topics/{topicId}")
    fun updateTopic(
        @Header("Authorization") token: String,
        @Path("topicId") topicId: String,
        @Body request: UpdateTopicRequest
    ): Call<UpdateResponse>

    @PUT("api/users/{username}/edit")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Body request: UpdateUser
    ): Call<UpdateUserResponse>

    @DELETE("api/topics/{topicId}")
    fun deleteTopic(
        @Header("Authorization") token: String,
        @Path("topicId") topicId: String): Call<DeleteResponse>

    @GET("api/topics/{topicId}/comments")
    fun getComments(@Path("topicId") topicId: String): Call<List<Comment>>

    @POST("api/topics/{topicId}/comments")
    fun postComment(
        @Header("Authorization") token: String,
        @Path("topicId") topicId: String,
        @Body commentText: CommentRequest
    ): Call<CommentResponse>


    @POST("api/topics/{topicId}/comments/{commentId}/upvote")
    fun postUpvote(
        @Path("topicId") topicId: String,
        @Path("commentId") commentId: String,
        @Body vote: CommentVoteRequest
    ): Call<Void>
}