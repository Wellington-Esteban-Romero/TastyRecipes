package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.tasty.recipes.databinding.ActivityLoginBinding
import com.tasty.recipes.utils.AuthHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authHelper: AuthHelper = AuthHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authHelper.signIn(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = authHelper.getCurrentUser()
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        val currentUser = authHelper.getCurrentUser()
        currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Usuario recargado exitosamente.", Toast.LENGTH_SHORT).show()
                // Puedes usar la información actualizada del usuario aquí
                updateUI(currentUser)
            } else {
                Toast.makeText(
                    this,
                    "Error al recargar usuario: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "Bienvenido ${user.email}", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USEREMAIL", user.email)
            startActivity(intent)

            finish()
        } else {
            Toast.makeText(
                this,
                "Por favor, verifica tus credenciales e inténtalo de nuevo.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}