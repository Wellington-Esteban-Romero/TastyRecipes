package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.databinding.ItemSelectedCategoriesBinding

class SelectedCategoriesAdapter(
    private val categories: MutableList<Category>,
    private val onCategoryChecked: (Category, Boolean) -> Unit
) :
    RecyclerView.Adapter<SelectedCategoriesViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedCategoriesViewHolder {
        return SelectedCategoriesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_selected_categories, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SelectedCategoriesViewHolder, position: Int) {
        holder.bind(
            categories[position], categories.contains(categories[position]),
            onCategoryChecked
        )
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }
}

class SelectedCategoriesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemSelectedCategoriesBinding = ItemSelectedCategoriesBinding.bind(view)

    fun bind(
        category: Category, isSelected: Boolean,
        onCategoryChecked: (Category, Boolean) -> Unit
    ) {
        itemSelectedCategoriesBinding.checkbox.text = category.name

        itemSelectedCategoriesBinding.checkbox.setOnCheckedChangeListener { compoundButton, b ->
            onCategoryChecked(category, isSelected)
        }
    }

}