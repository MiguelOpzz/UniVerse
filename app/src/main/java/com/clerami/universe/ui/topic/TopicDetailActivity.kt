package com.clerami.universe.ui.topic

import TopicDetailViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.clerami.universe.R
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.databinding.ActivityTopicDetailBinding
import com.clerami.universe.ui.editTopic.EditTopicActivity

import com.clerami.universe.utils.SessionManager
import kotlinx.coroutines.launch

class TopicDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopicDetailBinding

    private val viewModel: TopicDetailViewModel by viewModels { TopicDetailViewModelFactory(application) }

    private var isFavorite = false
    private var isLiked = false
    private val savedTopics = mutableListOf<String>()
    private lateinit var sessionManager: SessionManager
    private lateinit var refreshReceiver: BroadcastReceiver
    private var isReceiverRegistered = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val topicId = intent.getStringExtra("topicId") ?: return
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        refreshReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.getTopicDetails(topicId)
            }
        }
        val filter = IntentFilter("com.clerami.universe.ACTION_REFRESH_TOPIC")
        ContextCompat.registerReceiver(this, refreshReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)


        viewModel.getTopicDetails(topicId)
        viewModel.getComments(topicId)

        viewModel.topicDetails.observe(this, Observer { topic ->
            binding.postTitle.text = topic.title
            binding.postDescription.text = topic.description
            populateTags(topic.tags)
            val attachmentUrls = topic.attachmentUrls as? List<String> ?: emptyList()

            Glide.with(this)
                .load(topic.attachmentUrls.firstOrNull())
                .into(binding.postMedia)
            binding.threeDots.setOnClickListener {
                showPopupMenu(it, topicId, topic.title, description, topic.tags, attachmentUrls)
            }
            checkDescriptionLines()
        })

        viewModel.comments.observe(this, Observer { comments ->
            populateReplies(comments)
        })

        lifecycleScope.launch {
            isFavorite = viewModel.isFavorite(topicId)
            updateFavoriteIcon()
        }
        isLiked = viewModel.isLiked(topicId)

        updateLikeIcon()

        binding.postTitle.text = title
        binding.postDescription.text = description

        viewModel.deleteResponse.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Topic deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to delete topic", Toast.LENGTH_SHORT).show()
            }
        })

        binding.reply.setOnClickListener {
            binding.replyLayout.visibility = View.VISIBLE
        }

        binding.replyButton.setOnClickListener {
            val replyText = binding.replyInput.text.toString().trim()

            if (replyText.isNotEmpty()) {
                viewModel.createComment(topicId, replyText) // Posting the comment
                binding.replyInput.text.clear()
                showToast("Your reply has been posted.")
                hideKeyboard()
            } else {
                showToast("Please write a reply.")
            }
        }

        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.closeAiButton.setOnClickListener {
            binding.aiAnswerLayout.visibility = View.GONE
            binding.aiAnswerButton.visibility = View.VISIBLE
        }

        binding.aiAnswerButton.setOnClickListener {
            // Show the expanded AI answer and summary
            binding.aiAnswerLayout.visibility = View.VISIBLE
            binding.aiAnswerButton.visibility = View.GONE
        }

        binding.favButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()
            lifecycleScope.launch {
                viewModel.setFavorite(topicId, isFavorite)
            }
        }

        binding.likeIcon.setOnClickListener {
            isLiked = !isLiked
            updateLikeIcon()
            viewModel.setLiked(topicId, isLiked)
        }

        binding.replyButton.setOnClickListener {
            val replyText = binding.replyInput.text.toString().trim()

            if (replyText.isNotEmpty()) {
                viewModel.createComment(topicId, replyText)

                binding.replyInput.text.clear()

                showToast("Your reply has been posted.")

                hideKeyboard()

                updateRepliesUI(replyText)
            } else {
                showToast("Please write a reply.")
            }
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

    private fun checkDescriptionLines() {
        val postDescription = binding.postDescription
        postDescription.post {
            val lineCount = postDescription.layout.lineCount
            if (lineCount <= 4) {
                binding.readMore.visibility = View.GONE
            } else {
                binding.readMore.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val topicId = intent.getStringExtra("topicId") ?: return
        viewModel.getTopicDetails(topicId)  // Refresh the topic data
    }

    override fun onStop() {
        super.onStop()
        // Unregister the receiver when the Activity is stopped
        if (isReceiverRegistered) {
            unregisterReceiver(refreshReceiver)
            isReceiverRegistered = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure receiver is unregistered when the activity is destroyed to avoid memory leaks
        if (isReceiverRegistered) {
            unregisterReceiver(refreshReceiver)
            isReceiverRegistered = false
        }
    }


    private fun showPopupMenu(view: View, topicId: String,title:String,description:String,tags: List<String>?,attachmentUrls:List<String>) {
        // Create a PopupMenu
        val popupMenu = PopupMenu(this, view)
        val menu = popupMenu.menu
        val currentUserName = sessionManager.getUserName()
        viewModel.topicDetails.observe(this, Observer { topic ->
            val topicCreator = topic.createdBy


            if (currentUserName == topicCreator) {

                menu.add(Menu.NONE, R.id.menu_edit, Menu.NONE, "Edit Topic")
            }
            menu.add(Menu.NONE, R.id.menu_delete, Menu.NONE, "Delete Topic")


            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_delete -> {
                        showDeleteConfirmationDialog(topicId)
                        true
                    }
                    R.id.menu_edit -> {
                        // Handle edit logic here (e.g., open edit screen)
                        val intent = Intent(this, EditTopicActivity::class.java)
                        intent.putExtra("topicId", topicId)
                        intent.putExtra("title", title)
                        intent.putExtra("description", description)

                        if (tags != null && tags.isNotEmpty()) {
                            Log.d("TopicDetailActivity", "Tags being passed: $tags")
                            intent.putStringArrayListExtra("tags", ArrayList(tags)) // Pass as ArrayList
                        }

                        if (attachmentUrls != null && attachmentUrls.isNotEmpty()) {
                            intent.putStringArrayListExtra("attachments", ArrayList(attachmentUrls))
                        }
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
        })


        popupMenu.show()
    }



    // Show confirmation dialog before deletion
    private fun showDeleteConfirmationDialog(topicId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Topic")
            .setMessage("Are you sure you want to delete this topic?")
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.deleteTopic(topicId)  // Call delete method in ViewModel
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()  // Close the dialog
            }
            .create()
            .show()
    }



    private fun populateReplies(replies: List<Comment>) {
        binding.repliesContainer.removeAllViews()
        for (reply in replies) {
            val replyView = layoutInflater.inflate(R.layout.item_reply, binding.repliesContainer, false)
            val replyUsername = replyView.findViewById<TextView>(R.id.replyUsername)
            val replyText = replyView.findViewById<TextView>(R.id.replyText)
            val likeCount = replyView.findViewById<TextView>(R.id.likeCount)

            replyUsername.text = getUserName()
            replyText.text = reply.commentText
            likeCount.text = reply.upvotes.toString()

            binding.repliesContainer.addView(replyView)
        }
    }

    private fun populateTags(tags: List<String>) {
        binding.tagsContainer.removeAllViews()
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
            binding.tagsContainer.addView(tagView)
        }
    }


    private fun addToSavedTopics(topicId: String) {
        if (!savedTopics.contains(topicId)) {
            savedTopics.add(topicId)
            showToast("Topic added to saved discussions!")
        }
    }

    private fun removeFromSavedTopics(topicId: String) {
        savedTopics.remove(topicId)
        showToast("Topic removed from saved discussions!")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getUserName(): String {
        return sessionManager.getUserName() ?: "Guest"
    }

    private fun updateRepliesUI(newReplyText: String) {
        val replyView = layoutInflater.inflate(R.layout.item_reply, binding.repliesContainer, false)
        val replyUsername = replyView.findViewById<TextView>(R.id.replyUsername)
        val replyText = replyView.findViewById<TextView>(R.id.replyText)
        val likeCount = replyView.findViewById<TextView>(R.id.likeCount)

        replyUsername.text = getUserName()
        replyText.text = newReplyText
        likeCount.text = "0"

        binding.repliesContainer.addView(replyView)
    }


    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.replyInput.windowToken, 0)
    }
}
