package com.clerami.universe.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.clerami.universe.R
import com.clerami.universe.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    // Declare the binding variable
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!! // Non-nullable reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example: Update the userName TextView dynamically
        val loggedInUserName = getLoggedInUserName()
        binding.userName.text = loggedInUserName

        binding.toSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.UserProfile.setOnClickListener{
            val intent = Intent(requireContext(),ProfileSettingsActivity::class.java)
            startActivity(intent)
        }

    }

    // Mock method to get the logged-in user's name
    private fun getLoggedInUserName(): String {
        // Replace with your actual logic to fetch the user's name
        return "John Doe"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding reference
        _binding = null
    }
}
