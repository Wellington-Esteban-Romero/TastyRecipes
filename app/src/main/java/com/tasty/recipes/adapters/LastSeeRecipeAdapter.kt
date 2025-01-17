package com.tasty.recipes.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.databinding.ItemLastseeRecipeBinding

class LastSeeRecipeAdapter (private var recipes: List<Recipe> = emptyList(),
                     private val onClickListener: (Recipe) -> Unit): RecyclerView.Adapter<LastSeeRecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LastSeeRecipeViewHolder {
        return LastSeeRecipeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_lastsee_recipe, parent, false)
        )
    }

    override fun getItemCount() = recipes.size

    override fun onBindViewHolder(holder: LastSeeRecipeViewHolder, position: Int) {
        holder.bind(recipes[position], onClickListener)
    }

    fun updateData(newList: List<Recipe>) {
        this.recipes = newList
        notifyDataSetChanged()
    }
}

class LastSeeRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemRecipeBinding = ItemLastseeRecipeBinding.bind(view)
    //private val favoriteImageView = view.findViewById<ImageView>(R.id.imgFavorite)

    fun bind(recipe: Recipe, onClickListener: (Recipe) -> Unit) {

        itemRecipeBinding.recipeName.text = recipe.name
        itemRecipeBinding.preparationTime.text = "${recipe.prepTimeMinutes} min"

        if (recipe.image.startsWith("https://"))
            Picasso.get().load(recipe.image).into(itemRecipeBinding.recipeImage)
        else
            itemRecipeBinding.recipeImage.setImageURI(Uri.parse(recipe.image))

        itemView.setOnClickListener {
            onClickListener(recipe)
        }

       /* if (SessionManager(context).isFavorite(recipe.id.toString()))
            favoriteImageView.visibility = View.VISIBLE
        else
            favoriteImageView.visibility = View.GONE*/
    }
}