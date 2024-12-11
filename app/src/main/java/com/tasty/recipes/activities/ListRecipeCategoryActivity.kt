package com.tasty.recipes.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityListRecipeCategoryBinding

class ListRecipeCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListRecipeCategoryBinding
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var recipe: Recipe

    companion object {
        val EXTRA_RECIPE_TAG_ID = "EXTRA_RECIPE_TAG_ID"
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
        println(id)

    }
}