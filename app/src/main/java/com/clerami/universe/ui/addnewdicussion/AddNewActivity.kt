package com.clerami.universe.ui.addnewdicussion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.CreateTopicRequest
import com.clerami.universe.databinding.ActivityAddNewBinding
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewBinding
    private val addNewViewModel: AddNewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            if (title.isNotEmpty() && content.isNotEmpty()) {
                val request = CreateTopicRequest(title, content, createdBy, "Computer Science", tags)

                addNewViewModel.createNewTopic(request)

                Toast.makeText(this, "New discussion created!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                val base64Image = convertImageToBase64(it)
                binding.contentInput.append("\n[Image: $base64Image]\n")
            }
        }
    }

    private fun convertImageToBase64(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }

        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
