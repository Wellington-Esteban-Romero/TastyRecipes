package com.tasty.recipes.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.tasty.recipes.R
import com.tasty.recipes.adapters.RecipeAdapter
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var recipes:List<Recipe>
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeDAO: RecipeDAO

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
    }

    private fun initUI () {

        recipeDAO = RecipeDAO(this)
        recipes = recipeDAO.findAll()
        binding.searchView.isIconified = false
        binding.searchView.requestFocus()
        setupSearchView()
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(recipes) { recipe ->
            //onItemSelect(recipe)
        }

        binding.rvRecipes.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = recipeAdapter
        }
    }

    private fun setupSearchView () {
        binding.searchView.requestFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false;
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                searchByName(newText.orEmpty())
                return false
            }
        })
    }


    private fun searchByName (name: String) {

        val filteredList = recipes.filter { it.name.contains(name, true) }

        if (recipes.isEmpty()) {
            binding.rvRecipes.visibility = View.GONE
            //msg_empty.visibility = View.VISIBLE
        } else {
            binding.rvRecipes.visibility = View.VISIBLE
            //msg_empty.visibility = View.GONE
            recipeAdapter.updateRecipes(filteredList)
        }
    }
}