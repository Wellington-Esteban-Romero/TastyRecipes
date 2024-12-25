package com.tasty.recipes.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class AuthHelper {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getFirebaseAuth(): FirebaseAuth {
        return auth;
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

}