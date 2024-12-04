package com.clerami.universe.data.remote.response

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

data class GuestResponse(
    val success: Boolean,
    val message: String
)

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String
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