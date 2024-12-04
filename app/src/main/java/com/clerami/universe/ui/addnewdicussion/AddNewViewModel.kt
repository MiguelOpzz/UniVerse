package com.clerami.universe.ui.addnewdicussion

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.retrofit.CreateTopicRequest
import com.clerami.universe.data.remote.retrofit.Topic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddNewViewModel : ViewModel() {
    private val _createTopicStatus = MutableLiveData<String>()
    val createTopicStatus: LiveData<String> get() = _createTopicStatus

    fun createNewTopic(request: CreateTopicRequest, context: Context) {
        // Use ApiConfig with context to dynamically add the token
        ApiConfig.getApiService(context).createTopic(request).enqueue(object : Callback<Topic> {
            override fun onResponse(call: Call<Topic>, response: Response<Topic>) {
                if (response.isSuccessful) {
                    _createTopicStatus.value = "Discussion created successfully!"
                    Log.d("AddNewViewModel", "Response: ${response.body()}")
                } else {
                    _createTopicStatus.value = "Failed to create discussion. Try again."
                    Log.e("AddNewViewModel", "Error Response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Topic>, t: Throwable) {
                _createTopicStatus.value = "Error: ${t.message}"
                Log.e("AddNewViewModel", "Error creating discussion: ${t.message}")
            }
        })
    }
}