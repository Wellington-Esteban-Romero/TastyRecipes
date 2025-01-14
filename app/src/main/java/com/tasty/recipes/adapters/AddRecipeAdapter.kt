package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ItemAddIngredientBinding

class AddRecipeAdapter(
    private val ingredients: MutableList<String> = mutableListOf(),
    private val onClickListener: (Int) -> Unit
) :
    RecyclerView.Adapter<AddRecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddRecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_ingredient, parent, false)
        return AddRecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddRecipeViewHolder, position: Int) {
        holder.bind(ingredients[position], onClickListener, position)
    }

    override fun getItemCount(): Int = ingredients.size

    fun updateCategories(newCategories: MutableList<String>) {
        ingredients.clear()
        ingredients.addAll(newCategories)
        notifyDataSetChanged()
    }

    fun removeIngredient(position: Int) {
        if (position in ingredients.indices) {
            ingredients.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, ingredients.size)
        }
    }
}

class AddRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var itemAddIngredientBinding = ItemAddIngredientBinding.bind(view)

    fun bind(ingredient: String, onClickListener: (Int) -> Unit, position: Int) {
        itemAddIngredientBinding.textViewIngredient.text = ingredient

        itemAddIngredientBinding.btnDeleteIngredient.setOnClickListener {
            onClickListener(position)
        }
    }
}