package com.clerami.universe.ui.addnewdicussion

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.CreateTopicResponse
import com.clerami.universe.data.remote.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddNewViewModel(application: Application) : ViewModel() {

    private val _responseLiveData = MutableLiveData<CreateTopicResponse>()
    val responseLiveData: LiveData<CreateTopicResponse> get() = _responseLiveData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = ApiConfig.getApiService(application)

    // Function to create topics
    fun createTopic(token: String, request: CreateTopicRequest) {
        _isLoading.value = true // Set loading to true

        val call = apiService.createTopic("Bearer $token", request)

        call.enqueue(object : Callback<CreateTopicResponse> {
            override fun onResponse(
                call: Call<CreateTopicResponse>,
                response: Response<CreateTopicResponse>
            ) {
                if (response.isSuccessful) {
                    // Log the raw response for debugging
                    Log.d("API Response", "Raw response: ${response.body()}")
                    _responseLiveData.value = response.body()
                } else {
                    _errorMessage.value = "Error: ${response.message()}"
                }
                _isLoading.value = false
            }

            override fun onFailure(call: Call<CreateTopicResponse>, t: Throwable) {
                _errorMessage.value = "Network error: ${t.message}"
                _isLoading.value = false
            }
        })
    }
}




