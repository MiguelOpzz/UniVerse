package com.clerami.universe.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.databinding.DynamicTopicBinding
import com.clerami.universe.databinding.FragmentDashboardBinding
import com.clerami.universe.ui.topic.TopicDetailActivity

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dashboardViewModel.fetchTopics(requireContext())

        dashboardViewModel.filteredTopics.observe(viewLifecycleOwner) { topics ->
            displayTopics(topics)
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dashboardViewModel.filterTopics(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return root
    }

    private fun displayTopics(topics: List<Topic>) {
        binding.dynamicTopicsContainer.removeAllViews()
        if (topics.isNotEmpty()) {
            topics.forEach { topic ->
                val discussionView = createDiscussionView(topic, layoutInflater)
                binding.dynamicTopicsContainer.addView(discussionView)
            }
        } else {
            Log.d("DashboardFragment", "No topics found to display.")
        }
    }

    private fun createDiscussionView(topic: Topic, inflater: LayoutInflater): View {
        val topicBinding = DynamicTopicBinding.inflate(inflater, binding.dynamicTopicsContainer, false)

        topicBinding.discussionTitle.text = topic.title
        topicBinding.discussionSubtitle.text = topic.description ?: "No description available"

        topicBinding.likesCount.visibility = View.GONE
        topicBinding.commentsCount.visibility = View.GONE

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
