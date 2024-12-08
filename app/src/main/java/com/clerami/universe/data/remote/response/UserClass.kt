package com.clerami.universe.data.remote.response

import java.sql.Time
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val token: String,
    val username:String
)


data class CreateTopicRequest(
    val title: String,
    val description: String,
    val tags: List<String>,
    val attachmentUrls: List<String>,
    )

data class CreateTopicResponse(
    val status: String,
    val message: String,

)

data class CreateTopicsRequest(
    val title: String,
    val description: String,
)

data class CreateTopicsResponse(
    val message: String,
    val topic : Topics
    )

data class Topics(
    val topicId: String,
    val title: String,
    val description: String?,
    val createdBy: String,
    val createdAt: Timestamp,
    val likes :Int
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
    val createdAt: Timestamp,
    val updatedAt:  Timestamp
)


data class Timestamp(
    val _seconds: Long,
    val _nanoseconds: Long
) {
    fun toReadableDate(): String {
        val instant = Instant.ofEpochSecond(_seconds, _nanoseconds.toLong())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }
}

data class Comment(
    val commentId: String,
    val userId: String,
    val commentText: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVotes: Map<String, String> = emptyMap()
)

data class CommentVoteRequest(
    val userId: String,
    val voteType: String
)
