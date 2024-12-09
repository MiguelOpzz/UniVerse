package com.clerami.universe.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.clerami.universe.R
import com.clerami.universe.ui.settings.SettingsActivity
import com.clerami.universe.databinding.FragmentProfileBinding
import com.clerami.universe.ui.profilesettings.ProfileSettingsActivity
import com.clerami.universe.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())

        // Set username
        binding.userName.text = sessionManager.getUserName()

        // Profile picture setup
        // Add code to load the image if needed
        fetchPostCount()
        fetchRepliesCount()
        // Handle to Settings button click
        binding.toSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.UserProfile.setOnClickListener{
            val intent = Intent(requireContext(), ProfileSettingsActivity::class.java)
            startActivity(intent)
        }

        // Handle saved posts
        val savedPosts = getSavedPosts()
        if (savedPosts.isEmpty()) {
            binding.savedPostsRecyclerView.visibility = View.GONE
        } else {
            binding.savedPostsRecyclerView.visibility = View.VISIBLE
            binding.savedPostsRecyclerView.adapter = SavedPostsAdapter(savedPosts)
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

                        binding.repliesNumber.text = getString(R.string.replies_count_format, commentCount)
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

    private fun fetchVotesCount() {
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

                        binding.repliesNumber.text = getString(R.string.replies_count_format, commentCount)
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
        val sharedPreferences = requireContext().getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.all.filter { it.key.startsWith("isFavorite_") && it.value == true }
            .map { it.key.removePrefix("isFavorite_") }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
