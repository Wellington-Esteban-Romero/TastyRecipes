package com.tasty.recipes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.tasty.recipes.databinding.ActivityLoginBinding
import com.tasty.recipes.utils.AuthHelper
import com.tasty.recipes.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authHelper: AuthHelper = AuthHelper()
    private lateinit var googleSingIn: GoogleSingIn

    companion object {
        lateinit var sessionManager: SessionManager
        private const val TAG = "EmailPassword"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(applicationContext)

        initUI()
        initListener()
    }

    private fun initUI () {
        googleSingIn = GoogleSingIn(this)
    }

    private fun initListener() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validate(email, password)) {
                authHelper.getFirebaseAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:success")
                            val user = authHelper.getCurrentUser()
                            user?.let {
                                checkEmailVerification()
                                sessionManager.saveLastProvider(this, "password")
                            }
                        } else {
                            Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateUI(null)
                        }
                    }
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //sing in with red socials
        binding.btnLoginGoogle.setOnClickListener {
            sessionManager.saveLastProvider(this, "google.com")
            CoroutineScope(Dispatchers.Main).launch {
                if (googleSingIn.signIn()) {
                    val currentUser = authHelper.getCurrentUser()
                    updateUI(currentUser)
                }
            }
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

            sessionManager.saveUserEmail(this, user.email ?: "")
            val intent = Intent(this, MainActivity::class.java)
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

    private fun checkEmailVerification() {
        val user = authHelper.getCurrentUser()
        if (user != null) {
            user.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    if (user.isEmailVerified) {
                        Log.i(TAG, "El usuario ha verificado su email.")
                        updateUI(user)
                    } else {
                        Log.i(TAG, "El email no está verificado.")
                        Toast.makeText(this, "Por favor, verifica tu email para continuar.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e(TAG, "Error al recargar el usuario: ${reloadTask.exception?.message}")
                    Toast.makeText(this, "Error al comprobar la verificación.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e(TAG, "Usuario no autenticado.")
            Toast.makeText(this, "Debes iniciar sesión para verificar el email.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun validate(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.etFieldEmail.error = "Ingresa un correo electrónico"
            isValid = false
        } else if (!email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
            binding.etFieldEmail.error = "Ingresa un correo electrónico válido"
            isValid = false
        } else {
            binding.etFieldEmail.error = null
        }

        if (password.isEmpty()) {
            binding.etFieldPassword.error = "Ingresa una contraseña"
            isValid = false
        } else {
            binding.etFieldPassword.error = null
        }

        if (password.length < 6) {
            binding.etFieldPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }
        return isValid
    }
}