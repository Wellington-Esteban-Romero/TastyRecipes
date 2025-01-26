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
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tasty.recipes.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var photoUrl: String = ""

    companion object {
        private const val TAG = "Firestore"
        const val PATTERN_EMAIL = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        const val PATTERN_PASSWORD =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{6,}$"
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    this.contentResolver.takePersistableUriPermission(uri, flag)
                    photoUrl = uri.toString()
                    binding.ivSelectedImage.setImageURI(uri)
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
                        saveUserToFirestore(user.uid, userData) { isSuccess ->
                            if (isSuccess) {
                                sendVerificationEmail(user)
                                showToast("Verifica tu email antes de continuar.")
                            } else {
                                handleFirestoreFailure(Exception("Failed to save user data"), user)
                            }
                        }
                    }
                } else {
                    showToast("Error: ${task.exception?.message}")
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
        callback: (Boolean) -> Unit
    ) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User added to Firestore successfully.")
                val photoUrl = userData["photoUrl"].toString()
                val b = if (photoUrl.isNotEmpty()) {
                    uploadImageToStorage(photoUrl.toUri()) { imageUrl ->
                        if (imageUrl != null) {
                            saveImageFromStorage(imageUrl, userId)
                        } else {
                            Toast.makeText(
                                this,
                                "Error uploading image.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                } else {
                    false
                }
                callback(b)
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }

    private fun handleFirestoreFailure(exception: Exception, user: FirebaseUser) {
        user.delete().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Log.e(TAG, "User creation reverted due to Firestore failure", exception)
            } else {
                Log.e(
                    TAG,
                    "Failed to revert user creation in Auth.",
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

    private fun uploadImageToStorage(imageUri: Uri, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_pictures/image_${System.currentTimeMillis()}.web")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        callback(downloadUrl.toString())
                    }
                    .addOnFailureListener { _ ->
                        callback(null)
                    }
            }
            .addOnFailureListener { _ ->
                callback(null)
            }
    }

    private fun saveImageFromStorage(imageUrl: String, refID: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(refID)
            .update("photoUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "users added successfully",
                    Toast.LENGTH_SHORT
                ).show()
                signOutAndRedirectToLogin()
            }
            .addOnFailureListener { e ->
                showToast("Error updating recipe image URL: ${e.message}")
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
        } else if (!binding.etEmailRegister.text.toString().matches(Regex(PATTERN_EMAIL))) {
            binding.etFieldEmailRegister.error = "Ingresa un correo electrónico válido"
            isValid = false
        } else {
            binding.etFieldEmailRegister.error = null
        }

        if (binding.etPasswordRegister.text.toString().isEmpty()) {
            binding.etFieldPasswordRegister.error = "Ingresa una contraseña"
            isValid = false
        } else if (!binding.etPasswordRegister.text.toString().matches(Regex(PATTERN_PASSWORD))) {
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}