package com.clerami.universe.ui.addnewdicussion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.R

class AddNewActivity : AppCompatActivity() {

    private lateinit var contentInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new)

        val closeButton = findViewById<ImageView>(R.id.closeButton)
        val titleInput = findViewById<EditText>(R.id.titleInput)
        contentInput = findViewById(R.id.contentInput)
        val tagsInput = findViewById<EditText>(R.id.tagsInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val imageUploadIcon = findViewById<ImageView>(R.id.imageUploadIcon)

        // Close button: Return to the homepage
        closeButton.setOnClickListener { finish() }

        // Image upload: Open the gallery
        imageUploadIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        sendButton.setOnClickListener {
            val title = titleInput.text.toString()
            val content = contentInput.text.toString()
            val tags = tagsInput.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                Toast.makeText(this, "New discussion created!", Toast.LENGTH_SHORT).show()
                // Add logic for sending the post data
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the result of image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                contentInput.append("\n[Image: $selectedImageUri]\n")
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
