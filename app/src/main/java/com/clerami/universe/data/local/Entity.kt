package com.clerami.universe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_posts")
data class FavoritePost(
    @PrimaryKey val topicId: String,
    val title: String,
    val description: String
)
