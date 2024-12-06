package com.clerami.universe.ui.topic

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.Topic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicDetailActivity : AppCompatActivity() {
    private var isFavorite = false
    private var isLiked = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)
        sharedPreferences = getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)

        val topicId = intent.getStringExtra("topicId")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val postTitle = findViewById<TextView>(R.id.postTitle)
        val postDescription = findViewById<TextView>(R.id.postDescription)
        val readMore = findViewById<TextView>(R.id.readMore)
        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val replyButton = findViewById<Button>(R.id.aiAnswerButton)
        val repliesContainer = findViewById<LinearLayout>(R.id.repliesContainer)
        val tagsContainer = findViewById<LinearLayout>(R.id.tagsContainer)
        val tags = intent.getStringArrayListExtra("tags")
        val favoriteIcon = findViewById<ImageView>(R.id.favButton)
        val likeIcon = findViewById<ImageView>(R.id.likeIcon)

        isFavorite = sharedPreferences.getBoolean("isFavorite_$topicId", false)
        isLiked = sharedPreferences.getBoolean("isLiked_$topicId", false)

        updateFavoriteIcon(favoriteIcon)
        updateLikeIcon(likeIcon)

        favoriteIcon.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon(favoriteIcon)

            val editor = sharedPreferences.edit()
            editor.putBoolean("isFavorite_$topicId", isFavorite)
            editor.apply()
        }

        likeIcon.setOnClickListener {
            isLiked = !isLiked
            updateLikeIcon(likeIcon)

            val editor = sharedPreferences.edit()
            editor.putBoolean("isLiked_$topicId", isLiked)
            editor.apply()
        }

        postTitle.text = title
        postDescription.text = description

        closeButton.setOnClickListener {
            finish()
        }

        topicId?.let { fetchTopicDetails(it, postTitle, postDescription, tagsContainer) }
        topicId?.let { fetchComments(it, repliesContainer) }
    }

    private fun updateFavoriteIcon(favoriteIcon: ImageView) {
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.fav)
        } else {
            favoriteIcon.setImageResource(R.drawable.fav_outline)
        }
    }

    private fun updateLikeIcon(likeIcon: ImageView) {
        if (isLiked) {
            likeIcon.setImageResource(R.drawable.thumbs_up)
        } else {
            likeIcon.setImageResource(R.drawable.thumbs_up_outline)
        }
    }

    private fun fetchTopicDetails(topicId: String, postTitle: TextView, postDescription: TextView, tagsContainer: LinearLayout) {
        ApiConfig.getApiService(this).getTopicById(topicId).enqueue(object : Callback<Topic> {
            override fun onResponse(call: Call<Topic>, response: Response<Topic>) {
                if (response.isSuccessful) {
                    val topic = response.body()
                    topic?.let {
                        postTitle.text = it.title
                        postDescription.text = it.description
                        // Fetch and display tags dynamically
                        populateTags(tagsContainer, it.tags)
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

    private fun fetchComments(topicId: String, repliesContainer: LinearLayout) {
        ApiConfig.getApiService(this).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val comments = response.body()
                    if (comments != null && comments.isNotEmpty()) {
                        populateReplies(repliesContainer, comments)
                    } else {
                    }
                } else {
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.e("Error", "Error fetching comments: ${t.message}", t)
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun populateReplies(container: LinearLayout, replies: List<Comment>) {
        container.removeAllViews()
        for (reply in replies) {
            val replyView = layoutInflater.inflate(R.layout.item_reply, container, false)

            val replyUsername = replyView.findViewById<TextView>(R.id.replyUsername)
            val replyText = replyView.findViewById<TextView>(R.id.replyText)// Add in your layout if needed
            val likeCount = replyView.findViewById<TextView>(R.id.likeCount)
            val replyButton = replyView.findViewById<TextView>(R.id.replyButton)

            Log.d("TopicDetailActivity", "Upvotes for ${reply.commentId}: ${reply.upvotes}")

            replyUsername.text = reply.userId
            replyText.text = reply.commentText
            likeCount.text = reply.upvotes.toString()

            container.addView(replyView)
        }
    }

    private fun populateTags(container: LinearLayout, tags: List<String>) {
        container.removeAllViews()
        for (tag in tags) {
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
            container.addView(tagView)
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}