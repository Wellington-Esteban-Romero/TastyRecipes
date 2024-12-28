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
    private lateinit var photoUrl: String

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(uri, flag)
                photoUrl = uri.toString()

                // Copiar el URI a un archivo local
                val localFile = copyUriToFile(uri)
                if (localFile != null) {
                    // Cargar la imagen desde el archivo
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

            this.register_user = User("", username, email, password, repeatPassword)

            if (validate(register_user)) {
                createAccount(register_user.email, register_user.password)
            }
        }

        binding.btnSelectImage.setOnClickListener {
            if(ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this))
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun createAccount(email: String, password: String) {
        authHelper.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid

                    val userData: MutableMap<String, Any> = HashMap()
                    userData["id"] = user!!.uid
                    userData["username"] = this.register_user.username
                    userData["email"] = user.email!!
                    userData["photoUrl"] = register_user.photoUrl

                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId!!)
                        .set(userData)
                        .addOnSuccessListener { aVoid: Void? ->
                            Log.d(
                                "Firestore",
                                "Usuario añadido correctamente"
                            )
                            val currentUser = authHelper.getCurrentUser()
                            if (currentUser != null) {
                                authHelper.getFirebaseAuth().signOut()
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                        }
                        .addOnFailureListener { e: Exception? ->
                            user.delete()
                                .addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {
                                        Log.e("Firestore", "Error al guardar usuario, Auth revertido", e)
                                    } else {
                                        Log.e("Firestore", "Error al guardar usuario y fallo al eliminar de Auth", deleteTask.exception)
                                    }
                                }
                        }
                } else {
                    // Si el registro falla, muestra un mensaje al usuario
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
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