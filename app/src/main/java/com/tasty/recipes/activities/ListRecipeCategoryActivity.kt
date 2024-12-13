package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.adapters.ListRecipeCategoryAdapter
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityListRecipeCategoryBinding

class ListRecipeCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListRecipeCategoryBinding
    private lateinit var listCategoryRecipeAdapter: ListRecipeCategoryAdapter
    private lateinit var rvRecipesCategory: RecyclerView
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var recipes: List<Recipe>

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
    }

    private fun initUI () {

        val id = intent.getStringExtra(EXTRA_RECIPE_TAG_ID).orEmpty()
        val name = intent.getStringExtra(EXTRA_RECIPE_TAG_NAME).orEmpty()

        recipeDAO = RecipeDAO(this)
        rvRecipesCategory = findViewById(R.id.rvRecipesCategory)
        recipes = recipeDAO.findRecipeByCategory(name)
        //recipes.forEach { println(it) }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        listCategoryRecipeAdapter = ListRecipeCategoryAdapter(recipes) { recipe ->
            onItemSelect(recipe)
        }

        rvRecipesCategory.apply {
            layoutManager = LinearLayoutManager(this@ListRecipeCategoryActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = listCategoryRecipeAdapter
        }

    }

    private fun onItemSelect(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id.toString())
        startActivity(intent)
    }
}