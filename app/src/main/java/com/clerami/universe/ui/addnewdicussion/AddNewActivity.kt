package com.clerami.universe.ui.addnewdicussion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.data.remote.response.CreateTopicResponse
import com.clerami.universe.data.remote.response.CreateTopicsRequest
import com.clerami.universe.data.remote.response.CreateTopicsResponse
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.databinding.ActivityAddNewBinding
import com.clerami.universe.utils.SessionManager
import com.google.gson.Gson
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewBinding
    private val addNewViewModel: AddNewViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var imageCount = 0
    private val attachmentUrls = mutableListOf<String>() // Store URLs for uploaded images
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

            // Format date to ISO 8601
            val formattedDate = Instant.now().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT)

            val request = CreateTopicsRequest(
                title = title,
                description = description,

            )

            Log.d("AddNewActivity", "Request Payload: ${Gson().toJson(request)}")
            Log.d("AuthorizationHeader", "Bearer $token")

            val apiService = ApiConfig.getApiService(this)
            addNewViewModel.createTopics(apiService, token, request)
        }
    }

    private fun observeViewModel() {
        addNewViewModel.responseLiveData.observe(this) { response ->
            handleSuccess(response)
        }

        addNewViewModel.errorMessage.observe(this) { error ->
            Log.e("AddNewActivity", "Error: $error")
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSuccess(response: CreateTopicsResponse) {
        Toast.makeText(this, "New discussion created: ${response.message}", Toast.LENGTH_SHORT).show()
        finish()
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
