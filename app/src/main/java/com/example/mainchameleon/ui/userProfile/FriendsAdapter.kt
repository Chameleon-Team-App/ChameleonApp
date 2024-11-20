package com.example.mainchameleon.ui.userProfile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.ItemFriendBinding
import com.squareup.picasso.Picasso

class FriendsAdapter(private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private var friends = listOf<Friend>()

    inner class FriendViewHolder(private val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.friendName.text = friend.username
            if (friend.profilePictureUrl.isNotEmpty()) {
                Picasso.get()
                    .load(friend.profilePictureUrl)
                    .placeholder(R.drawable.default_profile)
                    .into(binding.friendImage)
            } else {
                binding.friendImage.setImageResource(R.drawable.default_profile)
            }
            binding.root.setOnClickListener { onClick(friend.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int = friends.size

    fun submitList(friends: List<Friend>) {
        this.friends = friends
        notifyDataSetChanged()
    }
}
