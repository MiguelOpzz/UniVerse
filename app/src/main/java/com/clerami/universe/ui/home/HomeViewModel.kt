package com.clerami.universe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _isSearchActive = MutableLiveData(false)
    val isSearchActive: LiveData<Boolean> get() = _isSearchActive

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery

    fun toggleSearchBar(isActive: Boolean) {
        _isSearchActive.value = isActive
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
