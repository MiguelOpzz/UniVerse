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

    private var allTopics: List<Topic> = emptyList()

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags

    // Fetch topics from the API
    fun fetchTopics(context: Context) {
        ApiConfig.getApiService(context).getAllTopics().enqueue(object : Callback<List<Topic>> {
            override fun onResponse(call: Call<List<Topic>>, response: Response<List<Topic>>) {
                if (response.isSuccessful) {
                    allTopics = response.body() ?: emptyList()
                    _topics.value = allTopics

                    // Extract unique tags from the topics
                    val uniqueTags = allTopics.flatMap { it.tags ?: emptyList() }.distinct()
                    _tags.value = uniqueTags
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
        val filteredTopics = allTopics.filter { topic ->
            topic.title.contains(query) ||
                    (topic.tags?.any { it.contains(query) } == true)
        }
        _topics.value = filteredTopics
    }


    fun filterTopicsByTag(tag: String) {
        val filteredTopics = allTopics.filter { topic ->
            topic.tags?.contains(tag) == true
        }
        _topics.value = filteredTopics
    }


    fun clearFilters() {
        _topics.value = allTopics
    }
}
