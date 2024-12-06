package com.clerami.universe.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.clerami.universe.R
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.databinding.DynamicTopicBinding
import com.clerami.universe.databinding.FragmentHomeBinding
import com.clerami.universe.ui.addnewdicussion.AddNewActivity
import com.clerami.universe.ui.topicdetail.TopicDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Fetch topics
        homeViewModel.fetchTopics(requireContext())

        // Observe topics LiveData
        homeViewModel.topics.observe(viewLifecycleOwner) { topics ->
            Log.d("HomeFragment", "Observed topics: $topics")
            binding.dynamicTopicsContainer.removeAllViews()
            if (topics.isNotEmpty()) {
                topics.forEach { topic ->
                    val discussionView = createDiscussionView(topic, inflater)
                    binding.dynamicTopicsContainer.addView(discussionView)
                }
            } else {
                Log.d("HomeFragment", "No topics found to display.")
            }
        }

        binding.btnAddDiscussion.setOnClickListener {
            val intent = Intent(requireContext(), AddNewActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun createDiscussionView(topic: Topic, inflater: LayoutInflater): View {
        val topicBinding = DynamicTopicBinding.inflate(inflater, binding.dynamicTopicsContainer, false)

        topicBinding.discussionTitle.text = topic.title
        topicBinding.discussionSubtitle.text = topic.description ?: "No description available"
        topicBinding.likesCount.visibility = View.GONE
        topicBinding.commentsCount.visibility = View.GONE

        val isFavorite = isTopicFavorite(topic.topicId)
        updateHeartIcon(topicBinding.heartIcon, isFavorite)

        topicBinding.root.setOnClickListener {
            val intent = Intent(requireContext(), TopicDetailActivity::class.java).apply {
                putExtra("topicId", topic.topicId)
                putExtra("title", topic.title)
                putExtra("description", topic.description)
            }
            startActivity(intent)
        }

        return topicBinding.root
    }

    private fun isTopicFavorite(topicId: String): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isFavorite_$topicId", false)
    }
    private fun updateHeartIcon(heartIcon: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            heartIcon.setImageResource(R.drawable.fav)
        } else {
            heartIcon.setImageResource(R.drawable.fav_outline)
        }
    }
}


