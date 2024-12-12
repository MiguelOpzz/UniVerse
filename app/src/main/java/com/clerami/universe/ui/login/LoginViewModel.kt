package com.clerami.universe.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.clerami.universe.data.remote.retrofit.ApiService
import com.clerami.universe.data.remote.response.LoginRequest
import com.clerami.universe.utils.Resource
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

class LoginViewModel(private val apiService: ApiService) : ViewModel() {

    fun login(usernameOrEmail: String, password: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading())
        try {
            val response = apiService.login(LoginRequest(usernameOrEmail, password)).execute()
            if (response.isSuccessful) {
                emit(Resource.success(response.body()))
            } else {
                val errorMessage = try {
                    val errorJson = JSONObject(response.errorBody()?.string())
                    errorJson.getString("message")
                } catch (e: Exception) {
                    "An unexpected error occurred. Please try again."
                }
                emit(Resource.error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.error("Network error: ${e.message}"))
        }
    }
}

class LoginViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
