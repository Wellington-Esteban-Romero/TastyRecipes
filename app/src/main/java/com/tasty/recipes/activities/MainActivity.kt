package com.tasty.recipes.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tasty.recipes.R
import com.tasty.recipes.adapters.RecipeAdapter
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.entities.RecipeResponse
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.data.providers.RetrofitProvider
import com.tasty.recipes.services.RecipeService
import com.tasty.recipes.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeService: RecipeService
    private lateinit var rvRecipes: RecyclerView
    private lateinit var recipeDAO: RecipeDAO

    companion object {
        lateinit var session: SessionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
    }

    private fun init () {

        session = SessionManager(applicationContext)
        recipeService = RetrofitProvider.getRetrofit()
        rvRecipes = findViewById(R.id.rvRecipes)
        recipeDAO = RecipeDAO(this)

        if (!session.isLoadRecipes("loadRecipe"))
            getAllRecipesFromService()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(recipeDAO.findAll()) { recipe ->
            //onItemSelect(recipe)
        }

        rvRecipes.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = recipeAdapter
        }
    }

    private fun getAllRecipesFromService () {

        CoroutineScope(Dispatchers.IO).launch {
            val response = recipeService.findAll()

            if (response.isSuccessful) {
                response.body()?.let { recipeResponse ->
                    Log.i("RecipesAPI", "Fetched recipes: ${recipeResponse.recipes.size}")
                    saveAndLoadRecipes(recipeResponse)
                }
            }
        }
    }

    private suspend fun saveAndLoadRecipes (recipeResponse:RecipeResponse) {

        recipeResponse.recipes.forEach { recipe ->
            recipeDAO.insert(
                Recipe(
                    id = recipe.id,
                    name = recipe.name,
                    ingredients = recipe.ingredients,
                    instructions = recipe.instructions,
                    prepTimeMinutes = recipe.prepTimeMinutes,
                    cookTimeMinutes = recipe.cookTimeMinutes,
                    servings = recipe.servings,
                    difficulty = recipe.difficulty,
                    image = recipe.image
                )
            )
        }

        withContext(Dispatchers.Main) {
            session.saveRecipes("loadRecipe", SessionManager.ACTIVE)
            recipeAdapter.updateRecipes(recipeDAO.findAll())
        }
    }
}