package com.tasty.recipes.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityRecipeDetailBinding
import com.tasty.recipes.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


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
            menu?.findItem(R.id.action_favorite)?.setIcon(R.drawable.ic_favorite)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println(item.icon)
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

    private fun createDetails (recipe: Recipe) {
        Picasso.get().load(recipe.image).into(binding.imageRecipe)
        binding.toolbar.title = recipe.name
        binding.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        //binding.txtInstructions.text = recipes[0].instructions.split(", ").joinToString("\n\n")

    }
}