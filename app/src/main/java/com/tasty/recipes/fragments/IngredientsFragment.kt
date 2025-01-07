package com.tasty.recipes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.adapters.IngredientsAdapter


class IngredientsFragment(
    private var ingredients: List<String>
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ingredients, container, false)

        view.findViewById<RecyclerView>(R.id.rvIngredients).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = IngredientsAdapter(ingredients)
        }
        return view
    }
}