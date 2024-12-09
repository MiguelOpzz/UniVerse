package com.clerami.universe.ui.addnewdicussion

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.CreateTopicResponse
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddNewViewModel(application: Application) : ViewModel() {

    private val _responseLiveData = MutableLiveData<CreateTopicResponse>()
    val responseLiveData: LiveData<CreateTopicResponse> get() = _responseLiveData

    private val _errorMessage = MutableLiveData<CreateTopicResponse>()
    val errorMessage: LiveData<CreateTopicResponse> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = ApiConfig.getApiService(application)

    private var isRequestInProgress = false

    fun createTopic(token: String, request: CreateTopicRequest) {
        if (isRequestInProgress) {
            return
        }

        isRequestInProgress = true
        _isLoading.value = true

        val call = apiService.createTopic("Bearer $token", request)

        call.enqueue(object : Callback<CreateTopicResponse> {
            override fun onResponse(
                call: Call<CreateTopicResponse>,
                response: Response<CreateTopicResponse>
            ) {
                if (response.isSuccessful) {
                    _responseLiveData.value = response.body()
                } else {
                    val errorResponse = response.errorBody()?.string()
                    if (!errorResponse.isNullOrEmpty()) {
                        try {
                            val serverError = Gson().fromJson(errorResponse, CreateTopicResponse::class.java)
                            _errorMessage.value = serverError // Set the entire response as the error
                        } catch (e: Exception) {
                            // Handle JSON parsing error if the error response is malformed
                            _errorMessage.value = CreateTopicResponse("fail", "Unexpected error occurred.")
                        }
                    } else {
                        _errorMessage.value = CreateTopicResponse("fail", "Error: ${response.message()}")
                    }
                }
                isRequestInProgress = false
                _isLoading.value = false
            }

            override fun onFailure(call: Call<CreateTopicResponse>, t: Throwable) {
                _errorMessage.value = CreateTopicResponse("fail", "Network error: ${t.message}")
                isRequestInProgress = false
                _isLoading.value = false
            }
        })
    }
}


