package com.clerami.universe.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.retrofit.Topic
import com.clerami.universe.data.remote.retrofit.Comment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> get() = _topics

    fun fetchTopics() {
        ApiConfig.getApiService().getAllTopics().enqueue(object : Callback<List<Topic>> {
            override fun onResponse(call: Call<List<Topic>>, response: Response<List<Topic>>) {
                if (response.isSuccessful) {
                    _topics.value = response.body()
                } else {
                    Log.e("HomeViewModel", "Error fetching topics: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Topic>>, t: Throwable) {
                Log.e("HomeViewModel", "Failed to fetch topics: ${t.message}")
            }
        })
    }

    fun fetchComments(topicId: String, callback: (List<Comment>) -> Unit) {
        ApiConfig.getApiService().getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    callback(response.body() ?: emptyList())
                } else {
                    Log.e("HomeViewModel", "Error fetching comments: ${response.errorBody()?.string()}")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.e("HomeViewModel", "Failed to fetch comments: ${t.message}")
                callback(emptyList())
            }
        })
    }
}
