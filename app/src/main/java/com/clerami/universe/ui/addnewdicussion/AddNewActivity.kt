package com.clerami.universe.ui.addnewdicussion

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.data.remote.response.CreateTopicRequest
import com.clerami.universe.databinding.ActivityAddNewBinding

class AddNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewBinding
    private val addNewViewModel: AddNewViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)

        binding.closeButton.setOnClickListener { finish() }

        binding.imageUploadIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        binding.sendButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val content = binding.contentInput.text.toString()
            val tags = binding.tagsInput.text.toString().split(",").map { it.trim() }
            val createdBy = "User123"
            val token = getAuthToken()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val request = CreateTopicRequest(title, content, createdBy, "Computer Science", tags)

                addNewViewModel.createNewTopic(request, this)

                Toast.makeText(this, "New discussion created!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var imageCount = 0

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
                    else -> {
                        binding.extraImagesText.visibility = TextView.VISIBLE
                    }
                }
            }
        }
    }

    private fun getAuthToken(): String {
        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}