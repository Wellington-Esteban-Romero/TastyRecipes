package com.tasty.recipes.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.tasty.recipes.data.entities.User
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
            val username = binding.etUserNameRegister.text.toString()
            val email =  binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()
            val repeatPassword = binding.etRepeatPasswordRegister.text.toString()

            val user = User(username, email, password, repeatPassword)

            if (validate(user)) {
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

    private fun validate(user: User): Boolean {
        var isValid = true

        if (user.username.isEmpty()) {
            binding.etFieldUserNameRegister.error = "ingresa un nombre de usuario"
            isValid = false
        } else {
            binding.etFieldUserNameRegister.error = null
        }

        if (user.email.isEmpty()) {
            binding.etFieldEmailRegister.error = "Ingresa un correo electrónico"
            isValid = false
        } else if (!user.email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
            binding.etFieldEmailRegister.error = "Ingresa un correo electrónico válido"
            isValid = false
        } else {
            binding.etFieldEmailRegister.error = null
        }

        if (user.password.isEmpty()) {
            binding.etFieldPasswordRegister.error = "Ingresa una contraseña"
            isValid = false
        } else {
            binding.etFieldPasswordRegister.error = null
        }

        if (user.password.length < 6) {
            binding.etFieldPasswordRegister.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (user.repeatPassword.isEmpty()) {
            binding.etFieldRepeatPasswordRegister.error = "Repite la contraseña"
            isValid = false
        } else {
            binding.etFieldRepeatPasswordRegister.error = null
        }

        if (user.password != user.repeatPassword) {
            binding.etFieldRepeatPasswordRegister.error = "Las contraseñas no coinciden"
            isValid = false
        }
        return isValid
    }
}