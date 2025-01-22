package com.tasty.recipes.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tasty.recipes.R
import com.tasty.recipes.adapters.AddCategoryAdapter
import com.tasty.recipes.adapters.AddIngredientAdapter
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RecipeDAO
import com.tasty.recipes.databinding.ActivityAddRecipeBinding

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var addRecipeIngredientsAdapter: AddIngredientAdapter
    private lateinit var addRecipeCategoriesAdapter: AddCategoryAdapter
    private val ingredientsList = mutableListOf<String>()
    private val categoryList = mutableListOf<Category>()
    private var selectedCategories = mutableListOf<String>()
    private lateinit var recipeDAO: RecipeDAO
    private lateinit var recipe: Recipe
    var isEditing: Boolean = false

    companion object {
        const val EXTRA_RECIPE_CREATE_TAG_ID = "RECIPE_CREATE_TAG_ID"
        const val EXTRA_IS_DETAILS = "IS_DETAILS"
        var SELECTED_CATEGORIES = mutableListOf<Category>()
    }

    // Registrar el launcher para seleccionar imágenes
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(uri, flag)
                recipe.image = uri.toString()
                Toast.makeText(this, "Se ha cargado la imagen correctamente", Toast.LENGTH_SHORT)
                    .show()
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

    private fun initUI() {

        recipeDAO = RecipeDAO(this)

        recipe = Recipe(-1, "")

        loadData()
    }

    private fun initListener() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonSelectCategories.setOnClickListener {
            val categoryNames = categoryList.map { it.name }.toTypedArray()
            val checkedItems = BooleanArray(categoryList.size)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Categories")
                .setMultiChoiceItems(
                    categoryNames,
                    checkedItems
                ) { _, which, isChecked ->
                    if (isChecked) {
                        if (!selectedCategories.contains(categoryList[which].name)) {
                            selectedCategories.add(categoryList[which].name)
                        }
                    } else {
                        selectedCategories.remove(selectedCategories[which])
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    addRecipeCategoriesAdapter.notifyItemInserted(selectedCategories.size - 1)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            //startActivity(Intent(this, CategorySelectionActivity::class.java))
        }

        binding.buttonSelectImage.setOnClickListener {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.buttonAddIngredient.setOnClickListener {
            val ingredient = binding.editTextIngredient.text.toString()
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
        loadCategories()
    }


    private fun setupRecyclerView() {
        addRecipeIngredientsAdapter = AddIngredientAdapter(ingredientsList) { pos ->
            addRecipeIngredientsAdapter.removeIngredient(pos)
        }

        addRecipeCategoriesAdapter = AddCategoryAdapter(selectedCategories) { pos ->
            addRecipeCategoriesAdapter.removeCategory(pos)
        }


        binding.rvAddIngredients.apply {
            layoutManager = LinearLayoutManager(this@AddRecipeActivity)
            adapter = addRecipeIngredientsAdapter
        }

        binding.rvSelectedCategories.apply {
            layoutManager =
                LinearLayoutManager(this@AddRecipeActivity, LinearLayoutManager.HORIZONTAL, false)
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
        }*/
    }

    private fun loadCategories() {
        FirebaseFirestore.getInstance().collection("categories").get()
            .addOnSuccessListener { querySnapshot ->
                categoryList.clear()

                querySnapshot.forEach { document ->
                    val category = document.toObject(Category::class.java)
                    categoryList.add(category)
                }
                addRecipeCategoriesAdapter.notifyItemInserted(categoryList.size - 1)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar categorías: ${exception.message}")
            }
    }

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

        /*if (recipe.categoryIds.isEmpty()) {
            binding.buttonSelectCategories.error = "Choose a category"
            isValid = false
        } else {
            binding.buttonSelectCategories.error = null
        }

        if (recipe.image.trim().isEmpty()) {
            binding.buttonSelectImage.error = "Provides a valid image"
            isValid = false
        } else {
            binding.buttonSelectImage.error = null
        }*/
        return isValid
    }


    private fun saveRecipe() {
        recipe.name = binding.textFieldTitleName.editText?.text.toString()
        recipe.ingredients = ingredientsList
        recipe.instructions = binding.textFieldInstructions.editText?.text.toString()
        if (binding.textFieldPrepTime.editText?.text.toString().isEmpty()) {
            recipe.prepTimeMinutes = 0
        } else {
            recipe.prepTimeMinutes = binding.textFieldPrepTime.editText?.text.toString().toInt()
        }
        if (binding.textFieldCookTime.editText?.text.toString().isEmpty()) {
            recipe.cookTimeMinutes = 0
        } else {
            recipe.cookTimeMinutes = binding.textFieldCookTime.editText?.text.toString().toInt()
        }
        if (binding.textFieldServings.editText?.text.toString().isEmpty()) {
            recipe.servings = 0
        } else {
            recipe.servings = binding.textFieldServings.editText?.text.toString().toInt()
        }
        recipe.difficulty = binding.spinnerDifficulty.selectedItem.toString()

        recipe.categoryIds = categoryList.filter {
            selectedCategories.contains(it.name)
        }.map { it.id }
            .toList()

        recipe.userId = FirebaseAuth.getInstance().currentUser!!.uid

        /* if (!isEditing) {
             recipe.category = intent.getStringExtra(EXTRA_RECIPE_CREATE_TAG_ID).orEmpty()
         }*/

        if (validateRecipe()) {
            //recipeDAO.insert(recipe)
            FirebaseFirestore.getInstance().collection("recipes")
                .add(recipe)
                .addOnSuccessListener {
                    Toast.makeText(this, "Recipe added with ID: 3", Toast.LENGTH_SHORT).show();
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding recipe: " + e.message, Toast.LENGTH_SHORT)
                        .show();
                }
            finish()
        }
    }

    private fun getTokenFromFirebase() {
        /*FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        }*/
    }

    override fun finish() {
        super.finish()
        SELECTED_CATEGORIES = mutableListOf()
    }
}