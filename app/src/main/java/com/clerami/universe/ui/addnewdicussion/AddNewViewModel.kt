package com.clerami.universe.ui.addnewdicussion

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.CreateTopicResponse
import com.clerami.universe.data.remote.response.CreateTopicsRequest
import com.clerami.universe.data.remote.response.CreateTopicsResponse
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.retrofit.ApiService
import com.clerami.universe.utils.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AddNewViewModel(application: Application) : ViewModel() {

    private val _responseLiveData = MutableLiveData<CreateTopicsResponse>()
    val responseLiveData: LiveData<CreateTopicsResponse> get() = _responseLiveData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = ApiConfig.getApiService(application)

    // Function to create topics
    fun createTopics(token: String, request: CreateTopicsRequest) {
        _isLoading.value = true // Set loading to true

        val call = apiService.createTopics("Bearer $token", request)

        call.enqueue(object : Callback<CreateTopicsResponse> {
            override fun onResponse(
                call: Call<CreateTopicsResponse>,
                response: Response<CreateTopicsResponse>
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

            override fun onFailure(call: Call<CreateTopicsResponse>, t: Throwable) {
                _errorMessage.value = "Network error: ${t.message}"
                _isLoading.value = false
            }
        })
    }
}




