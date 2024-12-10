package com.clerami.universe.ui.editTopic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.clerami.universe.data.remote.response.UpdateTopicRequest
import com.clerami.universe.databinding.ActivityEditTopicBinding
import com.clerami.universe.ui.home.HomeViewModel
import com.clerami.universe.utils.SessionManager
import com.clerami.universe.viewmodel.TopicViewModel

class EditTopicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTopicBinding
    private val PICK_IMAGE_REQUEST = 1
    private var currentImageUri: Uri? = null  // Store the current image URI if available
    private val viewModel: TopicViewModel by viewModels()  // ViewModel to handle the API request
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditTopicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Initialize session manager and retrieve token
        sessionManager = SessionManager(this)
        val token = sessionManager.getUserToken()

        // Retrieve data passed from previous activity
        val topicId = intent.getStringExtra("topicId") ?: return
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val attachmentUrls: ArrayList<String>? = intent.getStringArrayListExtra("attachmentsUrls")
        val tags: ArrayList<String>? = intent.getStringArrayListExtra("tags")
        val tagsList = tags ?: emptyList()

        populateTags(tagsList)

        binding.editTitle.setText(title)
        binding.editDescription.setText(description)

        // Image selection for update
        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        viewModel.updateState.observe(this) { state ->
            when (state) {
                is TopicViewModel.UpdateState.Loading -> {
                    // Show loading spinner
                    binding.loading.visibility = View.VISIBLE
                    binding.saveButton.isEnabled = false
                }
                is TopicViewModel.UpdateState.Success -> {
                    // Hide loading spinner and enable button
                    binding.loading.visibility = View.GONE
                    binding.saveButton.isEnabled = true
                    Toast.makeText(this, "Topic updated successfully", Toast.LENGTH_SHORT).show()
                    finish()  // Optionally finish the activity and return
                }
                is TopicViewModel.UpdateState.Error -> {
                    // Hide loading spinner and enable button
                    binding.loading.visibility = View.GONE
                    binding.saveButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Save Button - Handle the save logic
        binding.saveButton.setOnClickListener {
            val updatedTitle = binding.editTitle.text.toString()
            val updatedDescription = binding.editDescription.text.toString()
            val updatedTags = binding.editTags.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // Check for required fields (optional validation)
            if (updatedTitle.isBlank() || updatedDescription.isBlank()) {
                Toast.makeText(this, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create UpdateTopicRequest
            val updateRequest = UpdateTopicRequest(
                title = updatedTitle,
                description = updatedDescription,
                tags = updatedTags,
                attachmentUrls = attachmentUrls ?: emptyList()  // Use the attachments as they are (list of strings)
            )

            // Call ViewModel to update topic
            if (topicId.isNotEmpty()) {
                viewModel.updateTopic("Bearer $token", topicId, updateRequest)

                // Send a broadcast to refresh the topic
                val intent = Intent("com.clerami.universe.ACTION_REFRESH_TOPIC")
                intent.putExtra("topicId", topicId)
                sendBroadcast(intent)

            } else {
                Toast.makeText(this, "Invalid topic ID", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the update state from the ViewModel

    }

    // Handle image selection from the gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            val imageUri = data?.data
            binding.imageView.setImageURI(imageUri)  // Display the selected image
            currentImageUri = imageUri // Save the selected image URI
        }
    }


    private fun populateTags(tags: List<String>) {
        binding.editTags.setText(tags.joinToString(", "))
    }


    private fun showLoading() {
        binding.loading.visibility = android.view.View.VISIBLE
    }


    private fun hideLoading() {
        binding.loading.visibility = android.view.View.GONE
    }
}
