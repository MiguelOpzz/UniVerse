package com.clerami.universe.data.remote.retrofit

import com.clerami.universe.data.RegisterUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


data class GuestResponse(val success: Boolean, val message: String)
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val userId: String
)


data class CreateTopicRequest(
    val title: String,
    val description: String,
    val createdBy: String,
    val major: String,
    val tags: List<String>
)

data class UpdateTopicRequest(
    val title: String,
    val tags: List<String>,
    val body: String
)

data class Topic(
    val topicId: String,
    val attachmentUrls: List<Any>,
    val title: String,
    val description: String?,
    val createdBy: String,
    val tags: List<String>,
    val isNSFW: Boolean,
    val postCount: Int,
    val createdAt: Map<String, Any>,
    val updatedAt: Map<String, Any>
)

data class Comment(
    val commentId: String,
    val userId: String,
    val commentText: String,
    val createdAt: Long,
    val updatedAt: Long,
    val upvotes: Int,
    val downvotes: Int,
    val userVotes: Map<String, String>
)

data class CommentVoteRequest(
    val userId: String,
    val voteType: String
)

interface ApiService {

    @POST("api/signup")
    fun signUp(@Body user: RegisterUser): Call<GuestResponse>


    @POST("api/guest")
    fun guestLogin(): Call<GuestResponse>


    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/topics")
    fun createTopic(@Body request: CreateTopicRequest): Call<Topic>

    @GET("api/topics")
    fun getAllTopics(): Call<List<Topic>>

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

    @POST("api/topics/{topicId}/comments/{commentId}/upvote")
    fun postUpvote(
        @Path("topicId") topicId: String,
        @Path("commentId") commentId: String,
        @Body vote: CommentVoteRequest
    ): Call<Void>
}