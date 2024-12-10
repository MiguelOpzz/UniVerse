package com.clerami.universe.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.response.Topic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> get() = _topics

    private var allTopics: MutableList<Topic> = mutableListOf()
    private var currentPage = 1
    private val pageSize = 10  // Fetch 10 topics per request

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags

    // Fetch topics from the API with pagination
    fun fetchTopics(context: Context) {
        ApiConfig.getApiService(context)
            .getAllTopics(page = currentPage, pageSize = pageSize)
            .enqueue(object : Callback<List<Topic>> {
                override fun onResponse(call: Call<List<Topic>>, response: Response<List<Topic>>) {
                    if (response.isSuccessful) {
                        val newTopics = response.body() ?: emptyList()

                        // Append new topics to the existing list
                        allTopics.addAll(newTopics)
                        _topics.value = allTopics

                        // Extract unique tags from the topics
                        val uniqueTags = allTopics.flatMap { it.tags ?: emptyList() }.distinct()
                        _tags.value = uniqueTags

                        // Increment the page number for the next fetch
                        currentPage++
                    } else {
                        Log.e("HomeViewModel", "Error fetching topics: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<Topic>>, t: Throwable) {
                    Log.e("HomeViewModel", "Failed to fetch topics: ${t.message}")
                }
            })
    }

    // Reset the pagination and topics
    fun resetPagination() {
        allTopics.clear()
        currentPage = 1
    }

    fun filterTopics(query: String) {
        val filteredTopics = allTopics.filter { topic ->
            topic.title.contains(query, ignoreCase = true) ||
                    (topic.tags?.any { it.contains(query, ignoreCase = true) } == true)
        }
        _topics.value = filteredTopics
    }

    fun filterTopicsByTag(tag: String) {
        val filteredTopics = allTopics.filter { topic ->
            topic.tags?.contains(tag) == true
        }
        _topics.value = filteredTopics
    }

    fun fetchTopicById(context: Context, topicId: String) {
        ApiConfig.getApiService(context).getTopicById(topicId).enqueue(object : Callback<Topic> {
            override fun onResponse(call: Call<Topic>, response: Response<Topic>) {
                if (response.isSuccessful) {
                    _topics.value = listOf(response.body() ?: return)  // Display the single topic
                } else {
                    Log.e("HomeViewModel", "Error fetching topic by ID: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Topic>, t: Throwable) {
                Log.e("HomeViewModel", "Failed to fetch topic by ID: ${t.message}")
            }
        })
    }

    fun clearFilters() {
        _topics.value = allTopics
    }
}
