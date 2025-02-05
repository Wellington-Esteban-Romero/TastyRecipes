package com.tasty.recipes.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.User
import com.tasty.recipes.databinding.ItemListUserBinding
import com.tasty.recipes.databinding.ItemUserBinding

class ListUserAdapter (private var users: List<User> = emptyList(),
                    private val onClickListener: (User) -> Unit): RecyclerView.Adapter<ListUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListUserViewHolder {
        return ListUserViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_user, parent, false)
        )
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ListUserViewHolder, position: Int) {
        holder.bind(users[position], onClickListener)
    }

    fun updateUsers(list: List<User>) {
        users = list
        notifyDataSetChanged()
    }
}

class ListUserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemListUserBinding = ItemListUserBinding.bind(view)

    fun bind(user: User, onClickListener: (User) -> Unit) {

        itemListUserBinding.tvNameUser.text = user.username
        Picasso.get().load(user.photoUrl).into(itemListUserBinding.userProfileImage)

        itemView.setOnClickListener {
            onClickListener(user)
        }

    }
}