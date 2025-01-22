package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ItemAddCategoryBinding

class AddCategoryAdapter(
    private val categories: MutableList<String> = mutableListOf(),
    private val onClickListener: (Int) -> Unit
) :
    RecyclerView.Adapter<AddCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_category, parent, false)
        return AddCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddCategoryViewHolder, position: Int) {
        holder.bind(categories[position], onClickListener, position)
    }

    override fun getItemCount(): Int = categories.size

    fun removeCategory(position: Int) {
        if (position in categories.indices) {
            categories.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, categories.size)
        }
    }
}

class AddCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var itemAddCategoryBinding = ItemAddCategoryBinding.bind(view)

    fun bind(category: String, onClickListener: (Int) -> Unit, position: Int) {
        itemAddCategoryBinding.chipCategory.text = category

        itemAddCategoryBinding.chipCategory.setOnCloseIconClickListener {
            onClickListener(position)
        }
    }
}