package com.clerami.universe.ui.topic

import TopicDetailViewModel
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.clerami.universe.R
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.databinding.ActivityTopicDetailBinding

class TopicDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopicDetailBinding

    // Using ViewModelProvider with ViewModelFactory to inject Application context
    private val viewModel: TopicDetailViewModel by viewModels { TopicDetailViewModelFactory(application) }

    private var isFavorite = false
    private var isLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topicId = intent.getStringExtra("topicId") ?: return
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val tags = intent.getStringArrayListExtra("tags")

        // Initialize ViewModel and fetch data
        viewModel.getTopicDetails(topicId)
        viewModel.getComments(topicId)

        // Observe LiveData
        viewModel.topicDetails.observe(this, Observer { topic ->
            binding.postTitle.text = topic.title
            binding.postDescription.text = topic.description
            populateTags(tags, topic.tags)
        })

        viewModel.comments.observe(this, Observer { comments ->
            populateReplies(comments)
        })

        // Handle favorite and like state
        isFavorite = viewModel.isFavorite(topicId)
        isLiked = viewModel.isLiked(topicId)

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
            viewModel.setFavorite(topicId, isFavorite)
        }

        // Like icon click listener
        binding.likeIcon.setOnClickListener {
            isLiked = !isLiked
            updateLikeIcon()
            viewModel.setLiked(topicId, isLiked)
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
