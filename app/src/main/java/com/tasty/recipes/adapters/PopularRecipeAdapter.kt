package com.tasty.recipes.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.databinding.ItemPopularRecipeBinding
import com.tasty.recipes.utils.SessionManager

class PopularRecipeAdapter (private var recipes: List<Recipe> = emptyList(),
                     private val onClickListener: (Recipe) -> Unit): RecyclerView.Adapter<PopularRecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularRecipeViewHolder {
        return PopularRecipeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_popular_recipe, parent, false)
        )
    }

    override fun getItemCount() = recipes.size

    override fun onBindViewHolder(holder: PopularRecipeViewHolder, position: Int) {
        holder.bind(recipes[position], onClickListener)
    }

    fun updateRecipes (list: List<Recipe>) {
        recipes = list
        notifyDataSetChanged()
    }
}

class PopularRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemRecipeBinding = ItemPopularRecipeBinding.bind(view)

    fun bind(recipe: Recipe, onClickListener: (Recipe) -> Unit) {
        val context = itemView.context

        itemRecipeBinding.recipeName.text = recipe.name
        itemRecipeBinding.preparationTime.text = "${recipe.prepTimeMinutes} min"

        if (recipe.image.startsWith("https://"))
            Picasso.get().load(recipe.image).into(itemRecipeBinding.recipeImage)
        else
            itemRecipeBinding.recipeImage.setImageURI(Uri.parse(recipe.image))

        itemView.setOnClickListener {
            onClickListener(recipe)
        }

        if (SessionManager(context).isFavorite(recipe.id.toString()))
            itemRecipeBinding.favoriteIcon.visibility = View.VISIBLE
        else
            itemRecipeBinding.favoriteIcon.visibility = View.GONE
    }
}