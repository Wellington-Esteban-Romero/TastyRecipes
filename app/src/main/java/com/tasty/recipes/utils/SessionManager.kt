package com.tasty.recipes.utils

import android.content.Context

class SessionManager(context: Context) {

    private val PREF_NAME = "user_session"
    private val EMAIL_KEY = "user_email"

    private val PREF_LAST_SEE = "last_see_session"
    private val LAST_SEE_KEY = "last_see"

    private val SHARED_NAME = "Mydtb"

    companion object {
        const val ACTIVE = "1"
        const val DES_ACTIVE = "0"
    }

    private val storage = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);

    fun getAllFavoriteRecipe(): MutableMap<String, *>? {
        return storage.all
    }

    fun saveRecipe(key: String, value: String) {
        storage.edit().putString(key, value).apply()
    }

    private fun getRecipe(key: String): String {
        return storage.getString(key, "")!!
    }

    fun isFavorite(id: String): Boolean {
        return (getRecipe(id) == ACTIVE)
    }

    //session email

    fun saveUserEmail(context: Context, email: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(EMAIL_KEY, email)
        editor.apply()
    }

    fun getUserEmail(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(EMAIL_KEY, null)
    }

    fun clearSession(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    //session last_see

    fun saveLastSee(context: Context, isLastSee: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_LAST_SEE, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LAST_SEE_KEY, isLastSee)
        editor.apply()
    }

    fun getLastSee(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_LAST_SEE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAST_SEE_KEY, null)
    }

    fun clearSessionLastSee(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_LAST_SEE, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}