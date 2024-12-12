package com.clerami.universe.data.repository

import android.content.Context
import com.clerami.universe.data.local.AppDatabase
import com.clerami.universe.data.local.FavoritePost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavoriteRepository(
    context: Context
) {
    private val favoriteDao = AppDatabase.getDatabase(context).favoritePostDao()

    suspend fun getFavoritePostByTopicId(topicId: String): FavoritePost? {
        return withContext(Dispatchers.IO) {
            favoriteDao.getFavoritePostByTopicId(topicId)
        }
    }

    fun getFavoritePosts(): Flow<List<FavoritePost>> {
        return favoriteDao.getAllFavorites()
    }

    suspend fun insertFavoritePost(favoritePost: FavoritePost) {
        withContext(Dispatchers.IO) {
            favoriteDao.insert(favoritePost)
        }
    }

    suspend fun deleteFavoritePost(topicId: String) {
        withContext(Dispatchers.IO) {
            favoriteDao.deleteFavoritePost(topicId)
        }
    }
}