package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tasty.recipes.R
import com.tasty.recipes.adapters.CategoryAdapter
import com.tasty.recipes.adapters.LastSeeRecipeAdapter
import com.tasty.recipes.adapters.PopularRecipeAdapter
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.entities.RecipeCategory
import com.tasty.recipes.data.providers.CategoryDAO
import com.tasty.recipes.data.providers.RecipeCategoryDAO
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var categoryRecipeAdapter: CategoryAdapter
    private lateinit var popularRecipeAdapter: PopularRecipeAdapter
    private lateinit var lastSeeRecipeAdapter: LastSeeRecipeAdapter
    private lateinit var rvCategoryRecipes: RecyclerView
    private lateinit var rvRecipesPopular: RecyclerView
    private lateinit var rvRecipesLastSee: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var btnAddRecipe: FloatingActionButton
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var categoryDAO: CategoryDAO
    private lateinit var recipeCategoryDAO: RecipeCategoryDAO

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
        initUI()
        initListener()
    }

    override fun onResume() {
        editTextSearch.setBackgroundResource(R.drawable.search_background)
        editTextSearch.clearFocus()
        super.onResume()
    }

    private fun initUI() {

        session = SessionManager(applicationContext)
        rvCategoryRecipes = findViewById(R.id.rvCategories)
        rvRecipesPopular = findViewById(R.id.rvRecipesPopular)
        rvRecipesLastSee = findViewById(R.id.rvRecipesLastSee)
        editTextSearch = findViewById(R.id.editTextSearch)
        //btnAddRecipe = findViewById(R.id.btnAddRecipe)
        recipeDAO = RecipeDAO(this)
        categoryDAO = CategoryDAO(this)
        recipeCategoryDAO = RecipeCategoryDAO(this)

        if (!session.isLoadRecipes("loadRecipe")) {
            saveCategories()
            saveRecipeCategory()
        }
        setupRecyclerView()
    }

    private fun initListener() {

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

        categoryRecipeAdapter = CategoryAdapter(categoryDAO.findAll()) { category ->
            onItemSelectCategory(category)
        }

        rvCategoryRecipes.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryRecipeAdapter
        }

        popularRecipeAdapter = PopularRecipeAdapter(recipeDAO.findAll().take(5)) { recipe ->
            onItemSelect(recipe)
        }

        rvRecipesPopular.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularRecipeAdapter
        }

        lastSeeRecipeAdapter = LastSeeRecipeAdapter(recipeDAO.findAll().take(1)) { recipe ->
            onItemSelect(recipe)
        }

        rvRecipesLastSee.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = lastSeeRecipeAdapter
        }
    }

    private fun onItemSelectCategory(category: Category) {
        val intent = Intent(this, ListRecipeCategoryActivity::class.java)
        intent.putExtra(ListRecipeCategoryActivity.EXTRA_RECIPE_TAG_ID, category.id.toString())
        startActivity(intent)
    }

    private fun onItemSelect(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id.toString())
        startActivity(intent)
    }

    private fun saveCategories() {
        categoryDAO.insert(Category(1, "Main", ""))
        categoryDAO.insert(Category(2, "Dessert", ""))
        categoryDAO.insert(Category(3, "Salad", ""))
        categoryDAO.insert(Category(4, "Italian", ""))
        categoryDAO.insert(Category(5, "Asian", ""))
        categoryDAO.insert(Category(6, "Salsa", ""))
        categoryDAO.insert(Category(7, "Japanese", ""))
        categoryDAO.insert(Category(8, "Soup", ""))
        categoryDAO.insert(Category(9, "Cocktail", ""))
        categoryDAO.insert(Category(10, "Spanish", ""))
        categoryDAO.insert(Category(11, "Snack", ""))
        categoryDAO.insert(Category(12, "Kebabs", ""))
        categoryDAO.insert(Category(13, "Lunch", ""))
        categoryDAO.insert(Category(14, "Indian", ""))


        session.saveRecipes("loadRecipe", SessionManager.ACTIVE)
        //popularRecipeAdapter.updateRecipes(recipeDAO.findAll().take(5))

    }

    private fun saveRecipeCategory() {
        recipeCategoryDAO.insert(RecipeCategory(1, 4))
        recipeCategoryDAO.insert(RecipeCategory(2, 5))
        recipeCategoryDAO.insert(RecipeCategory(2, 7))
    }
}