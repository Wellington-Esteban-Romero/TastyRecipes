package com.tasty.recipes.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
}