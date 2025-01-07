package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ItemIngredientBinding

class IngredientsAdapter(private var ingredients: List<String>) :
    RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount() = ingredients.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemIngredientBinding = ItemIngredientBinding.bind(view)

        fun bind (ingredient: String) {
            itemIngredientBinding.checkbox.text = ingredient
        }
    }
}