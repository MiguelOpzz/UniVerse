package com.clerami.universe.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.data.remote.response.TopicsResponse
import com.clerami.universe.data.remote.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardViewModel : ViewModel() {

    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> get() = _topics

    private val _filteredTopics = MutableLiveData<List<Topic>>()
    val filteredTopics: LiveData<List<Topic>> get() = _filteredTopics

    private var allTopics: MutableList<Topic> = mutableListOf()
    private var currentPage = 1
    private val pageSize = 10  // Fetch 10 topics per request



    // Fetch topics from the API with pagination
    fun fetchTopics(context: Context) {
        ApiConfig.getApiService(context)
            .getAllTopics(page = currentPage, pageSize = pageSize)
            .enqueue(object : Callback<TopicsResponse> {  // Change the expected response type to TopicsResponse
                override fun onResponse(call: Call<TopicsResponse>, response: Response<TopicsResponse>) {
                    if (response.isSuccessful) {
                        val topicsResponse = response.body() // The response will now contain TopicsResponse

                        // If topicsResponse is not null, extract topics and handle pagination
                        topicsResponse?.let {
                            val newTopics = it.topics  // Extract topics list

                            // Append new topics to the existing list
                            allTopics.addAll(newTopics)
                            _topics.value = allTopics

                            // Handle pagination, increment currentPage only if there is a nextCursor
                            if (it.nextCursor != null) {
                                currentPage++  // Increment page number for the next fetch if there's a nextCursor
                            }
                        } ?: run {
                            Log.e("Dasboard", "No topics found in response")
                        }
                    } else {
                        Log.e("Dashboard", "Error fetching topics: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<TopicsResponse>, t: Throwable) {
                    Log.e("Dashboard", "Failed to fetch topics: ${t.message}")
                }
            })
    }


    fun filterTopics(query: String) {
        val currentTopics = _topics.value ?: emptyList()
        val filtered = if (query.isEmpty()) {
            currentTopics
        } else {
            currentTopics.filter { topic ->
                topic.title.contains(query, ignoreCase = true) ||
                        topic.description?.contains(query, ignoreCase = true) == true ||
                        topic.tags?.any { it.contains(query, ignoreCase = true) } == true
            }
        }
        _filteredTopics.value = filtered
    }
}
