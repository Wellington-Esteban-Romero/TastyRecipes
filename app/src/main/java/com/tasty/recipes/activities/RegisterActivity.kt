package com.tasty.recipes.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tasty.recipes.data.entities.User
import com.tasty.recipes.databinding.ActivityRegisterBinding
import com.tasty.recipes.utils.AuthHelper
import java.io.File

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authHelper: AuthHelper = AuthHelper()
    private lateinit var register_user:User
    private var photoUrl:String = ""

    companion object {
        private const val TAG = "Register"
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(uri, flag)

                val localFile = copyUriToFile(uri)
                if (localFile != null) {
                    photoUrl = localFile.toString()
                    Picasso.get()
                        .load(localFile)
                        .into(binding.ivSelectedImage)

                    Toast.makeText(this, "Imagen cargada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ImagePicker", "Error al procesar la imagen: ${e.message}", e)
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener () {

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCreateUser.setOnClickListener {
            val username = binding.etUserNameRegister.text.toString()
            val email =  binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()
            val repeatPassword = binding.etRepeatPasswordRegister.text.toString()

            register_user = User("", username, email, password, repeatPassword)
            register_user.photoUrl = this.photoUrl

            if (validate(register_user))
                createAccount(register_user.email, register_user.password)
        }

        binding.btnSelectImage.setOnClickListener {
            if(ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun createAccount(email: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("users")
        userCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    authHelper.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                user?.let {
                                    sendVerificationEmail(it)
                                    Toast.makeText(this, "Verifica tu email antes de continuar.", Toast.LENGTH_LONG).show()

                                    val userData = createUserDataMap(user)
                                    saveUserToFirestore(user.uid, userData, user)
                                }
                            } else {
                                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }  else {
                    Toast.makeText(this, "Este email ya está registrado.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendVerificationEmail(user: FirebaseUser) {
        authHelper.getCurrentUser()?.sendEmailVerification()
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(TAG, "VerificationSent")
                } else {
                    Log.e(TAG, "Failed to send verification email: ${it.exception?.message}")
                }
            }
    }

    private fun createUserDataMap(user: FirebaseUser): MutableMap<String, Any> {
        return mutableMapOf(
            "id" to user.uid,
            "username" to this.register_user.username,
            "email" to user.email!!,
            "photoUrl" to this.register_user.photoUrl,
            "createdAt" to System.currentTimeMillis()
        )
    }

    private fun saveUserToFirestore(userId: String, userData: Map<String, Any>, user: FirebaseUser) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("Firestore", "Usuario añadido correctamente")
                signOutAndRedirectToLogin()
            }
            .addOnFailureListener { exception ->
                handleFirestoreFailure(exception, user)
            }
    }

    private fun handleFirestoreFailure(exception: Exception, user: FirebaseUser) {
        user.delete().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Log.e("Firestore", "Error al guardar usuario, Auth revertido", exception)
            } else {
                Log.e(
                    "Firestore",
                    "Error al guardar usuario y fallo al eliminar de Auth",
                    deleteTask.exception
                )
            }
        }
    }

    private fun signOutAndRedirectToLogin() {
        val currentUser = authHelper.getCurrentUser()
        if (currentUser != null) {
            authHelper.getFirebaseAuth().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun copyUriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error al copiar URI a archivo: ${e.message}", e)
            null
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