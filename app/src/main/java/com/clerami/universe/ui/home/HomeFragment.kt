package com.clerami.universe.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.databinding.DynamicTopicBinding
import com.clerami.universe.databinding.FragmentHomeBinding
import com.clerami.universe.ui.addnewdicussion.AddNewActivity
import com.clerami.universe.ui.topic.TopicDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()

    private val handler = android.os.Handler(Looper.getMainLooper())
    private val debounceRunnable = Runnable {
        val query = binding.searchEditText.text.toString().lowercase()
        homeViewModel.filterTopics(query)
    }

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

        // Implement search functionality with debounce
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Trigger search after typing
                handler.removeCallbacks(debounceRunnable) // Remove any pending actions
                handler.postDelayed(debounceRunnable, 500) // Wait for 500ms before triggering
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for filtering
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for filtering, but required to override
            }
        })

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

        // Hide likes and comments count initially
        topicBinding.likesCount.visibility = View.GONE
        topicBinding.commentsCount.visibility = View.GONE

        // Fetch and set the comments and likes count dynamically
        fetchCommentsForTopic(
            requireContext(),
            topic.topicId,
            topicBinding.commentsCount,
            topicBinding.likesCount
        )

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
        context: Context,
        topicId: String,
        commentsCountTextView: TextView,
        likesCountTextView: TextView
    ) {
        ApiConfig.getApiService(context).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    val comments = response.body()
                    if (comments != null && comments.isNotEmpty()) {
                        val likesCount = comments.sumOf { it.upvotes }
                        commentsCountTextView.text = context.getString(R.string.replies, comments.size)
                        likesCountTextView.text = context.getString(R.string.likes, likesCount)
                    } else {
                        commentsCountTextView.text = context.getString(R.string.no_replies_yet)
                        likesCountTextView.text = context.getString(R.string.no_likes_yet)
                    }
                    // Make them visible after data is fetched
                    commentsCountTextView.visibility = View.VISIBLE
                    likesCountTextView.visibility = View.VISIBLE
                } else {
                    commentsCountTextView.text = context.getString(R.string.error_loading_replies)
                    likesCountTextView.text = context.getString(R.string.error_loading_likes)
                    Log.e("HomeFragment", "Error fetching comments: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                commentsCountTextView.text = context.getString(R.string.error_loading_replies)
                likesCountTextView.text = context.getString(R.string.error_loading_likes)
                Log.e("HomeFragment", "Failed to fetch comments", t)
            }
        })
    }
}
