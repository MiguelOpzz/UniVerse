package com.clerami.universe.ui.profilesettings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clerami.universe.data.remote.response.UpdateUser
import com.clerami.universe.data.remote.response.UpdateUserResponse
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.retrofit.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = ApiConfig.getApiService(application)

    private val _updateState = MutableLiveData<UpdateState>()
    val updateState: LiveData<UpdateState> get() = _updateState

    fun updateProfile(token: String, username: String, request: UpdateUser) {
        _updateState.value = UpdateState.Loading

        // Retrofit API call to update the user profile
        apiService.updateProfile("Bearer $token", username, request).enqueue(object : Callback<UpdateUserResponse> {
            override fun onResponse(call: Call<UpdateUserResponse>, response: Response<UpdateUserResponse>) {
                if (response.isSuccessful) {
                    // On success, update state with updated fields
                    val updatedResponse = response.body()
                    _updateState.value = UpdateState.Success(updatedResponse)
                } else {
                    // If the API call fails, show error message
                    _updateState.value = UpdateState.Error("Error updating profile: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                // Handle network failure
                _updateState.value = UpdateState.Error("Network error: ${t.localizedMessage}")
            }
        })
    }

    // Sealed class to represent different states of the update process
    sealed class UpdateState {
        object Loading : UpdateState()
        data class Success(val updateResponse: UpdateUserResponse?) : UpdateState()
        data class Error(val message: String) : UpdateState()
    } class Error(val message: String) : UpdateState()
}

