package com.clerami.universe.ui.topicdetail

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.retrofit.Comment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        // Retrieve passed data
        val topicId = intent.getStringExtra("topicId")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")

        // Find views
        val postTitle = findViewById<TextView>(R.id.postTitle)
        val postDescription = findViewById<TextView>(R.id.postDescription)
        val readMore = findViewById<TextView>(R.id.readMore)
        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val replyButton = findViewById<Button>(R.id.aiAnswerButton)
        val repliesContainer = findViewById<LinearLayout>(R.id.repliesContainer)

        // Set static data
        postTitle.text = title
        postDescription.text = description

        // Event Listeners
        closeButton.setOnClickListener {
            finish() // Close the activity
        }

        readMore.setOnClickListener {
            Toast.makeText(this, "Read More Clicked!", Toast.LENGTH_SHORT).show()
        }

        replyButton.setOnClickListener {
            Toast.makeText(this, "AI Answer Button Clicked!", Toast.LENGTH_SHORT).show()
        }

        // Fetch and populate comments
        topicId?.let { fetchComments(it, repliesContainer) }
    }

    private fun fetchComments(topicId: String, repliesContainer: LinearLayout) {
        ApiConfig.getApiService(this).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val comments = response.body()
                    if (comments != null && comments.isNotEmpty()) {
                        populateReplies(repliesContainer, comments)
                    } else {
                        showToast("No replies found.")
                    }
                } else {
                    showToast("Failed to load replies.")
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun populateReplies(container: LinearLayout, replies: List<Comment>) {
        container.removeAllViews()
        for (reply in replies) {
            val replyView = layoutInflater.inflate(R.layout.item_reply, container, false)

            val replyUsername = replyView.findViewById<TextView>(R.id.replyUsername)
            val replyText = replyView.findViewById<TextView>(R.id.replyText)
            val likeCount = replyView.findViewById<TextView>(R.id.likeCount)
            val replyButton = replyView.findViewById<TextView>(R.id.replyButton)

            replyUsername.text = reply.userId
            replyText.text = reply.commentText
            likeCount.text = reply.upvotes.toString()

            replyButton.setOnClickListener {
                showToast("Replying to ${reply.userId}")
            }

            container.addView(replyView)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
