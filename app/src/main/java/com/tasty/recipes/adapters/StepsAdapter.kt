package com.tasty.recipes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R

class StepsAdapter(private val steps: String) : RecyclerView.Adapter<StepsAdapter.StepViewHolder>() {

    class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stepTextView: TextView = itemView.findViewById(R.id.tvStep)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.stepTextView.text = steps
    }

    override fun getItemCount() = 1

}