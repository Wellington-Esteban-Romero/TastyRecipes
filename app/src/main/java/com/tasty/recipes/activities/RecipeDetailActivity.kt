package com.tasty.recipes.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Follower
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.entities.User
import com.tasty.recipes.databinding.ActivityRecipeDetailBinding
import com.tasty.recipes.fragments.IngredientsFragment
import com.tasty.recipes.fragments.StepsFragment
import com.tasty.recipes.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var idRecipe: String
    private lateinit var recipe: Recipe

    companion object {
        const val EXTRA_RECIPE_ID = "EXTRA_RECIPE_ID"
        lateinit var session: SessionManager
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

       window.insetsController?.let {
            it.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

         /*ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBarsInsets.top, 0, systemBarsInsets.bottom)
            insets
        }*/

        setSupportActionBar(binding.toolbar)
        session = SessionManager(applicationContext)

        initUI()
        initListener()
    }

    override fun onResume() {
        loadRecipeById()
        super.onResume()
    }

    private fun initUI() {
        idRecipe = intent.getStringExtra(EXTRA_RECIPE_ID).orEmpty()
        updateRecipe()
    }

    private fun initListener() {
        setupToolBarListeners()
        setupButtonNavigationView()
        setupButtonAppBarListeners()
        setupButtonDeleteRecipe()
        setupButtonFollowListeners()
    }

    private fun loadRecipeById() {
        FirebaseFirestore.getInstance().collection("recipes")
            .whereEqualTo("id", idRecipe)
            .get()
            .addOnSuccessListener { querySnapshot ->
                recipe = querySnapshot.documents[0].toObject(Recipe::class.java)!!
                createDetails()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun updateRecipe() {
        binding.btnUpdateRecipe.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            intent.putExtra(AddRecipeActivity.EXTRA_UPDATE_TAG_ID, idRecipe)
            startActivity(intent)
        }
    }

    private fun setupToolBarListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupButtonNavigationView() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_ingredients -> selectedFragment = IngredientsFragment(recipe.ingredients)
                R.id.nav_steps -> selectedFragment = StepsFragment(recipe.instructions)
            }

            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }
    }

    private fun setupButtonAppBarListeners() {
        binding.bottomAppBar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search_details -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }

                R.id.action_favorite_details -> {
                    true
                }

                else -> false
            }
        }
    }

    private fun setupButtonDeleteRecipe() {
        binding.btnDeleteRecipe.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.alert_dialog_delete_title)
                .setMessage(R.string.alert_dialog_delete_message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    FirebaseFirestore.getInstance().collection("recipes")
                        .document(idRecipe)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Document successfully deleted!")
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firestore", "Error deleting document", e)
                        }
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setIcon(android.R.drawable.ic_delete)
                .show()
        }
    }

    private fun setupButtonFollowListeners() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        binding.btnFollow.setOnClickListener {
            FirebaseFirestore.getInstance().collection("followers")
                .add(Follower(uid, recipe.userId, recipe.id))
                .addOnSuccessListener {
                    showToast("followers add successfully")
                    disableButtonFollow()
                }
                .addOnFailureListener { e ->
                    showToast("Error add followers: ${e.message}")
                    finish()
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.recipe_toolbar_menu, menu)
        if (session.isFavorite(FirebaseAuth.getInstance().currentUser?.uid + "_" + idRecipe))
            menu?.findItem(R.id.action_favorite)?.setIcon(R.drawable.ic_favorite)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        when (item.itemId) {
            R.id.action_favorite -> {
                if (!session.isFavorite(currentUser?.uid + "_" + idRecipe)) {
                    session.saveRecipe(currentUser?.uid + "_" + idRecipe, SessionManager.ACTIVE)
                    item.setIcon(R.drawable.ic_favorite)
                } else {
                    session.saveRecipe(currentUser?.uid + "_" + idRecipe, SessionManager.DES_ACTIVE)
                    item.setIcon(R.drawable.ic_favorite_empty)
                }
                true
            }

            R.id.action_share -> {

                binding.imageRecipe.isDrawingCacheEnabled = true
                val bitmap: Bitmap = Bitmap.createBitmap(binding.imageRecipe.drawingCache)
                binding.imageRecipe.isDrawingCacheEnabled = false

                shareTextAndImage(binding.toolbar.title.toString(), bitmap);
                true
            }

            else -> false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareTextAndImage(text: String, bitmap: Bitmap) {

        val imageUri: Uri = saveImageToCache(bitmap)!!

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("image/*")
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(Intent.createChooser(shareIntent, "Share Recipe via"))
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri? {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, UUID.randomUUID().toString() + ".png")
        try {
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                return FileProvider.getUriForFile(
                    this,
                    "com.tasty.recipes",
                    file
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun createDetails() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        createDetailsUser()
        checkEnabledButtonFollow()

        if (recipe.userId == currentUser?.uid) {
            binding.btnDeleteRecipe.visibility = View.VISIBLE
            binding.btnUpdateRecipe.visibility = View.VISIBLE
            binding.btnFollow.visibility = View.GONE
        }
        Picasso.get().load(recipe.image).into(binding.imageRecipe)
        binding.toolbarLayout.title = recipe.name
        binding.toolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, R.color.white))
        binding.bottomAppBar.setNavigationIconTint(ContextCompat.getColor(this, R.color.white))
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, IngredientsFragment(recipe.ingredients))
            .commit()
    }

    private fun createDetailsUser() {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("id", recipe.userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val user = querySnapshot.documents[0].toObject(User::class.java)!!
                    binding.userName.text = user.username
                    Picasso.get().load(user.photoUrl).into(binding.userPhoto)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "FirestoreError",
                    "Error al cargar los datos asociados a la receta: ${exception.message}"
                )
            }
    }

    private fun checkEnabledButtonFollow () {
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        FirebaseFirestore.getInstance().collection("followers")
            .whereEqualTo("followerId", uid)
            .whereEqualTo("followedId", recipe.userId)
            .whereEqualTo("recipeId", recipe.id)
            .get()
            .addOnSuccessListener { task ->
                if (!task.isEmpty) {
                    disableButtonFollow()
                }
            }
            .addOnFailureListener { e ->
                showToast("Error: ${e.message}")
                finish()
            }
    }

    private fun disableButtonFollow() {
        with( binding.btnFollow) {
            isEnabled = false
            isActivated = false
            setBackgroundResource(R.drawable.search_background)
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}