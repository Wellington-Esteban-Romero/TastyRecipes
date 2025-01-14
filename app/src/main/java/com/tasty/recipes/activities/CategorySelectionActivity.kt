package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.tasty.recipes.R
import com.tasty.recipes.adapters.SelectedCategoriesAdapter
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.databinding.ActivityCategorySelectionBinding

class CategorySelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategorySelectionBinding
    private val categoryList: MutableList<Category> = mutableListOf()
    private var selectedCategories: MutableList<String> = mutableListOf()
    private lateinit var selectedCategoriesAdapter: SelectedCategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) { // cambiar a un popup
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategorySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        loadCategories()
        setupToolBarListeners()
        selectedCategories()
    }

    private fun setupToolBarListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadCategories() {
        FirebaseFirestore.getInstance().collection("categories").get()
            .addOnSuccessListener { querySnapshot ->
                categoryList.clear()

                querySnapshot.forEach { document ->
                    val category = document.toObject(Category::class.java)
                    categoryList.add(category)
                }
                selectedCategoriesAdapter.notifyItemInserted(categoryList.size - 1)

                if (AddRecipeActivity.SELECTED_CATEGORIES.isNotEmpty()) {
                    selectedCategories = AddRecipeActivity.SELECTED_CATEGORIES.map { it.name }.toMutableList()
                    selectedCategoriesAdapter.autoCheckCategories(AddRecipeActivity.SELECTED_CATEGORIES)
                }

            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar categorías: ${exception.message}")
            }
    }

    private fun setupRecyclerView() {
        selectedCategoriesAdapter = SelectedCategoriesAdapter(categoryList) { category, isChecked ->
            if (isChecked) {
                selectedCategories.add(category.name)
            } else {
                selectedCategories.remove(category.name)
            }
        }
        binding.rvSelectCategories.apply {
            layoutManager = LinearLayoutManager(this@CategorySelectionActivity)
            adapter = selectedCategoriesAdapter
        }
    }

    private fun selectedCategories () {
        binding.buttonDone.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            intent.putStringArrayListExtra("selectedCategories", ArrayList(selectedCategories))
            startActivity(intent)
        }
    }

}