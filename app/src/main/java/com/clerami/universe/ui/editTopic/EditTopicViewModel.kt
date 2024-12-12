package com.clerami.universe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clerami.universe.data.remote.response.UpdateResponse
import com.clerami.universe.data.remote.response.UpdateTopicRequest
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.retrofit.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = ApiConfig.getApiService(application)

    private val _updateState = MutableLiveData<UpdateState>()
    val updateState: LiveData<UpdateState> get() = _updateState


    fun updateTopic(token: String, topicId: String, request: UpdateTopicRequest) {
        // Set loading state
        _updateState.value = UpdateState.Loading

        apiService.updateTopic(token, topicId, request).enqueue(object : Callback<UpdateResponse> {
            override fun onResponse(call: Call<UpdateResponse>, response: Response<UpdateResponse>) {
                if (response.isSuccessful) {
                    val updatedTopic = response.body()

                    _updateState.value = UpdateState.Success(updatedTopic)
                } else {

                    _updateState.value = UpdateState.Error("Failed to update topic: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {

                _updateState.value = UpdateState.Error("Network error: ${t.message}")
            }
        })
    }


    sealed class UpdateState {
        object Loading : UpdateState()
        data class Success(val updateResponse: UpdateResponse?) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
}
