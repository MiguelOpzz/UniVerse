package com.clerami.universe.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.data.remote.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> get() = _topics

    // Holds the full list of topics (before filtering)
    private var allTopics = listOf<Topic>()

    // Fetch topics from API
    fun fetchTopics(context: Context) {
        fetchTopicsFromApi(context)
    }

    private fun fetchTopicsFromApi(context: Context) {
        ApiConfig.getApiService(context).getAllTopics().enqueue(object : Callback<List<Topic>> {
            override fun onResponse(call: Call<List<Topic>>, response: Response<List<Topic>>) {
                if (response.isSuccessful) {
                    allTopics = response.body() ?: emptyList()
                    _topics.value = allTopics
                    Log.d("HomeViewModel", "Fetched topics: ${allTopics}")
                } else {
                    Log.e("HomeViewModel", "Error fetching topics: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Topic>>, t: Throwable) {
                Log.e("HomeViewModel", "Failed to fetch topics: ${t.message}")
            }
        })
    }

    fun filterTopics(query: String) {
        val filteredTopics = if (query.isNotEmpty()) {
            allTopics.filter { topic ->
                topic.title.contains(query, ignoreCase = true) ||
                        topic.description?.contains(query, ignoreCase = true) == true
            }
        } else {
            allTopics
        }
        _topics.value = filteredTopics
    }
}
