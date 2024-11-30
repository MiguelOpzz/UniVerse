package com.clerami.universe.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.retrofit.Comment
import com.clerami.universe.data.remote.retrofit.Topic
import com.clerami.universe.databinding.DynamicTopicBinding
import com.clerami.universe.databinding.FragmentHomeBinding
import com.clerami.universe.ui.addnewdicussion.AddNewActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel.fetchTopics()

        homeViewModel.topics.observe(viewLifecycleOwner) { topics ->
            binding.dynamicTopicsContainer.removeAllViews()
            if (topics.isNotEmpty()) {
                topics.forEach { topic ->
                    val discussionView = createDiscussionView(topic, inflater)
                    binding.dynamicTopicsContainer.addView(discussionView)
                }
            }
        }

        binding.btnAddDiscussion.setOnClickListener {
            val intent = Intent(requireContext(), AddNewActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun createDiscussionView(topic: Topic, inflater: LayoutInflater): View {
        val topicBinding =
            DynamicTopicBinding.inflate(inflater, binding.dynamicTopicsContainer, false)

        topicBinding.discussionTitle.text = topic.title
        topicBinding.discussionSubtitle.text = topic.description

        topicBinding.likesCount.text = "Loading likes..."
        topicBinding.commentsCount.text = "Loading replies..."

        fetchCommentsForTopic(topic.topicId, topicBinding.commentsCount, topicBinding.likesCount)

        return topicBinding.root
    }

    private fun fetchCommentsForTopic(
        topicId: String,
        commentsCountTextView: TextView,
        likesCountTextView: TextView
    ) {
        ApiConfig.getApiService().getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    // handle response
                } else {
                    // handle error
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                // handle failure
            }
        })
    }
}