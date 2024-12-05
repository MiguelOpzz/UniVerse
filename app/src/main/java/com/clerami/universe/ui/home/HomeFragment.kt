package com.clerami.universe.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.clerami.universe.ui.topicdetail.TopicDetailActivity
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
        topicBinding.likesCount.text = getString(R.string.loading_likes)
        topicBinding.commentsCount.text = getString(R.string.loading_replies)

        // Fetch comments and likes
        fetchCommentsForTopic(topic.topicId, topicBinding.commentsCount, topicBinding.likesCount)

        // Set click listener to open TopicDetailActivity
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


    private fun fetchCommentsForTopic(
        topicId: String,
        commentsCountTextView: TextView,
        likesCountTextView: TextView
    ) {
        ApiConfig.getApiService(requireContext()).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val comments = response.body()
                    if (comments != null && comments.isNotEmpty()) {
                        val likesCount = comments.sumOf { it.upvotes }
                        commentsCountTextView.text = getString(R.string.replies, comments.size)
                        likesCountTextView.text = getString(R.string.likes, likesCount)
                    } else {
                        commentsCountTextView.text = getString(R.string.no_replies_yet)
                        likesCountTextView.text = getString(R.string.no_likes_yet)
                    }
                } else {
                    commentsCountTextView.text = getString(R.string.error_loading_replies)
                    likesCountTextView.text = getString(R.string.error_loading_likes)
                    Log.e("HomeFragment", "Error fetching comments: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                commentsCountTextView.text = getString(R.string.failed_to_load_replies)
                likesCountTextView.text = getString(R.string.failed_to_load_likes)
                Log.e("HomeFragment", "Failed to fetch comments: ${t.localizedMessage}", t)
            }
        })
    }
}

