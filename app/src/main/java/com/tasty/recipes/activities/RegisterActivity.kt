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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.tasty.recipes.data.entities.User
import com.tasty.recipes.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val storageRef = FirebaseStorage.getInstance().reference
    private var photoUrl: String = ""

    companion object {
        private const val TAG = "Register"
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    this.contentResolver.takePersistableUriPermission(uri, flag)
                    uploadImage(uri)
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

    private fun initListener() {

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCreateUser.setOnClickListener {
            val username = binding.etEmailRegister.text.toString()
            val email = binding.etPasswordRegister.text.toString()

            if (validateData()) {
                createAccount(username, email)
            }
        }

        binding.btnSelectImage.setOnClickListener {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun createAccount(email: String, password: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.let {
                        val userData = createUserDataMap(user)
                        saveUserToFirestore(user.uid, userData, user)

                        sendVerificationEmail(it)
                        Toast.makeText(
                            this,
                            "Verifica tu email antes de continuar.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun sendVerificationEmail(user: FirebaseUser) {
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
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
            "username" to binding.etUserNameRegister.text.toString(),
            "email" to binding.etEmailRegister.text.toString(),
            "photoUrl" to this.photoUrl,
            "createdAt" to System.currentTimeMillis()
        )
    }

    private fun saveUserToFirestore(
        userId: String,
        userData: Map<String, Any>,
        user: FirebaseUser
    ) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("Firestore", "Usuario añadido correctamente")
                if (userData["photoUrl"].toString().isNotEmpty())
                    saveImageUriToDatabase(userData["photoUrl"].toString())
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
        val currentUser = FirebaseAuth.getInstance()
        if (currentUser.currentUser != null) {
            currentUser.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val fileRef = storageRef.child("profile_pictures/image_${System.currentTimeMillis()}.web")
        val uploadTask = fileRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri)
                    .into(binding.ivSelectedImage)
                Toast.makeText(this, "Imagen cargada correctamente", Toast.LENGTH_SHORT)
                    .show()
                this.photoUrl = uri.toString()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUriToDatabase(downloadUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        FirebaseDatabase.getInstance().reference.child("users").child(userId).child("photoUrl").setValue(downloadUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    photoUrl = downloadUrl
                    Picasso.get()
                        .load(downloadUrl)
                        .into(binding.ivSelectedImage)
                } else {
                    Toast.makeText(this, "Error al guardar URL", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateData(): Boolean {
        var isValid = true

        if (binding.etUserNameRegister.text.toString().isEmpty()) {
            binding.etFieldUserNameRegister.error = "ingresa un nombre de usuario"
            isValid = false
        } else {
            binding.etFieldUserNameRegister.error = null
        }

        if (binding.etEmailRegister.text.toString().isEmpty()) {
            binding.etFieldEmailRegister.error = "Ingresa un correo electrónico"
            isValid = false
        } else if (!binding.etEmailRegister.text.toString().matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
            binding.etFieldEmailRegister.error = "Ingresa un correo electrónico válido"
            isValid = false
        } else {
            binding.etFieldEmailRegister.error = null
        }

        if (binding.etPasswordRegister.text.toString().isEmpty()) {
            binding.etFieldPasswordRegister.error = "Ingresa una contraseña"
            isValid = false
        } else if (!binding.etPasswordRegister.text.toString().matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{6,}$"))) {
            binding.etFieldPasswordRegister.error = "Mínimo debe haber 1 letra mayúscula.\n" +
                    "Mínimo debe haber 1 letra minúscula.\n" +
                    "Mínimo debe haber 1 número.\n" +
                    "Mínimo debe haber 1 símbolo especial [@\$!%*?&#].\n" +
                    "Longitud mínima de 6 caracteres."
            isValid = false
        } else {
            binding.etFieldPasswordRegister.error = null
        }

        if (binding.etRepeatPasswordRegister.text.toString().length < 6) {
            binding.etFieldPasswordRegister.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (binding.etRepeatPasswordRegister.text.toString().isEmpty()) {
            binding.etFieldRepeatPasswordRegister.error = "Repite la contraseña"
            isValid = false
        } else {
            binding.etFieldRepeatPasswordRegister.error = null
        }

        if (binding.etPasswordRegister.text.toString() != binding.etRepeatPasswordRegister.text.toString()) {
            binding.etFieldRepeatPasswordRegister.error = "Las contraseñas no coinciden"
            isValid = false
        }
        return isValid
    }
}