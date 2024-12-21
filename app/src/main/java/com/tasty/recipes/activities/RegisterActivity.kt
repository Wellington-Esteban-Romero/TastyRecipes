package com.tasty.recipes.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tasty.recipes.R
import com.tasty.recipes.databinding.ActivityRegisterBinding
import com.tasty.recipes.utils.AuthHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authHelper: AuthHelper = AuthHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateUser.setOnClickListener {
            val username = binding.etPasswordRegister.text.toString()
            val email =  binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()

            if (validate(username, email, password)) {
                createAccount(email, password)
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        authHelper.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = authHelper.getCurrentUser()
                    Toast.makeText(this, "Cuenta creada: ${user?.email}", Toast.LENGTH_SHORT).show()
                } else {
                    // Si el registro falla, muestra un mensaje al usuario
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validate(username: String, email: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.etUserNameRegister.error = "ingresa un nombre de usuario"
            Toast.makeText(this, "Por favor ingresa un nombre de usuario", Toast.LENGTH_SHORT).show()
            isValid = false
        } else {
            binding.etUserNameRegister.error = null
        }

        if (email.isEmpty()) {
            binding.etEmailRegister.error = "Ingresa un correo electrónico"
            Toast.makeText(this, "Por favor ingresa un correo electrónico", Toast.LENGTH_SHORT).show()
            isValid = false
        } else {
            binding.etEmailRegister.error = null
        }

        if (password.isEmpty()) {
            binding.etPasswordRegister.error = "Ingresa una contraseña"
            Toast.makeText(this, "Por favor ingresa una contraseña", Toast.LENGTH_SHORT).show()
            isValid = false
        } else {
            binding.etEmailRegister.error = null
        }

        if (password.length < 6) {
            binding.etPasswordRegister.error = "La contraseña debe tener al menos 6 caracteres"
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }
}