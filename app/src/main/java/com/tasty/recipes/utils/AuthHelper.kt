package com.tasty.recipes.utils

import com.google.firebase.auth.FirebaseAuth


class AuthHelper {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

}