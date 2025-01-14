package com.tasty.recipes.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ActivityResetPasswordBinding


class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        binding.btnResetPassword.setOnClickListener {

            val email = binding.email.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your registered email id", Toast.LENGTH_SHORT)
                    .show();
            } else {
                auth!!.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "We have sent you instructions to reset your password!",
                                Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                this,
                                "Failed to send reset email!",
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            }
        }

    }
}