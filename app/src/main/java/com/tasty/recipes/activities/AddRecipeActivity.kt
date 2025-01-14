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
import androidx.recyclerview.widget.LinearLayoutManager
import com.tasty.recipes.R
import com.tasty.recipes.adapters.AddRecipeAdapter
import com.tasty.recipes.adapters.SelectedCategoriesAdapter
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityAddRecipeBinding

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var addRecipeIngredientsAdapter: AddRecipeAdapter
    private lateinit var addRecipeCategoriesAdapter: AddRecipeAdapter
    private val ingredientsList = mutableListOf<String>()
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var recipe: Recipe
    var isEditing: Boolean = false
    private var hasCategoriesBeenUpdated = false


    companion object {
        const val EXTRA_RECIPE_CREATE_TAG_ID = "RECIPE_CREATE_TAG_ID"
        const val EXTRA_IS_DETAILS = "IS_DETAILS"
        var SELECTED_CATEGORIES = mutableListOf<Category>()
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
        initUI()
        initListener()
    }

    private fun initUI () {

        recipeDAO = RecipeDAO(this)

        recipe =  Recipe(-1,"")

        loadData()
    }

    private fun initListener () {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }

        binding.buttonSelectCategories.setOnClickListener{
            startActivity(Intent(this, CategorySelectionActivity::class.java))
        }

        binding.buttonSelectImage.setOnClickListener {
            if(ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.buttonAddIngredient.setOnClickListener {
            val ingredient =  binding.editTextIngredient.text.toString()
            if (ingredient.isNotEmpty()) {
                ingredientsList.add(ingredient)
                addRecipeIngredientsAdapter.notifyItemInserted(ingredientsList.size - 1)
                binding.editTextIngredient.text?.clear()
            }
        }

        binding.buttonSaveRecipe.setOnClickListener {
            saveRecipe()
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        if (!hasCategoriesBeenUpdated) {
            var selectedCategories = intent.extras?.getStringArrayList("selectedCategories")
            if (!selectedCategories.isNullOrEmpty()) {
                SELECTED_CATEGORIES = selectedCategories.map { Category(0, it, "") }.toMutableList()
            } else {
                selectedCategories = arrayListOf()
            }
            addRecipeCategoriesAdapter.updateCategories(selectedCategories)
            addRecipeCategoriesAdapter.notifyItemInserted(SELECTED_CATEGORIES.size - 1)
            hasCategoriesBeenUpdated = true

        } else {
            finish()
        }
    }

    private fun setupRecyclerView() {
        addRecipeIngredientsAdapter = AddRecipeAdapter(ingredientsList) { pos ->
            addRecipeIngredientsAdapter.removeIngredient(pos)
        }

        addRecipeCategoriesAdapter = AddRecipeAdapter(SELECTED_CATEGORIES.map { it.name }.toMutableList()) { _ -> }


        binding.rvAddIngredients.apply {
            layoutManager = LinearLayoutManager(this@AddRecipeActivity)
            adapter = addRecipeIngredientsAdapter
        }

        binding.rvSelectedCategories.apply {
            layoutManager = LinearLayoutManager(this@AddRecipeActivity)
            adapter = addRecipeCategoriesAdapter
        }
    }

   private fun loadData() {
        binding.editTextTitle.setText(recipe.name)
        binding.editTextIngredient.setText(recipe.ingredients.joinToString { "," })
        binding.editTextInstructions.setText(recipe.instructions)
        binding.editTextPrepTime.setText(recipe.prepTimeMinutes.toString())
        binding.editTextCookTime.setText(recipe.cookTimeMinutes.toString())
        binding.editTextServings.setText(recipe.servings.toString())
        //binding.editTextDifficulty.setText(recipe.difficulty)
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
        } else if (recipe.name.length > 50) {
            binding.textFieldTitleName.error = "Is over 50 characters"
            isValid = false
        } else {
            binding.textFieldTitleName.error = null
        }

        if (recipe.ingredients.isEmpty()) {
            binding.textFieldIngredient.error = "Write the ingredients"
            isValid = false
        } else {
            binding.textFieldIngredient.error = null
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

        if (recipe.servings <= 0) {
            binding.textFieldServings.error = "Servings must be greater than 0"
            isValid = false
        } else {
            binding.textFieldServings.error = null
        }

        if (recipe.image.trim().isEmpty()) {
            binding.buttonSelectImage.error = "Provides a valid image"
            isValid = false
        } else {
            binding.buttonSelectImage.error = null
        }
        return isValid
    }


    private fun saveRecipe() {
        recipe.name = binding.textFieldTitleName.editText?.text.toString()
        recipe.ingredients = ingredientsList
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
        recipe.difficulty = binding.spinnerDifficulty.selectedItem.toString()

       /* if (!isEditing) {
            recipe.category = intent.getStringExtra(EXTRA_RECIPE_CREATE_TAG_ID).orEmpty()
        }*/

        if (validateRecipe()) {
            recipeDAO.insert(recipe)
            finish()
        }
    }

    override fun finish() {
        super.finish()
        SELECTED_CATEGORIES = mutableListOf()
    }
}