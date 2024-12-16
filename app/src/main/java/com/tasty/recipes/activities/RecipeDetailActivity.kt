package com.tasty.recipes.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityRecipeDetailBinding
import com.tasty.recipes.utils.SessionManager

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var recipe:Recipe
    private lateinit var idRecipe: String

    companion object {
        val EXTRA_RECIPE_ID = "EXTRA_RECIPE_ID"
        lateinit var session: SessionManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initUI()
        initListener()
    }

    private fun initUI () {

        session = SessionManager(applicationContext)

        idRecipe = intent.getStringExtra(EXTRA_RECIPE_ID).orEmpty()

        recipeDAO = RecipeDAO(this)

        recipe = recipeDAO.findRecipeById(idRecipe)!!

        createDetails(recipe)

        println(idRecipe)
    }

    private fun initListener () {

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.recipe_toolbar_menu, menu)
        if (session.isFavorite(idRecipe))
            menu?.findItem(R.id.actionFavorite)?.setIcon(R.drawable.ic_favorite)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println(item.icon)
        when (item.itemId) {
            R.id.action_favorite -> {
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                if (!session.isFavorite(idRecipe)) {
                    session.saveRecipe(idRecipe, SessionManager.ACTIVE)
                    item.setIcon(R.drawable.ic_favorite)
                } else {
                    session.saveRecipe(idRecipe, SessionManager.DES_ACTIVE)
                    item.setIcon(R.drawable.ic_favorite_empty)
                }
                true
            }
            R.id.action_share -> {
                Toast.makeText(this, "Sharing Recipe...", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createDetails (recipe: Recipe) {
        Picasso.get().load(recipe.image).into(binding.imageRecipe)
        binding.toolbar.title = recipe.name
        binding.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        //binding.txtInstructions.text = recipes[0].instructions.split(", ").joinToString("\n\n")

    }
}