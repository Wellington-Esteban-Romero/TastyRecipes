package com.tasty.recipes.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.adapters.CategoryAdapter
import com.tasty.recipes.adapters.LastSeeRecipeAdapter
import com.tasty.recipes.adapters.PopularRecipeAdapter
import com.tasty.recipes.adapters.UsersAdapter
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.entities.User
import com.tasty.recipes.databinding.ActivityMainBinding
import com.tasty.recipes.utils.SessionManager
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryRecipeAdapter: CategoryAdapter
    private lateinit var popularRecipeAdapter: PopularRecipeAdapter
    private lateinit var lastSeeRecipeAdapter: LastSeeRecipeAdapter
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    private val categoryList: MutableList<Category> = mutableListOf()
    private val recipeList: MutableList<Recipe> = mutableListOf()
    private val userList: MutableList<User> = mutableListOf()
    private var recipeListLastSee: MutableList<Recipe> = mutableListOf()

    companion object {
        lateinit var session: SessionManager
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.insetsController?.let {
            it.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        binding.bottomAppBar.setOnApplyWindowInsetsListener { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }

        initUI()
        initListener()
    }

    override fun onResume() {
        loadRecipes()
        resetSearchField()
        getLastSeeRecipe()
        super.onResume()
    }

    private fun initUI() {
        session = SessionManager(applicationContext)
        firebaseAuth = FirebaseAuth.getInstance()
        showInfoProfile(false)
        loadCategories()
        loadUsers()
        setupRecyclerView()
        resetSearchField()
    }

    private fun initListener() {
        setupSearchListener()
        setupProfileMenuListeners()
        setupBottomAppBarListeners()
        setupFloatingActionButton()
        setupShowViewAll()
    }

    private fun resetSearchField() {
        with(binding.editTextSearch) {
            setBackgroundResource(R.drawable.search_background)
            clearFocus()
        }
    }

    private fun setupSearchListener() {
        binding.editTextSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.editTextSearch.setBackgroundResource(R.drawable.edittext_default_background)
                startActivity(Intent(this, SearchActivity::class.java))
                true
            } else {
                false
            }
        }
    }

    private fun setupProfileMenuListeners() {
        binding.iconProfile.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            showInfoProfile(true)

            binding.navigationView.findViewById<ImageView>(R.id.profile_image).setOnClickListener {
                startActivity(Intent(this, EditProfileUser::class.java))
            }
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    firebaseAuth.signOut()
                }

                R.id.nav_my_recipes -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra(
                        SearchActivity.EXTRA_RECIPE_TAG_SEARCH,
                        SearchActivity.LOAD_RECIPES_USER_ID
                    )
                    startActivity(intent)
                }

                R.id.nav_favorites -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra(
                        SearchActivity.EXTRA_RECIPE_TAG_SEARCH,
                        SearchActivity.LOAD_RECIPES_FAVORITES
                    )
                    intent.putStringArrayListExtra(SearchActivity.EXTRA_RECIPE_TAG_FAVORITE,
                        session.getAllFavoriteRecipe()?.entries?.filter {
                            FirebaseAuth.getInstance().currentUser?.uid == it.key.split("_")[0] && it.value == "1"
                        }?.map { it.key } as ArrayList<String>?
                    )
                    startActivity(intent)
                }

                R.id.nav_shared_with_me -> {
                    val appPackageName = applicationContext.packageName
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out the App at: https://play.google.com/store/apps/details?id=$appPackageName"
                    )
                    intent.type = "text/plain"
                    startActivity(intent)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomAppBarListeners() {
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
    }

    private fun setupFloatingActionButton() {
        binding.floatingActionButton.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }
    }

    private fun setupShowViewAll() {
        binding.btnViewAll.setOnClickListener {
            startActivity(Intent(this, ListUserActivity::class.java))
        }
    }

    private fun showInfoProfile(openMenu: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("users")

        userCollection.whereEqualTo("email", firebaseAuth.currentUser?.email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener

                val userInfo = documents.documents[0]
                updateProfile(
                    openMenu,
                    userInfo["photoUrl"].toString(),
                    userInfo["username"].toString(),
                    userInfo["email"].toString()
                )
            }
    }

    private fun updateProfile(
        openMenu: Boolean,
        photoUrl: String,
        username: String,
        email: String
    ) {
        val profileImage =
            if (openMenu) binding.navigationView.findViewById(R.id.profile_image) else binding.iconProfile
        Picasso.get().load(photoUrl).into(profileImage)
        if (openMenu) {
            binding.navigationView.findViewById<TextView>(R.id.user_name).text = username
            binding.navigationView.findViewById<TextView>(R.id.user_email).text = email
        }
    }

    private fun setupRecyclerView() {

        categoryRecipeAdapter = CategoryAdapter(categoryList) { category ->
            onItemSelectCategory(category)
        }

        binding.rvCategories.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL,
                    false)
            adapter = categoryRecipeAdapter
        }

        popularRecipeAdapter = PopularRecipeAdapter(recipeList) { recipe ->
            onItemSelect(recipe)
        }

        binding.rvRecipesPopular.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL,
                    false)
            adapter = popularRecipeAdapter
        }

        usersAdapter = UsersAdapter(userList) {

        }

        binding.rvRegisteredUsers.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL,
                    false)
            adapter = usersAdapter
        }

        lastSeeRecipeAdapter = LastSeeRecipeAdapter(recipeListLastSee) { recipe ->
            onItemSelect(recipe)
        }

        binding.rvRecipesLastSee.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = lastSeeRecipeAdapter
        }
    }

    private fun onItemSelectCategory(category: Category) {
        val intent = Intent(this, ListRecipeCategoryActivity::class.java)
        intent.putExtra(ListRecipeCategoryActivity.EXTRA_RECIPE_TAG_ID, category.id)
        intent.putExtra(ListRecipeCategoryActivity.EXTRA_RECIPE_TAG_NAME, category.name)
        startActivity(intent)
    }

    private fun onItemSelect(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (!session.isFavorite(currentUser?.uid + "_" + recipe.id))
            session.saveRecipe(currentUser?.uid + "_" + recipe.id, SessionManager.DES_ACTIVE)
        session.saveLastSee(this, recipe.id)

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
                categoryRecipeAdapter.notifyItemInserted(categoryList.size - 1)
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
                popularRecipeAdapter.updateRecipes(recipeList)
                getLastSeeRecipe()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun loadUsers() {
        FirebaseFirestore.getInstance().collection("users")
            .whereNotEqualTo("id", firebaseAuth.currentUser?.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                userList.clear()

                querySnapshot.forEach { document ->
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
                usersAdapter.updateUsers(userList)
                getLastSeeRecipe()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun getLastSeeRecipe() {
        val lastSee = session.getLastSee(this)
        lastSee?.let { lastSeeId ->
            recipeListLastSee = recipeList.filter { it.id == lastSeeId }.toMutableList()
            lastSeeRecipeAdapter.updateRecipe(recipeListLastSee)
        } ?: run {
            println("El valor de lastSee no es un número válido.")
        }
    }
}