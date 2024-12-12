package com.clerami.universe.ui.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clerami.universe.data.local.FavoritePost
import com.clerami.universe.databinding.ItemPostBinding
import com.clerami.universe.ui.topic.TopicDetailActivity


class SavedPostsAdapter(private val posts: List<FavoritePost>) : RecyclerView.Adapter<SavedPostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)

        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TopicDetailActivity::class.java).apply {
                putExtra("topicId", post.topicId)
                putExtra("title", post.title)
                putExtra("description", post.description)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: FavoritePost) {
            binding.savedTitle.text = post.title
            binding.savedDescription.text = post.description
        }
    }
}
