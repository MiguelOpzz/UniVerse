package com.clerami.universe.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.clerami.universe.R
import com.clerami.universe.databinding.FragmentProfileBinding
import com.clerami.universe.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

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
