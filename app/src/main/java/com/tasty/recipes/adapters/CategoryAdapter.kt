package com.tasty.recipes.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.databinding.ItemRecipeBinding

class CategoryAdapter (private var recipes: List<Category> = emptyList(),
                     private val onClickListener: (Category) -> Unit): RecyclerView.Adapter<CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        )
    }

    override fun getItemCount() = recipes.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(recipes[position], onClickListener)
    }

    fun updateRecipes (list: List<Category>) {
        recipes = list
        notifyDataSetChanged()
    }
}

class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemRecipeBinding = ItemRecipeBinding.bind(view)

    fun bind(category: Category, onClickListener: (Category) -> Unit) {

        itemRecipeBinding.recipeName.text = category.name

        itemView.setOnClickListener {
            onClickListener(category)
        }

       /* if (SessionManager(context).isFavorite(recipe.id.toString()))
            favoriteImageView.visibility = View.VISIBLE
        else
            favoriteImageView.visibility = View.GONE*/
    }
}