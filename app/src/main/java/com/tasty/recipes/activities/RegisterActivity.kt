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
            binding.etFieldUserNameRegister.error = "ingresa un nombre de usuario"
            isValid = false
        } else {
            binding.etFieldUserNameRegister.error = null
        }

        if (email.isEmpty()) {
            binding.etFieldEmailRegister.error = "Ingresa un correo electrónico"
            isValid = false
        } else {
            binding.etFieldEmailRegister.error = null
        }

        if (password.isEmpty()) {
            binding.etFieldPasswordRegister.error = "Ingresa una contraseña"
            isValid = false
        } else {
            binding.etFieldPasswordRegister.error = null
        }

        if (password.length < 6) {
            binding.etFieldPasswordRegister.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }
        return isValid
    }
}