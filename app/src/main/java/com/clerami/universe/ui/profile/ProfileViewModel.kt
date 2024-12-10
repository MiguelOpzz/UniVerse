package com.clerami.universe.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerami.universe.data.local.FavoritePost
import com.clerami.universe.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application
): AndroidViewModel(application) {
    private val favoriteRepository = FavoriteRepository(application.applicationContext)

    val favoritePosts = favoriteRepository.getFavoritePosts()
}
