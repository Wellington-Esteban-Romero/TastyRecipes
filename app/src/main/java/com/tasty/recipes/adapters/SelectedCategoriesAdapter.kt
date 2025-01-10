package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.data.entities.Category

class SelectedCategoriesAdapter(private val categories: MutableList<Category>) :
    RecyclerView.Adapter<CategorySelectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySelectionViewHolder {
        return CategorySelectionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            //crear un item
        )
    }

    override fun onBindViewHolder(holder: CategorySelectionViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }
}

class CategorySelectionViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(category: Category) {
        (view as TextView).text = category.name
    }

}