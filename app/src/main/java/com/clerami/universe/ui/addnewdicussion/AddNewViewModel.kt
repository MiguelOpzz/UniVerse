package com.clerami.universe.ui.addnewdicussion

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.CreateTopicResponse
import com.clerami.universe.data.remote.retrofit.ApiService
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class AddNewViewModel : ViewModel() {

    private val _responseLiveData = MutableLiveData<CreateTopicResponse>()
    val responseLiveData: LiveData<CreateTopicResponse> get() = _responseLiveData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun createTopic(apiService: ApiService, token: String, request: CreateTopicRequest) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val response = apiService.createTopic("Bearer $token", request)
                handleResponse(response)
            } catch (e: IOException) {
                Log.e("AddNewViewModel", "Network Error: ${e.localizedMessage}")
                _errorMessage.postValue("Network error. Please check your connection.")
            } catch (e: Exception) {
                Log.e("AddNewViewModel", "Request failed: ${e.localizedMessage}")
                _errorMessage.postValue("Something went wrong. Please try again.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun handleResponse(response: Response<CreateTopicResponse>) {
        if (response.isSuccessful) {
            response.body()?.let {
                _responseLiveData.postValue(it)
            } ?: run {
                Log.e("AddNewViewModel", "Error: Response body is null")
                _errorMessage.postValue("Response body is null")
            }
        } else {
            val errorBody = parseErrorResponse(response.errorBody()?.string())
            Log.e("AddNewViewModel", "Error Code: ${response.code()} | Body: $errorBody")
            _errorMessage.postValue("Error Code: ${response.code()} | $errorBody")
        }
    }

    private fun parseErrorResponse(errorBody: String?): String {
        return try {
            errorBody?.let {
                val json = JSONObject(it)
                json.optString("message", json.toString(4)) // Show full JSON if message is absent
            } ?: "Unknown error occurred"
        } catch (e: JSONException) {
            Log.e("AddNewViewModel", "Error parsing response: $errorBody")
            "Failed to parse server error."
        }
    }
}
