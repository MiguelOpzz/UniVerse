package com.clerami.universe.ui.profile

import TopicDetailViewModel
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.clerami.universe.R
import com.clerami.universe.ui.settings.SettingsActivity
import com.clerami.universe.databinding.FragmentProfileBinding
import com.clerami.universe.ui.profilesettings.ProfileSettingsActivity
import com.clerami.universe.ui.topic.TopicDetailViewModelFactory
import com.clerami.universe.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    private var universityName: String? = null
    private var profilePictureUrl: String? = null
    private val viewModel: ProfileViewModel by viewModels { ProfileViewModelFactory(requireActivity().application) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())



        binding.userName.text = sessionManager.getUserName()
        fetchUniversity()
        fetchProfilePicture()

        fetchPostCount()

        fetchRepliesCount()
        // Handle to Settings button click
        binding.toSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.UserProfile.setOnClickListener {
            // Check if both university and profile picture data are available
            val intent = Intent(requireContext(), ProfileSettingsActivity::class.java)
            intent.putExtra("universityName", universityName)
            intent.putExtra("profilePictureUrl", profilePictureUrl)
            profileSettingsLauncher.launch(intent)



        }


        // Handle saved posts
        lifecycleScope.launch {
            viewModel.favoritePosts.collectLatest { posts ->
                if (posts.isEmpty()) {
                    binding.savedPostsRecyclerView.visibility = View.GONE
                } else {
                    binding.savedPostsRecyclerView.visibility = View.VISIBLE
                    val adapter = SavedPostsAdapter(posts)
                    binding.savedPostsRecyclerView.adapter = adapter
                    binding.savedPostsRecyclerView.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                }
            }
        }
        return binding.root
    }



    private fun fetchPostCount() {
        binding.postsNumber.text = getString(R.string.loading)
        val username = sessionManager.getUserName()
        if (username != null) {

            val userRef = db.collection("users")
                .whereEqualTo("username", username)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {

                        val document = documents.documents.first()
                        val topicCount = document.getLong("topicCount") ?: 0

                        binding.postsNumber.text = getString(R.string.post_count_format, topicCount)
                    } else {

                        binding.postsNumber.text = getString(R.string.post_count_format, 0)
                    }
                } else {

                    binding.postsNumber.text = getString(R.string.post_count_format, 0)
                }
            }
        } else {
            // Handle case where the user is not logged in or has no username
            binding.postsNumber.text = getString(R.string.post_count_format, 0)
        }
    }



    private fun fetchRepliesCount() {
        binding.postsNumber.text = getString(R.string.loading)
        val username = sessionManager.getUserName()
        if (username != null) {

            val userRef = db.collection("users")
                .whereEqualTo("username", username)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {

                        val document = documents.documents.first()
                        val commentCount = document.getLong("commentCount") ?: 0

                        binding.repliesNumber.text =
                            getString(R.string.replies_count_format, commentCount)
                    } else {

                        binding.repliesNumber.text = getString(R.string.replies_count_format, 0)
                    }
                } else {

                    binding.repliesNumber.text = getString(R.string.replies_count_format, 0)
                }
            }
        } else {
            // Handle case where the user is not logged in or has no username
            binding.repliesNumber.text = getString(R.string.replies_count_format, 0)
        }
    }


    private fun getSavedPosts(): List<String> {
        val sharedPreferences =
            requireContext().getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.all.filter { it.key.startsWith("isFavorite_") && it.value == true }
            .map { it.key.removePrefix("isFavorite_") }
    }



    override fun onResume() {
        super.onResume()
        fetchUniversity()
        fetchProfilePicture()
    }


    private val profileSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedUniversityName = result.data?.getStringExtra("universityName")
                val updatedProfilePictureUrl = result.data?.getStringExtra("profilePictureUrl")

                // Update the UI directly with the updated values
                if (updatedUniversityName != null) {
                    universityName = updatedUniversityName
                    binding.university.text = updatedUniversityName
                }
                if (updatedProfilePictureUrl != null) {
                    profilePictureUrl = updatedProfilePictureUrl
                    Glide.with(this)
                        .load(updatedProfilePictureUrl)
                        .placeholder(R.drawable.baseline_person_24)
                        .into(binding.profilePic)
                }

                // Call the fetch methods again to ensure the data is reloaded
                fetchUniversity()
                fetchProfilePicture()
                fetchPostCount()
                fetchRepliesCount()
            }
        }


    private fun fetchUniversity() {
        binding.university.text = getString(R.string.loading) // Show loading initially
        val username = sessionManager.getUserName()

        if (username != null) {
            val userRef = db.collection("users").document(username)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        universityName = document.getString("university") // Store fetched value
                        // Update UI with the fetched value
                        binding.university.text = universityName ?: getString(R.string.no_university)
                        Log.d("ProfileFragment", "Fetched university: $universityName")
                    } else {
                        binding.university.text = getString(R.string.no_university)
                        Log.d("ProfileFragment", "No university found for the user.")
                    }
                } else {
                    binding.university.text = getString(R.string.no_university)
                    Log.d("ProfileFragment", "Error fetching university.")
                }
            }
        } else {
            binding.university.text = getString(R.string.no_university)
        }
    }



    private fun fetchProfilePicture() {
        Glide.with(this)
            .load(R.drawable.baseline_person_24) // Show placeholder
            .into(binding.profilePic)

        val username = sessionManager.getUserName()

        if (username != null) {
            val userRef = db.collection("users").document(username)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        profilePictureUrl = document.getString("profilePicture") // Store fetched URL
                        Log.d("ProfileFragment", "Fetched profile picture URL: $profilePictureUrl")
                        if (profilePictureUrl != null) {
                            Glide.with(this)
                                .load(profilePictureUrl) // Load the image
                                .placeholder(R.drawable.baseline_person_24)
                                .into(binding.profilePic)
                        }
                    } else {
                        Log.d("ProfileFragment", "No profile picture found for the user.")
                    }
                } else {
                    Log.d("ProfileFragment", "Error fetching profile picture.")
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

