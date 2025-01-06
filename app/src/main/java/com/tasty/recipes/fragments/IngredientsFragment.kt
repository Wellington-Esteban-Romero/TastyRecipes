package com.tasty.recipes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ItemIngredientBinding


class IngredientsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ingredients, container, false)

        val ingredients: List<String> = mutableListOf(
            "2 cups of flour",
            "1 cup of sugar",
            "1/2 cup of butter",
            "1 teaspoon of vanilla extract",
            "1/2 teaspoon of salt",
            "1/2 teaspoon of baking soda"
        ) //pasar lista
        view.findViewById<RecyclerView>(R.id.rvIngredients).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = IngredientsAdapter(ingredients)
        }
        return view
    }
}

class IngredientsAdapter(private val ingredients: List<String>) :
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