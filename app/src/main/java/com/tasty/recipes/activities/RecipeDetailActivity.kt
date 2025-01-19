package com.tasty.recipes.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        session = SessionManager(applicationContext)

        initUI()
        initListener()
    }

    private fun initUI() {
        idRecipe = intent.getStringExtra(EXTRA_RECIPE_ID).orEmpty()
        loadRecipeById()
    }

    private fun initListener() {
        setupToolBarListeners()
        setupBottomNavigationView()
        setupDeleteRecipe()
    }

    private fun loadRecipeById() {
        FirebaseFirestore.getInstance().collection("recipes")
            .whereEqualTo("id", idRecipe.toLong())
            .get()
            .addOnSuccessListener { querySnapshot ->
                recipe = querySnapshot.documents[0].toObject(Recipe::class.java)!!
                createDetails()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun setupToolBarListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupBottomNavigationView() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_ingredients -> selectedFragment = IngredientsFragment(recipe.ingredients)
                R.id.nav_steps -> selectedFragment = StepsFragment(recipe.instructions.split("."))
            }

            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }
    }

    private fun setupDeleteRecipe () {
        binding.btnDeleteRecipe.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.alert_dialog_delete_title)
                .setMessage(R.string.alert_dialog_delete_message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    // Borramos la tarea en caso de pulsar el boton OK
                    println("Se ha borrado la siguiente receta: " )
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setIcon(android.R.drawable.ic_delete)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.recipe_toolbar_menu, menu)
        if (session.isFavorite(idRecipe))
            menu?.findItem(R.id.action_favorite)?.setIcon(R.drawable.ic_favorite)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_favorite -> {
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
        Picasso.get().load(recipe.image).into(binding.imageRecipe)
        binding.toolbar.title = recipe.name
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, IngredientsFragment(recipe.ingredients))
            .commit()
    }
}