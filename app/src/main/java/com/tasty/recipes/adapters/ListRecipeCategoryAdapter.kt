package com.tasty.recipes.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.databinding.ItemRecipeCategoryBinding

class ListRecipeCategoryAdapter (private var recipes: List<Recipe> = emptyList(),
                     private val onClickListener: (Recipe) -> Unit): RecyclerView.Adapter<ListRecipeCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListRecipeCategoryViewHolder {
        return ListRecipeCategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_category, parent, false)
        )
    }

    override fun getItemCount() = recipes.size

    override fun onBindViewHolder(holder: ListRecipeCategoryViewHolder, position: Int) {
        holder.bind(recipes[position], onClickListener)
    }
}

class ListRecipeCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemRecipeCategoryBinding = ItemRecipeCategoryBinding.bind(view)

    fun bind(recipe: Recipe, onClickListener: (Recipe) -> Unit) {

        itemRecipeCategoryBinding.recipeName.text = recipe.name

        if (recipe.image.startsWith("https://"))
            Picasso.get().load(recipe.image).into(itemRecipeCategoryBinding.recipeImage)
        else
            itemRecipeCategoryBinding.recipeImage.setImageURI(Uri.parse(recipe.image))

        itemView.setOnClickListener {
            onClickListener(recipe)
        }
    }
}