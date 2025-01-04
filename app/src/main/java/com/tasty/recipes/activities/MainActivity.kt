package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.adapters.CategoryAdapter
import com.tasty.recipes.adapters.LastSeeRecipeAdapter
import com.tasty.recipes.adapters.PopularRecipeAdapter
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeCategoryDAO
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityMainBinding
import com.tasty.recipes.utils.AuthHelper
import com.tasty.recipes.utils.SessionManager
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryRecipeAdapter: CategoryAdapter
    private lateinit var popularRecipeAdapter: PopularRecipeAdapter
    private lateinit var lastSeeRecipeAdapter: LastSeeRecipeAdapter
    private lateinit var recipeDAO: RecipeDAO

    //private lateinit var categoryDAO: CategoryDAO
    private lateinit var recipeCategoryDAO: RecipeCategoryDAO

    private val categoryList: MutableList<Category> = mutableListOf()
    private val recipeList: MutableList<Recipe> = mutableListOf()


    companion object {
        lateinit var session: SessionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        initListener()
    }

    override fun onResume() {
        binding.editTextSearch.setBackgroundResource(R.drawable.search_background)
        binding.editTextSearch.clearFocus()
        //popularRecipeAdapter.notifyDataSetChanged()
        super.onResume()
    }

    private fun initUI() {

        session = SessionManager(applicationContext)
        recipeDAO = RecipeDAO(this)
        //categoryDAO = CategoryDAO(this)
        recipeCategoryDAO = RecipeCategoryDAO(this)

        binding.editTextSearch.clearFocus()

        loadCategories()
        loadRecipes()
        showInfoProfile(binding.iconProfile)

    }

    private fun initListener() {

        binding.editTextSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.editTextSearch.setBackgroundResource(R.drawable.edittext_default_background)

                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)

                true
            } else {
                false
            }
        }

        // Abrir el menú lateral al hacer clic en el ícono de perfil
        binding.iconProfile.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            val icoProfile = findViewById<ImageView>(R.id.profile_image);

            showInfoProfile(icoProfile)

            icoProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileUser::class.java))
            }
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    AuthHelper().getFirebaseAuth().signOut()
                    session.clearSession(this)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.bottomAppBar.setNavigationOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mainActionFavorite -> {

                    true
                }

                /*R.id.user -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    true
                }*/
                else -> false
            }

        }

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

    }

    private fun showInfoProfile(imageView: ImageView) {
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("users")

        userCollection.whereEqualTo("email", session.getUserEmail(this))
            .get()
            .addOnSuccessListener { documents ->
                Picasso.get()
                    .load(File(documents.documents[0]["photoUrl"].toString()))
                    .into(imageView)
                findViewById<TextView>(R.id.user_name).text =
                    documents.documents[0]["username"].toString()
                findViewById<TextView>(R.id.user_email).text =
                    documents.documents[0]["email"].toString()

            }
    }

    private fun setupRecyclerView() {

        categoryRecipeAdapter = CategoryAdapter(categoryList) { category ->
            onItemSelectCategory(category)
        }

        binding.rvCategories.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryRecipeAdapter
        }

        popularRecipeAdapter = PopularRecipeAdapter(recipeList) { recipe ->
            onItemSelect(recipe)
        }

        binding.rvRecipesPopular.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularRecipeAdapter
        }

        lastSeeRecipeAdapter = LastSeeRecipeAdapter(recipeDAO.findAll().take(1)) { recipe ->
            onItemSelect(recipe)
        }

        binding.rvRecipesLastSee.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = lastSeeRecipeAdapter
        }
    }

    private fun onItemSelectCategory(category: Category) {
        val intent = Intent(this, ListRecipeCategoryActivity::class.java)
        intent.putExtra(ListRecipeCategoryActivity.EXTRA_RECIPE_TAG_ID, category.id.toString())
        intent.putExtra(ListRecipeCategoryActivity.EXTRA_RECIPE_TAG_NAME, category.name)
        startActivity(intent)
    }

    private fun onItemSelect(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id.toString())

        if (!session.isFavorite(recipe.id.toString()))
            session.saveRecipe(recipe.id.toString(), SessionManager.DES_ACTIVE)

        startActivity(intent)
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun loadCategories() {
        FirebaseFirestore.getInstance().collection("categories").get()
            .addOnSuccessListener { querySnapshot ->
                categoryList.clear()

                querySnapshot.forEach { document ->
                    val category = document.toObject(Category::class.java)
                    categoryList.add(category)
                }
                setupRecyclerView()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar categorías: ${exception.message}")
            }

    }

    private fun loadRecipes() {
        FirebaseFirestore.getInstance().collection("recipes").get()
            .addOnSuccessListener { querySnapshot ->
                recipeList.clear()

                querySnapshot.forEach { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipeList.add(recipe)
                }
                setupRecyclerView()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }
}