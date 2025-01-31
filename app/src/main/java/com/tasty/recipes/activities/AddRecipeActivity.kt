package com.tasty.recipes.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.adapters.AddCategoryAdapter
import com.tasty.recipes.adapters.AddIngredientAdapter
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Message
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.providers.RetrofitProvider
import com.tasty.recipes.databinding.ActivityAddRecipeBinding
import com.tasty.recipes.utils.Difficulty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var addRecipeIngredientsAdapter: AddIngredientAdapter
    private lateinit var addRecipeCategoriesAdapter: AddCategoryAdapter
    private var ingredientsList = mutableListOf<String>()
    private val categoryList = mutableListOf<Category>()
    private var selectedCategories = mutableListOf<String>()
    private lateinit var recipe: Recipe
    private var isEditing: Boolean = false

    companion object {
        const val EXTRA_RECIPE_CREATE_TAG_ID = "RECIPE_CREATE_TAG_ID"
        const val EXTRA_UPDATE_TAG_ID = "EXTRA_UPDATE_TAG_ID"
    }

    // Registrar el launcher para seleccionar imágenes
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(uri, flag)
                recipe.image = uri.toString()
                binding.imageViewSelected.setImageURI(uri)
                binding.imageViewSelected.visibility = View.VISIBLE
                showToast("Se ha cargado la imagen correctamente")
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
        val id = intent.getStringExtra(EXTRA_UPDATE_TAG_ID)

        if (!id.isNullOrEmpty()) {
            isEditing = true
            loadRecipeById(id)
        } else {
            isEditing = false
            recipe = Recipe("", "")
            loadData()
        }
        loadCategories()
        setupRecyclerView()
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
                        selectedCategories.remove(categoryList[which].name)
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    addRecipeCategoriesAdapter.notifyItemInserted(selectedCategories.size - 1)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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
        binding.editTextInstructions.setText(recipe.instructions)
        binding.editTextPrepTime.setText(recipe.prepTimeMinutes.toString())
        binding.editTextCookTime.setText(recipe.cookTimeMinutes.toString())
        binding.editTextServings.setText(recipe.servings.toString())
        Picasso.get()
            .load(recipe.image.toUri())
            .into(binding.imageViewSelected)
        binding.imageViewSelected.visibility = View.VISIBLE
        if (!isEditing) {
            binding.editTextIngredient.setText(recipe.ingredients.joinToString { "," })
            binding.buttonSaveRecipe.text = "Save Recipe"
        } else {
            addRecipeIngredientsAdapter.updateIngredients(recipe.ingredients.toMutableList())

            val difficulty = Difficulty.entries.filter { it.name == recipe.difficulty.uppercase() }
                .map { it }
                .toTypedArray()
            binding.spinnerDifficulty.setSelection(difficulty[0].ordinal)

            addRecipeCategoriesAdapter.updateCategories(categoryList.filter { // ver categoryList
                recipe.categoryIds.contains(it.id)
            }.map { it.name }
                .toMutableList()
            )
            binding.buttonSaveRecipe.text = "Edit Recipe"
        }
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

        if (recipe.categoryIds.isEmpty()) {
            binding.buttonSelectCategories.error = "Choose a category"
            isValid = false
        } else {
            binding.buttonSelectCategories.error = null
        }

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

        if (recipe.image.isEmpty()) {
            recipe.image = getResources().getResourceName(R.drawable.photo_recipe_default)
        }

        recipe.userId = FirebaseAuth.getInstance().currentUser!!.uid

        if (validateRecipe()) {
            val firestore = FirebaseFirestore.getInstance()
            if (!isEditing) {
                firestore.collection("recipes")
                    .add(recipe)
                    .addOnSuccessListener { documentReference ->
                        val refID = documentReference.id
                        recipe.id = refID

                        firestore.collection("recipes")
                            .document(refID)
                            .update("id", refID)
                            .addOnSuccessListener {
                                val imageUri = recipe.image.toUri()
                                uploadImageToStorage(imageUri, refID) { imageUrl ->
                                    if (imageUrl != null) {
                                        saveImageFromStorage(imageUrl, refID)
                                    } else {
                                        showToast("Error uploading image.")
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                showToast("Error update ID in recipe: " + e.message)
                            }
                    }
                    .addOnFailureListener { e ->
                        showToast("Error adding recipe: " + e.message)
                    }
            } else {
                firestore.collection("recipes")
                    .document(recipe.id)
                    .update(recipe.toMap())
                    .addOnSuccessListener {
                        showToast("Recipe updated successfully")
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showToast("Error updating recipe: ${e.message}")
                    }
            }
        }
    }

    private fun uploadImageToStorage(imageUri: Uri, refID: String, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("recipes/$refID.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        callback(downloadUrl.toString())
                    }
                    .addOnFailureListener { _ ->
                        callback(null)
                    }
            }
            .addOnFailureListener { _ ->
                callback(null)
            }
    }

    private fun saveImageFromStorage(imageUrl: String, refID: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("recipes")
            .document(refID)
            .update("image", imageUrl)
            .addOnSuccessListener {
                showToast("Recipe added successfully")
                clearFieldsFormRecipe()
            }
            .addOnFailureListener { e ->
                showToast("Error updating recipe image URL: ${e.message}")
            }
    }

    private fun clearFieldsFormRecipe() {
        binding.textFieldTitleName.editText?.setText("")
        binding.textFieldIngredient.editText?.setText("")
        binding.textFieldInstructions.editText?.setText("")
        binding.textFieldPrepTime.editText?.setText("0")
        binding.textFieldCookTime.editText?.setText("0")
        binding.textFieldServings.editText?.setText("0")
        binding.spinnerDifficulty.setSelection(0)
        binding.imageViewSelected.setImageURI(null)
        addRecipeCategoriesAdapter.updateCategories(mutableListOf())
        addRecipeIngredientsAdapter.updateIngredients(mutableListOf())
    }

    private fun loadRecipeById(idRecipe: String) {
        FirebaseFirestore.getInstance().collection("recipes")
            .whereEqualTo("id", idRecipe)
            .get()
            .addOnSuccessListener { querySnapshot ->
                recipe = querySnapshot.documents[0].toObject(Recipe::class.java)!!
                loadData()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error al cargar recipes: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /*private fun getTokenFromFirebaseAndSendNotification() {
        FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            CoroutineScope(Dispatchers.IO).launch {
                val xx = RetrofitProvider.getRetrofit().sendNotificationService(
                    Message("Receta 1","Receta nueva", token)
                )
                println(xx)
            }

            // Log and toast
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        }
    }*/
}