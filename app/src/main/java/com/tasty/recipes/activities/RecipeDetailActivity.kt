package com.tasty.recipes.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityRecipeDetailBinding
import com.tasty.recipes.databinding.ActivitySearchBinding

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var recipe:Recipe
    private lateinit var toolbar:MaterialToolbar

    companion object {
        val EXTRA_RECIPE_ID = "EXTRA_RECIPE_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
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
        val id = intent.getStringExtra(EXTRA_RECIPE_ID).orEmpty()

        recipeDAO = RecipeDAO(this)

        toolbar = findViewById(R.id.toolbar)
        recipe = recipeDAO.findRecipeById(id)!!

        createDetails(recipe)

        println(id)
    }

    private fun initListener () {

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_favorite -> {
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_share -> {
                    Toast.makeText(this, "Sharing Recipe...", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun createDetails (recipe: Recipe) {
        Picasso.get().load(recipe.image).into(binding.imageRecipe)
        binding.toolbar.title = recipe.name

        //binding.txtInstructions.text = recipes[0].instructions.split(", ").joinToString("\n\n")

    }
}