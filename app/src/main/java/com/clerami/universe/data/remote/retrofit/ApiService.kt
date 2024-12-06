package com.clerami.universe.data.remote.retrofit

import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.CommentVoteRequest
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.GuestResponse
import com.clerami.universe.data.remote.response.LoginRequest
import com.clerami.universe.data.remote.response.LoginResponse
import com.clerami.universe.data.remote.response.RegisterRequest
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.data.remote.response.UpdateTopicRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path




interface ApiService {

    @POST("api/signup")
    fun signUp(@Body user: RegisterRequest): Call<GuestResponse>


    @POST("api/guest")
    fun guestLogin(): Call<GuestResponse>

    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/topics")
    fun createTopic(@Body request: CreateTopicRequest): Call<Topic>

    @GET("api/topics")
    fun getAllTopics(): Call<List<Topic>>

    @GET("topics")
    fun getTopics(): Call<List<Topic>>

    @GET("api/topics/{topicId}")
    fun getTopicById(@Path("topicId") topicId: String): Call<Topic>

    @PUT("api/topics/{topicId}")
    fun updateTopic(
        @Path("topicId") topicId: String,
        @Body request: UpdateTopicRequest
    ): Call<Topic>

    @DELETE("api/topics/{topicId}")
    fun deleteTopic(@Path("topicId") topicId: String): Call<Void>

    @GET("api/topics/{topicId}/comments")
    fun getComments(@Path("topicId") topicId: String): Call<List<Comment>>

    @POST("topics/{topicId}/comments")
    fun postComment(
        @Path("topicId") topicId: String,
        @Body commentText: String
    ): Call<Comment>

    @POST("api/topics/{topicId}/comments/{commentId}/upvote")
    fun postUpvote(
        @Path("topicId") topicId: String,
        @Path("commentId") commentId: String,
        @Body vote: CommentVoteRequest
    ): Call<Void>
}