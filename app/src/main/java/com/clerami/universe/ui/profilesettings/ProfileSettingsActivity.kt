package com.clerami.universe.ui.profilesettings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.clerami.universe.R
import com.clerami.universe.data.remote.response.UpdateUser
import com.clerami.universe.databinding.ActivityProfileSettingsBinding
import com.clerami.universe.ui.login.LoginActivity
import com.clerami.universe.ui.profile.ProfileFragment
import com.clerami.universe.utils.SessionManager
import com.clerami.universe.utils.compressAndResizeImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.UUID

class ProfileSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var profileSettingsViewModel: ProfileSettingsViewModel

    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    // ActivityResultLauncher for picking an image
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Display selected image in profileImageView
                Glide.with(this).load(it).into(binding.profileImageView)
                // Compress and upload the image
                compressAndUploadImage(it)
            } ?: run {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Session Manager and ViewModel
        sessionManager = SessionManager(this)
        profileSettingsViewModel = ViewModelProvider(this).get(ProfileSettingsViewModel::class.java)

        // Set initial values from session or intent extras
        val universityName = intent.getStringExtra("universityName")
        val profilePictureUrl = intent.getStringExtra("profilePictureUrl")
        if (universityName != null) {
            Log.d("Profs", universityName)
        }
        if (profilePictureUrl != null) {
            Log.d("Profs", profilePictureUrl)
        }

        binding.editUniversity.setText(universityName)
        if (!profilePictureUrl.isNullOrEmpty()) {
            Glide.with(this).load(profilePictureUrl).into(binding.profileImageView)
        }

        // Set up "Change Picture" button to pick an image
        binding.changePictureButton.setOnClickListener {
            openGallery()
        }

        binding.saveChangesButton.isEnabled = false
        // Handle sign out
        binding.SignOut.setOnClickListener {
            signOut()
        }

        // Save profile changes
        binding.saveChangesButton.setOnClickListener {
            val universityName = binding.editUniversity.text.toString()
            val username = sessionManager.getUserName()
            // Get the token from SessionManager
            val token = sessionManager.getUserToken() // Assuming this method exists
            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "User token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the university name is empty
            if (universityName.isBlank()) {
                Toast.makeText(this, "University name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the new profile picture URL
            val newProfilePictureUrl = binding.profileImageView.tag as? String ?: ""

            // Prepare the UpdateUser object
            val updateUser = UpdateUser(universityName, newProfilePictureUrl)

            // Call the ViewModel to update the profile
            if (username != null) {
                profileSettingsViewModel.updateProfile(token, username , updateUser)
                Toast.makeText(this, "Updating User Profile", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }

    // Open the gallery to pick an image
    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }

    // Upload compressed image to Firebase Storage
    private fun compressAndUploadImage(uri: Uri) {
        compressAndResizeImage(uri, contentResolver)?.let { compressedBitmap ->
            val imageRef = storageRef.child("profilePictures/${UUID.randomUUID()}.jpg")

            val byteArrayOutputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()

            imageRef.putBytes(imageData)
                .addOnSuccessListener {
                    // Image uploaded successfully
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Save the download URL in the profile image view's tag
                        binding.profileImageView.tag = downloadUri.toString()
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        binding.saveChangesButton.isEnabled=true
                    }
                }
                .addOnFailureListener {
                    // Failed to upload image
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Error compressing image", Toast.LENGTH_SHORT).show()
        }
    }

    // Sign out and clear session
    private fun signOut() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
