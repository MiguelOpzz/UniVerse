package com.clerami.universe.ui.profilesettings

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.R
import com.clerami.universe.databinding.ActivityProfileSettingsBinding
import com.clerami.universe.ui.login.LoginActivity
import com.clerami.universe.utils.SessionManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.UUID
import android.graphics.BitmapFactory
import com.bumptech.glide.Glide


class ProfileSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var sessionManager: SessionManager

    private var imageUri: Uri? = null
    private val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://myproject-441712.firebasestorage.app")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Set initial values from session
        binding.editUsername.setText(sessionManager.getUserName())
        binding.editUniversity.setText(sessionManager.getUserUniversity())

        val profileImageUrl = sessionManager.getProfileImageUrl()
        if (!profileImageUrl.isNullOrEmpty()) {
            // Use a library like Glide or Picasso to load the image from the URL
            Glide.with(this)
                .load(profileImageUrl)
                .into(binding.profileImageView)
        }

        // Change profile picture
        binding.changePictureButton.setOnClickListener {
            openImagePicker()
        }

        // Save changes
        binding.saveChangesButton.setOnClickListener {
            saveChanges()
        }

        // Sign out
        binding.SignOut.setOnClickListener {
            signOut()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*" // Pick image from gallery
        }
        imagePickerActivityResult.launch(intent)
    }

    private val imagePickerActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            imageUri?.let {
                binding.profileImageView.setImageURI(it)

                // Now, upload the selected profile picture
                uploadProfilePicture(it)
            }
        }
    }

    private fun saveChanges() {
        val newUsername = binding.editUsername.text.toString()
        val newUniversity = binding.editUniversity.text.toString()

        if (newUsername.isNotBlank() && newUniversity.isNotBlank()) {
            // Save locally first
            sessionManager.saveUserName(newUsername)
            sessionManager.saveUserUniversity(newUniversity)

            // Now make the API call to update the backend
            updateUsernameOnBackend(newUsername, newUniversity)
            Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show()
        } else {
            // Show an error message if fields are empty
            Toast.makeText(this, "Username and University cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateUsernameOnBackend(newUsername: String, newUniversity: String) {
        // Assuming you have a ViewModel or a NetworkClient to handle API requests
        val token = sessionManager.getUserToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token is missing, please log in again", Toast.LENGTH_SHORT).show()
            return
        }
    }


    private fun uploadProfilePicture(uri: Uri) {
        val imageRef = storageRef.child("profile_pictures/${UUID.randomUUID()}.jpg")

        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmap = getBitmapFromUri(uri)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

        val imageData = byteArrayOutputStream.toByteArray()

        imageRef.putBytes(imageData)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    sessionManager.saveProfileImageUrl(imageUrl)
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileSettings", "Error uploading image", exception)
                Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun signOut() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
