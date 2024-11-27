package com.clerami.universe.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.clerami.universe.R
import com.clerami.universe.ui.addnewdicussion.AddNewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Views
        val searchButton = rootView.findViewById<ImageButton>(R.id.btnSearch)
        val searchEditText = rootView.findViewById<EditText>(R.id.searchEditText)
        val trendingButton = rootView.findViewById<Button>(R.id.btnTrending)
        val newButton = rootView.findViewById<Button>(R.id.btnNew)
        val trendingNewContainer = rootView.findViewById<LinearLayout>(R.id.trendingNewContainer)
        val addDiscussionButton = rootView.findViewById<FloatingActionButton>(R.id.btnAddDiscussion)

        // Observe isSearchActive state
        homeViewModel.isSearchActive.observe(viewLifecycleOwner, Observer { isSearchActive ->
            if (isSearchActive) {
                searchEditText.visibility = View.VISIBLE
                trendingNewContainer.visibility = View.GONE
            } else {
                searchEditText.visibility = View.GONE
                trendingNewContainer.visibility = View.VISIBLE
            }
        })

        // Search Button Click Listener
        searchButton.setOnClickListener {
            val currentState = homeViewModel.isSearchActive.value ?: false
            homeViewModel.toggleSearchBar(!currentState)

            // Hide Trending and New buttons when search is active
            if (!currentState) {
                trendingButton.visibility = View.GONE
                newButton.visibility = View.GONE
            } else {
                trendingButton.visibility = View.VISIBLE
                newButton.visibility = View.VISIBLE
            }
        }

        // FloatingActionButton Click Listener to navigate to AddNewActivity
        addDiscussionButton.setOnClickListener {
            val intent = Intent(requireContext(), AddNewActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }
}
