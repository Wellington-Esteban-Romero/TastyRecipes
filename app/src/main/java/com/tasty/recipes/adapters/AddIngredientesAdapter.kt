package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ItemAddIngredientBinding

class AddIngredientsAdapter(
    private val ingredients: MutableList<String>,
    private val onClickListener: (Int) -> Unit) :
    RecyclerView.Adapter<AddIngredientsAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position], onClickListener, position)
    }

    override fun getItemCount(): Int = ingredients.size

    class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var itemAddIngredientBinding = ItemAddIngredientBinding.bind(view)

        fun bind (ingredient: String, onClickListener: (Int) -> Unit, position: Int) {
            itemAddIngredientBinding.textViewIngredient.text = ingredient

            itemAddIngredientBinding.btnDeleteIngredient.setOnClickListener {
                onClickListener(position)
            }
        }
    }
}