package com.clerami.universe.ui.addnewdicussion

import AddNewViewModelFactory
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.CreateTopicResponse
import com.clerami.universe.databinding.ActivityAddNewBinding
import com.clerami.universe.utils.SessionManager
import com.google.gson.Gson


class AddNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewBinding
    private val addNewViewModel: AddNewViewModel by viewModels { AddNewViewModelFactory(application) }
    private lateinit var sessionManager: SessionManager

    private var imageCount = 0
    private val attachmentUrls = mutableListOf<String>()
    private val maxImages = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupListeners()
        observeViewModel()

        Log.d("AddNewActivity", "Token: ${sessionManager.getUserToken()}")
    }

    private fun setupListeners() {
        binding.closeButton.setOnClickListener { finish() }

        binding.imageUploadIcon.setOnClickListener {
            if (imageCount < maxImages) {
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                startActivityForResult(intent, IMAGE_PICK_CODE)
            } else {
                Toast.makeText(this, "You can only upload up to $maxImages images.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.sendButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val tags = binding.tagsInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Title and description cannot be empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val token = sessionManager.getUserToken()

            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "Authentication token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            binding.loading.visibility = View.VISIBLE

            val request = CreateTopicRequest(
                title = title,
                description = description,
                tags = tags,
                attachmentUrls = attachmentUrls
                // Include additional fields if necessary
            )

            addNewViewModel.createTopic(token, request)

        }

    }

    private fun observeViewModel() {
        addNewViewModel.responseLiveData.observe(this) { response ->
            handleSuccess(response)
        }

        addNewViewModel.errorMessage.observe(this) { errorResponse ->
            handleError(errorResponse) // Now, it's a CreateTopicResponse
        }

        addNewViewModel.isLoading.observe(this) { isLoading ->
            // Handle loading state if necessary
            if (isLoading) {
                binding.loading.visibility = View.VISIBLE // Show progress bar
            } else {
                binding.loading.visibility = View.GONE // Hide progress bar when done
            }
        }
    }


    private fun handleSuccess(response: CreateTopicResponse) {
        Toast.makeText(this, "New discussion created: ${response.message}", Toast.LENGTH_SHORT).show()
        binding.loading.visibility = View.GONE // Hide the progress bar after success
        val intent = Intent("com.clerami.universe.ACTION_REFRESH_TOPICS")
        sendBroadcast(intent)
        finish()
    }


    private fun handleError(errorResponse: CreateTopicResponse) {
        // Extract the error message from the response or fallback to a generic message
        val errorMessage = errorResponse.message ?: "Unknown error"
        val errorReason = errorResponse.reason ?: "No reason provided"

        // Log the error details (for debugging purposes)
        Log.e("AddNewActivity", "Error status: ${errorResponse.status}, Reason: $errorReason")

        // Create and show an AlertDialog with the error message and reason
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("Message: $errorMessage\nReason: $errorReason")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog on click
            }
            .setCancelable(false) // Prevent closing the dialog by tapping outside
            .show()

        // Hide the progress bar
        binding.loading.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                imageCount++
                when (imageCount) {
                    1 -> {
                        binding.imagePreview1.setImageURI(it)
                        binding.imagePreview1.visibility = ImageView.VISIBLE
                    }
                    2 -> {
                        binding.imagePreview2.setImageURI(it)
                        binding.imagePreview2.visibility = ImageView.VISIBLE
                    }
                }
                attachmentUrls.add(it.toString())
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}

