package com.clerami.universe.ui.topicdetail

import android.os.Bundle
import android.util.Log
import android.widget.TextView
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

        val topicId = intent.getStringExtra("topicId")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")

        val titleTextView = findViewById<TextView>(R.id.topicTitle)
        val descriptionTextView = findViewById<TextView>(R.id.topicDescription)
        val commentsCountTextView = findViewById<TextView>(R.id.commentsCount)
        val likesCountTextView = findViewById<TextView>(R.id.likesCount)

        titleTextView.text = title
        descriptionTextView.text = description

        // Fetch comments for this topic
        if (topicId != null) {
            fetchCommentsForTopic(topicId, commentsCountTextView, likesCountTextView)
        }
    }

    private fun fetchCommentsForTopic(
        topicId: String,
        commentsCountTextView: TextView,
        likesCountTextView: TextView
    ) {
        ApiConfig.getApiService(this).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val comments = response.body()
                    if (comments != null && comments.isNotEmpty()) {
                        val likesCount = comments.sumOf { it.upvotes }
                        commentsCountTextView.text = "${comments.size} replies"
                        likesCountTextView.text = "$likesCount likes"
                    } else {
                        commentsCountTextView.text = "No replies yet"
                        likesCountTextView.text = "No likes yet"
                    }
                } else {
                    commentsCountTextView.text = "Error loading replies"
                    likesCountTextView.text = "Error loading likes"
                    Log.e("TopicDetailActivity", "Error fetching comments: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                commentsCountTextView.text = "Failed to load replies"
                likesCountTextView.text = "Failed to load likes"
                Log.e("TopicDetailActivity", "Failed to fetch comments: ${t.localizedMessage}", t)
            }
        })
    }
}
