package com.clerami.universe.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.clerami.universe.data.remote.retrofit.ApiService
import com.clerami.universe.data.remote.response.RegisterRequest

import com.clerami.universe.utils.Resource
import kotlinx.coroutines.Dispatchers


class RegisterViewModel(private val apiService: ApiService) : ViewModel() {

    fun register(email: String, password: String, username: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading())
        try {
            val response = apiService.signUp(RegisterRequest(email, username, password)).execute()
            if (response.isSuccessful) {
                emit(Resource.success(response.body()))
            } else {
                emit(Resource.error("Registration failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            emit(Resource.error("Network error: ${e.message}"))
        }
    }


}

class RegisterViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
