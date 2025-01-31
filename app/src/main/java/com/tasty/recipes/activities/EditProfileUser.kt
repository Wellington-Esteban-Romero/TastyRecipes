package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ActivityEditProfileUserBinding

class EditProfileUser : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileUserBinding

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(uri, flag)
                binding.profilePicture.setImageURI(uri)
                showToast("Se ha cargado la imagen correctamente")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        showImageProfile()

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editProfilePicture.setOnClickListener {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun showImageProfile() {
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("users")

        userCollection.whereEqualTo("email", FirebaseAuth.getInstance().currentUser?.email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    println(document.data["photoUrl"] as String)
                    Picasso.get()
                        .load(document.data["photoUrl"].toString())
                        .into(binding.profilePicture)
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}