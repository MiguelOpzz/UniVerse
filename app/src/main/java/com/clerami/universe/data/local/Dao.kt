package com.clerami.universe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePostDao {
    @Insert
    suspend fun insert(favoritePost: FavoritePost)

    @Query("SELECT * FROM favorite_posts")
    fun getAllFavorites(): Flow<List<FavoritePost>>

    @Query("SELECT * FROM favorite_posts WHERE topicId = :topicId LIMIT 1")
    suspend fun getFavoritePostByTopicId(topicId: String): FavoritePost?

    @Query("DELETE FROM favorite_posts WHERE topicId = :topicId")
    suspend fun deleteFavoritePost(topicId: String)
}

