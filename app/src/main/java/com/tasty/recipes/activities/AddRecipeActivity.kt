package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tasty.recipes.R
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityAddRecipeBinding

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var recipe: Recipe
    var isEditing: Boolean = false

    companion object {
        const val EXTRA_RECIPE_CREATE_TAG_ID = "RECIPE_CREATE_TAG_ID"
        const val EXTRA_IS_DETAILS = "IS_DETAILS"
    }

    // Registrar el launcher para seleccionar imágenes
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this.contentResolver.takePersistableUriPermission(uri, flag)
            recipe.image = uri.toString()
            Toast.makeText(this, "Se ha cargado la imagen correctamente", Toast.LENGTH_SHORT).show()
            // Mostrar la imagen en un ImageView (opcional)
            //binding.imageViewSelected.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        initListener()
    }

    private fun init () {

        recipeDAO = RecipeDAO(this)

        recipe =  Recipe(-1,"")

        loadData()
    }

    private fun initListener () {

        binding.buttonSaveRecipe.setOnClickListener {
            saveRecipe()
        }

        binding.btnSelectImage.setOnClickListener {
            if(ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

   private fun loadData() {
        binding.editTextTitle.setText(recipe.name)
        binding.editTextIngredients.setText(recipe.ingredients.joinToString { "," })
        binding.editTextInstructions.setText(recipe.instructions)
        binding.editTextPrepTime.setText(recipe.prepTimeMinutes.toString())
        binding.editTextCookTime.setText(recipe.cookTimeMinutes.toString())
        binding.editTextServings.setText(recipe.servings)
        binding.editTextDifficulty.setText(recipe.difficulty)
        binding.imageViewSelected.setImageURI(recipe.image.toUri())
        /* if (!isEditing) {
            binding.btnSaveRecipe.text = "Save Recipe"
        } else {
            binding.btnSaveRecipe.text = "Edit Recipe"
        }*/ }


    private fun validateRecipe(): Boolean {
        var isValid = true

        if (recipe.name.trim().isEmpty()) {
            binding.textFieldTitleName.error = "Write something"
            isValid = false
        } else {
            binding.textFieldTitleName.error = null
        }
        if (recipe.name.length > 50) {
            binding.textFieldTitleName.error = "Is over 50 characters"
            isValid = false
        } else {
            binding.textFieldTitleName.error = null
        }

        if (recipe.ingredients.isEmpty()) {
            binding.textFieldIngredients.error = "Write the ingredients"
            isValid = false
        } else {
            binding.textFieldIngredients.error = null
        }

        if (recipe.instructions.isEmpty()) {
            binding.textFieldInstructions.error = "Write the instructions"
            isValid = false
        } else {
            binding.textFieldInstructions.error = null
        }

        if (recipe.prepTimeMinutes <= 0) {
            binding.textFieldPrepTime.error = "Preparation time must be greater than 0"
            isValid = false
        } else {
            binding.textFieldPrepTime.error = null
        }

        if (recipe.cookTimeMinutes <= 0) {
            binding.textFieldCookTime.error = "Cooking time must be greater than 0"
            isValid = false
        } else {
            binding.textFieldCookTime.error = null
        }

        if (recipe.servings.toInt() <= 0) {
            binding.textFieldServings.error = "Servings must be greater than 0"
            isValid = false
        } else {
            binding.textFieldServings.error = null
        }

        if (recipe.difficulty.trim().isEmpty()) {
            binding.textFieldDifficulty.error = "Select a difficulty"
            isValid = false
        } else {
            binding.textFieldDifficulty.error = null
        }

        if (recipe.image.trim().isEmpty()) {
            binding.btnSelectImage.error = "Provides a valid image"
            isValid = false
        } else {
            binding.btnSelectImage.error = null
        }
        return isValid
    }


    private fun saveRecipe() {
        recipe.name = binding.textFieldTitleName.editText?.text.toString()
        recipe.ingredients = listOf(binding.textFieldIngredients.editText?.text.toString())
        recipe.instructions = binding.textFieldInstructions.editText?.text.toString()
        if (binding.textFieldPrepTime.editText?.text.toString().isEmpty()){
            recipe.prepTimeMinutes = 0
        } else {
            recipe.prepTimeMinutes = binding.textFieldPrepTime.editText?.text.toString().toInt()
        }
        if (binding.textFieldCookTime.editText?.text.toString().isEmpty()){
            recipe.cookTimeMinutes = 0
        } else {
            recipe.cookTimeMinutes = binding.textFieldCookTime.editText?.text.toString().toInt()
        }
        if (binding.textFieldServings.editText?.text.toString().isEmpty()){
            recipe.servings = 0
        } else {
            recipe.servings = binding.textFieldServings.editText?.text.toString().toInt()
        }
        recipe.difficulty = binding.textFieldDifficulty.editText?.text.toString()

       /* if (!isEditing) {
            recipe.category = intent.getStringExtra(EXTRA_RECIPE_CREATE_TAG_ID).orEmpty()
        }*/

        if (validateRecipe()) {
            recipeDAO.insert(recipe)
            finish()
        }
    }
}