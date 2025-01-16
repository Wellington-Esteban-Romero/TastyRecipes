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
import com.tasty.recipes.adapters.ListRecipeCategoryAdapter
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.databinding.ActivityListRecipeCategoryBinding

class ListRecipeCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListRecipeCategoryBinding
    private lateinit var listCategoryRecipeAdapter: ListRecipeCategoryAdapter
    private val recipeList: MutableList<Recipe> = mutableListOf()

    companion object {
        val EXTRA_RECIPE_TAG_ID = "EXTRA_RECIPE_TAG_ID"
        val EXTRA_RECIPE_TAG_NAME = "EXTRA_RECIPE_TAG_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityListRecipeCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initUI()
        initListener()
    }

    private fun initUI () {

        val id = intent.getStringExtra(EXTRA_RECIPE_TAG_ID).orEmpty()
        val name = intent.getStringExtra(EXTRA_RECIPE_TAG_NAME).orEmpty()

        binding.titleRecipe.text = name

        setupRecyclerView()
        loadRecipes(id.toInt())
    }

    private fun initListener () {
        binding.goBackHome.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {

        listCategoryRecipeAdapter = ListRecipeCategoryAdapter(recipeList) { recipe ->
            onItemSelect(recipe)
        }

        binding.rvRecipesCategory.apply {
            layoutManager = LinearLayoutManager(this@ListRecipeCategoryActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = listCategoryRecipeAdapter
        }

    }

    private fun onItemSelect(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id.toString())
        startActivity(intent)
    }

    private fun loadRecipes(categoryId: Int) {
        FirebaseFirestore.getInstance().collection("recipes")
            .whereArrayContains("categoryId", mutableListOf(categoryId)).get()
            .addOnSuccessListener { querySnapshot ->
                recipeList.clear()

                querySnapshot.forEach { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipeList.add(recipe)
                }
                listCategoryRecipeAdapter.notifyItemInserted(recipeList.size - 1)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }
}