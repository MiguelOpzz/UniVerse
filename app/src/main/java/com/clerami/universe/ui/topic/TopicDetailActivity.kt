package com.clerami.universe.ui.topic

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.databinding.ActivityTopicDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicDetailActivity : AppCompatActivity() {
    private var isFavorite = false
    private var isLiked = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityTopicDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityTopicDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)

        val topicId = intent.getStringExtra("topicId")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val tags = intent.getStringArrayListExtra("tags")

        // Update favorite and like status based on shared preferences
        topicId?.let {
            isFavorite = sharedPreferences.getBoolean("isFavorite_$it", false)
            isLiked = sharedPreferences.getBoolean("isLiked_$it", false)
        }

        // Update the favorite and like icons
        updateFavoriteIcon()
        updateLikeIcon()

        // Set title and description from the intent
        binding.postTitle.text = title
        binding.postDescription.text = description

        // Close button functionality
        binding.closeButton.setOnClickListener {
            finish()
        }

        // Favorite icon click listener
        binding.favButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()

            val editor = sharedPreferences.edit()
            topicId?.let { editor.putBoolean("isFavorite_$it", isFavorite) }
            editor.apply()
        }

        // Like icon click listener
        binding.likeIcon.setOnClickListener {
            isLiked = !isLiked
            updateLikeIcon()

            val editor = sharedPreferences.edit()
            topicId?.let { editor.putBoolean("isLiked_$it", isLiked) }
            editor.apply()
        }

        // Fetch topic details and comments
        topicId?.let {
            fetchTopicDetails(it, tags)
            fetchComments(it)
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            binding.favButton.setImageResource(R.drawable.fav)
        } else {
            binding.favButton.setImageResource(R.drawable.fav_outline)
        }
    }

    private fun updateLikeIcon() {
        if (isLiked) {
            binding.likeIcon.setImageResource(R.drawable.thumbs_up)
        } else {
            binding.likeIcon.setImageResource(R.drawable.thumbs_up_outline)
        }
    }

    private fun fetchTopicDetails(topicId: String, tags: List<String>?) {
        ApiConfig.getApiService(this).getTopicById(topicId).enqueue(object : Callback<Topic> {
            override fun onResponse(call: Call<Topic>, response: Response<Topic>) {
                if (response.isSuccessful) {
                    val topic = response.body()
                    topic?.let {
                        binding.postTitle.text = it.title
                        binding.postDescription.text = it.description
                        populateTags(tags, it.tags)
                    }
                } else {
                    showToast("Failed to load topic details")
                }
            }

            override fun onFailure(call: Call<Topic>, t: Throwable) {
                Log.e("TopicDetailActivity", "Error fetching topic details: ${t.message}", t)
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun fetchComments(topicId: String) {
        ApiConfig.getApiService(this).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val comments = response.body()
                    if (comments != null && comments.isNotEmpty()) {
                        populateReplies(comments)
                    }
                } else {
                    showToast("Failed to load comments")
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.e("Error", "Error fetching comments: ${t.message}", t)
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun populateReplies(replies: List<Comment>) {
        binding.repliesContainer.removeAllViews()
        for (reply in replies) {
            val replyView = layoutInflater.inflate(R.layout.item_reply, binding.repliesContainer, false)

            val replyUsername = replyView.findViewById<TextView>(R.id.replyUsername)
            val replyText = replyView.findViewById<TextView>(R.id.replyText)
            val likeCount = replyView.findViewById<TextView>(R.id.likeCount)

            replyUsername.text = reply.userId
            replyText.text = reply.commentText
            likeCount.text = reply.upvotes.toString()

            binding.repliesContainer.addView(replyView)
        }
    }

    private fun populateTags(tags: List<String>?, topicTags: List<String>) {
        binding.tagsContainer.removeAllViews()
        val combinedTags = tags.orEmpty() + topicTags
        for (tag in combinedTags) {
            val tagView = TextView(this).apply {
                text = tag
                setPadding(8, 4, 8, 4)
                background = ContextCompat.getDrawable(this@TopicDetailActivity, R.drawable.tag_background)
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@TopicDetailActivity, R.color.white))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
            }
            binding.tagsContainer.addView(tagView)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
