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
import com.clerami.universe.utils.SessionManager

class ProfileFragment : Fragment() {


    // Declare the binding variable
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

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
        sessionManager = SessionManager(requireContext())

        binding.toSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.UserProfile.setOnClickListener{
            val intent = Intent(requireContext(),ProfileSettingsActivity::class.java)
            startActivity(intent)
        }




        binding.userName.text = getLoggedInUserName()
    }

    private fun getLoggedInUserName(): String {

        return sessionManager.getUserName() ?:"Guest"
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
