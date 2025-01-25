package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.User
import com.tasty.recipes.databinding.ItemUserBinding

class UsersAdapter (private var users: List<User> = emptyList(),
                     private val onClickListener: (User) -> Unit): RecyclerView.Adapter<UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        )
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(users[position], onClickListener)
    }

    fun updateUsers(list: List<User>) {
        users = list
        notifyDataSetChanged()
    }
}

class UsersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemUserBinding = ItemUserBinding.bind(view)

    fun bind(user: User, onClickListener: (User) -> Unit) {

        itemUserBinding.userName.text = user.username
        Picasso.get().load(user.photoUrl).into(itemUserBinding.userProfileImage)

        itemView.setOnClickListener {
            onClickListener(user)
        }

    }
}