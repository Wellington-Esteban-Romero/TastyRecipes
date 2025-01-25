package com.tasty.recipes.data.entities

class User (
    val id: String = "",
    val username: String = "",
    val email: String = "",
    var photoUrl: String = ""
) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "username" to username,
            "email" to email,
            "photoUrl" to photoUrl
        )
    }
}