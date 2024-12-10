package com.tasty.recipes.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tasty.recipes.R
import com.tasty.recipes.adapters.PopularRecipeAdapter
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

    private lateinit var popularRecipeAdapter: PopularRecipeAdapter
    private lateinit var recipeService: RecipeService
    private lateinit var rvRecipesPopular: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var btnAddRecipe: FloatingActionButton
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
        initListener()
    }

    override fun onResume() {
        editTextSearch.setBackgroundResource(R.drawable.search_background)
        super.onResume()
    }

    private fun init () {

        session = SessionManager(applicationContext)
        recipeService = RetrofitProvider.getRetrofit()
        rvRecipesPopular = findViewById(R.id.rvRecipesPopular)
        editTextSearch = findViewById(R.id.editTextSearch)
        //btnAddRecipe = findViewById(R.id.btnAddRecipe)
        recipeDAO = RecipeDAO(this)

        if (!session.isLoadRecipes("loadRecipe"))
            getAllRecipesFromService()

        setupRecyclerView()
    }

    private fun initListener () {

        editTextSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Cambiar el fondo del EditText
                editTextSearch.setBackgroundResource(R.drawable.edittext_default_background)

                // Iniciar la otra actividad
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)

                true // Intercepta el evento
            } else {
                false
            }
        }

    }

    private fun setupRecyclerView() {
        popularRecipeAdapter = PopularRecipeAdapter(recipeDAO.findAll().take(5)) { recipe ->
            //onItemSelect(recipe)
        }

        rvRecipesPopular.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularRecipeAdapter
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
            popularRecipeAdapter.updateRecipes(recipeDAO.findAll())
        }
    }
}