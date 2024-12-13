package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.databinding.ItemCategoryBinding

class CategoryAdapter (private var categories: List<Category> = emptyList(),
                     private val onClickListener: (Category) -> Unit): RecyclerView.Adapter<CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        )
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], onClickListener)
    }
}

class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val itemCategoryBinding = ItemCategoryBinding.bind(view)

    fun bind(category: Category, onClickListener: (Category) -> Unit) {

        itemCategoryBinding.ItemCategoryName.text = category.name

        itemCategoryBinding.ItemCategoryName.setOnClickListener {
            onClickListener(category)
        }
    }
}