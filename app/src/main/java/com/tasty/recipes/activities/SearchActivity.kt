package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tasty.recipes.R
import com.tasty.recipes.activities.MainActivity.Companion.session
import com.tasty.recipes.adapters.RecipeAdapter
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var recipeList:MutableList<Recipe> = mutableListOf()
    private lateinit var recipeAdapter: RecipeAdapter

    companion object {
        const val EXTRA_RECIPE_TAG_SEARCH = "EXTRA_RECIPE_TAG_SEARCH"
        const val EXTRA_RECIPE_TAG_FAVORITE = "EXTRA_RECIPE_TAG_FAVORITE"
        const val LOAD_RECIPES_USER_ID = "1"
        const val LOAD_RECIPES_FAVORITES = "2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
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
        binding.searchView.requestFocus()
        loadRecipesByTag()
        setupSearchView()
        setupRecyclerView()
    }

    private fun initListener () {
        binding.imageViewBack.setOnClickListener {
            finish()
        }
    }

    private fun loadRecipesByTag() {
        val tag = intent.getStringExtra(EXTRA_RECIPE_TAG_SEARCH)
        when (tag) {
            LOAD_RECIPES_USER_ID -> {
                loadRecipesByUserId()
            }
            LOAD_RECIPES_FAVORITES -> {
                val favoriteList = intent.extras?.getStringArrayList(EXTRA_RECIPE_TAG_FAVORITE)
                if (!favoriteList.isNullOrEmpty()) {
                    favoriteList.forEach {
                        loadRecipeFavorite(it.split("_")[1])
                    }
                }
            }
            else -> {
                loadRecipesAll()
            }
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(recipeList) { recipe ->
            onItemSelect(recipe)
            saveSessionLastSeeRecipe(recipe)
        }

        binding.rvRecipes.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = recipeAdapter
        }
    }

    private fun setupSearchView () {
        binding.searchView.requestFocus()
        binding.searchView.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                searchByName(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun searchByName (name: String) {

        val filteredList = recipeList.filter { it.name.contains(name, true) }

        if (recipeList.isEmpty()) {
            binding.rvRecipes.visibility = View.GONE
            //msg_empty.visibility = View.VISIBLE
        } else {
            binding.rvRecipes.visibility = View.VISIBLE
            //msg_empty.visibility = View.GONE
            recipeAdapter.updateRecipes(filteredList)
        }
    }

    private fun onItemSelect(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id.toString())
        startActivity(intent)
    }

    private fun loadRecipesAll() {
        FirebaseFirestore.getInstance().collection("recipes").get()
            .addOnSuccessListener { querySnapshot ->
                recipeList.clear()

                querySnapshot.forEach { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipeList.add(recipe)
                }
                recipeAdapter.notifyItemInserted(recipeList.size - 1)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun loadRecipesByUserId() {
        FirebaseFirestore.getInstance().collection("recipes")
            .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                recipeList.clear()

                querySnapshot.forEach { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipeList.add(recipe)
                }
                recipeAdapter.notifyItemInserted(recipeList.size - 1)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun loadRecipeFavorite(id:String) {
        FirebaseFirestore.getInstance().collection("recipes")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                //recipeList.clear()

                querySnapshot.forEach { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipeList.add(recipe)
                }
                recipeAdapter.notifyItemInserted(recipeList.size - 1)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun saveSessionLastSeeRecipe (recipe: Recipe) {
        session.saveLastSee(this, recipe.id)
    }
}