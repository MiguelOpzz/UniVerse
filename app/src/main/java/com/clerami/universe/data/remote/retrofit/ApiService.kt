package com.clerami.universe.data.remote.retrofit

import com.clerami.universe.data.RegisterUser
import org.w3c.dom.Comment
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

data class Topic(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String>,
    val userId: String,
    val createdAt: String
)

data class Comment(
    val id: String,
    val content: String,
    val userId: String,
    val createdAt: String
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

    // Create a new topic
    @POST("api/topics")
    fun createTopic(@Body topic: Topic): Call<Topic>

    // Get all topics
    @GET("api/topics")
    fun getAllTopics(): Call<List<Topic>>

    // Get a single topic by ID
    @GET("api/topics/{topicId}")
    fun getTopicById(@Path("topicId") topicId: String): Call<Topic>

    // Update an existing topic
    @PUT("api/topics/{topicId}")
    fun updateTopic(@Path("topicId") topicId: String, @Body topic: Topic): Call<Topic>

    // Delete a topic
    @DELETE("api/topics/{topicId}")
    fun deleteTopic(@Path("topicId") topicId: String): Call<Void>

    // Get comments for a topic
    @GET("api/topics/{topicId}/comments")
    fun getComments(@Path("topicId") topicId: String): Call<List<Comment>>

    // Post upvote/downvote for a comment
    @POST("api/topics/{topicId}/comments/{commentId}/upvote")
    fun postUpvote(@Path("topicId") topicId: String, @Path("commentId") commentId: String, @Body vote: CommentVoteRequest): Call<Void>

    @POST("api/topics/{topicId}/comments/{commentId}/downvote")
    fun postDownvote(@Path("topicId") topicId: String, @Path("commentId") commentId: String, @Body vote: CommentVoteRequest): Call<Void>
}
