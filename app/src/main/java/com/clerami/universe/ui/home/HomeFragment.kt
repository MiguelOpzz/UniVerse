package com.clerami.universe.ui.home


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

    private var selectedTag: String? = null

    private val handler = android.os.Handler(Looper.getMainLooper())
    private var isLoading = false // To prevent multiple fetches
    private var nextCursor: String? = null
    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            homeViewModel.fetchTopics(requireContext())
        }
    }
    private var isReceiverRegistered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel.fetchTopics(requireContext())

        homeViewModel.topics.observe(viewLifecycleOwner) { topics ->




            // Clear the container before adding new topics
            binding.dynamicTopicsContainer.removeAllViews()

            // If there are topics to display, create views for each topic
            if (topics.isNotEmpty()) {
                topics.forEach { topic ->
                    val discussionView = createDiscussionView(topic, layoutInflater)
                    binding.dynamicTopicsContainer.addView(discussionView)
                }
            } else {
                Log.d("HomeFragment", "No topics found to display.")
            }

            // Hide the loading indicator if data is loaded
            isLoading = false
        }

        // Scroll detection logic for lazy loading
        binding.dynamicTopicsContainer.viewTreeObserver.addOnScrollChangedListener {
            val container = binding.dynamicTopicsContainer
            val lastVisibleItem = container.getChildAt(container.childCount - 1)

            // Check if we reached the bottom of the container
            if (lastVisibleItem != null && isViewVisible(container, lastVisibleItem)) {
                if (!isLoading && nextCursor != null) {
                    // Start fetching the next set of topics if not loading
                    isLoading = true
                    homeViewModel.fetchTopics(requireContext())
                }
            }
        }

        // Initial fetch
        homeViewModel.fetchTopics(requireContext())


        // Check if the view is visible in the container


        binding.btnAddDiscussion.setOnClickListener {
            val intent = Intent(requireContext(), AddNewActivity::class.java)
            startActivity(intent)
        }


        homeViewModel.tags.observe(viewLifecycleOwner) { tags ->
            // Clear the previous tags views
            binding.topicContainer.removeAllViews()

            // Create the new tag views
            val inflater = LayoutInflater.from(requireContext())
            tags.forEach { tag ->
                val tagView = TextView(requireContext()).apply {
                    text = tag
                    setPadding(32, 8, 32, 8)

                    background = if (tag == selectedTag) {
                        ContextCompat.getDrawable(requireContext(), R.drawable.selected_topic_bg)
                    } else {
                        ContextCompat.getDrawable(requireContext(), R.drawable.topic_background)
                    }

                    textSize = 14f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    minimumWidth = (100 * resources.displayMetrics.density).toInt()
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = 8
                    }

                    // When a tag is clicked, toggle it
                    setOnClickListener {
                        selectedTag = if (selectedTag == tag) {
                            null  // If the tag is already selected, deselect it
                        } else {
                            tag  // Otherwise, select this tag
                        }

                        updateTagBackgrounds()  // Update the tag backgrounds based on selection

                        // Call the ViewModel method to filter or clear filters
                        if (selectedTag != null) {

                            homeViewModel.filterTopicsByTag(selectedTag!!)
                        } else {

                            homeViewModel.clearFilters()  // Show all topics
                        }
                    }
                }
                binding.topicContainer.addView(tagView)
            }
        }


        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter("com.clerami.universe.ACTION_REFRESH_TOPICS")
            ContextCompat.registerReceiver(requireContext(), refreshReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
        return binding.root
    }

    private fun updateTagBackgrounds() {
        val tagViews = binding.topicContainer.children.toList()

        tagViews.forEach { tagView ->
            if (tagView is TextView) {
                val tag = tagView.text.toString()
                tagView.background = if (tag == selectedTag) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.selected_topic_bg)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.topic_background)
                }
            }
        }
    }
    private fun isViewVisible(container: ViewGroup, view: View): Boolean {
        val containerHeight = container.height
        val viewBottom = view.bottom
        return viewBottom >= containerHeight
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.fetchTopics(requireContext())
    }

    override fun onStop() {
        super.onStop()
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(refreshReceiver)
            isReceiverRegistered = false
        }
    }

    private fun isViewVisible(container: View, view: View): Boolean {
        val visibleBounds = Rect()
        container.getHitRect(visibleBounds)
        return view.getLocalVisibleRect(visibleBounds)
    }

    // Helper method to create the view for each topic

    private fun createDiscussionView(topic: Topic, inflater: LayoutInflater): View {
        // Inflate the layout without attaching to the parent directly
        val topicBinding = DynamicTopicBinding.inflate(inflater)

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

        return topicBinding.root  // Return the root view, not the whole binding
    }



}