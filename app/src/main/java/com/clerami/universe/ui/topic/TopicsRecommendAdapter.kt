package com.clerami.universe.ui.topic

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clerami.universe.data.remote.response.RecommendResponse
import com.clerami.universe.databinding.ItemRecommendBinding

class TopicRecommendAdapter(
    private val context: Context,
    recommendResponses: List<RecommendResponse> = emptyList() // Default empty list
) : RecyclerView.Adapter<TopicRecommendAdapter.TopicViewHolder>() {

    private var topicList: List<RecommendResponse> = recommendResponses

    // Method to update the list
    fun updateList(newList: List<RecommendResponse>?) {
        if (newList == null || newList.isEmpty()) {
            Log.d("TopicRecommendAdapter", "Received an empty or null list")
            return
        }

        Log.d("TopicRecommendAdapter", "Updating list with ${newList.size} items")
        topicList = newList
        notifyDataSetChanged() // Notify the adapter that data has changed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemRecommendBinding.inflate(LayoutInflater.from(context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topicList[position]
        Log.d("TopicRecommendAdapter", "Binding topic at position $position: ${topic.title}")
        holder.binding.savedTitle.text = topic.title ?: "No Title" // Handle null title gracefully
    }

    override fun getItemCount(): Int = topicList.size

    inner class TopicViewHolder(val binding: ItemRecommendBinding) : RecyclerView.ViewHolder(binding.root)
}

