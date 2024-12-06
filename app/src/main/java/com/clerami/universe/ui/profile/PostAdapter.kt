package com.clerami.universe.ui.profile


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clerami.universe.databinding.ItemPostBinding


data class Post(
    val userName: String,
    val userPictureUrl: String,
    val description: String
)

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {

        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]


        holder.binding.userName.text = post.userName
        holder.binding.description.text = post.description


        Glide.with(holder.binding.root.context)
            .load(post.userPictureUrl)
            .circleCrop()
            .into(holder.binding.userPicture)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)
}
