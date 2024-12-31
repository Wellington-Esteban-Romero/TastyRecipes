package com.tasty.recipes.utils

import android.content.Context

class SessionManager(context: Context) {

    private  val PREF_NAME = "user_session"
    private  val EMAIL_KEY = "user_email"

    companion object {
        const val ACTIVE = "1"
        const val DES_ACTIVE = "0"
    }

    private val SHARED_NAME = "Mydtb"
    private val SHARED_NAME_RECIPE = "recipe"
    private val SHARED_NAME_RECIPES = "recipes"

    private val storage = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);

    fun saveRecipe (key:String ,value:String) {
        storage.edit().putString(SHARED_NAME_RECIPE + key, value).apply()
    }

    private fun getRecipe (key:String):String {
        return storage.getString(SHARED_NAME_RECIPE + key, "")!!
    }

    fun isFavorite (id:String):Boolean {
        return (getRecipe (id) == ACTIVE)
    }

    fun isLoadRecipes (id:String):Boolean {
        return (getRecipes (id) == ACTIVE)
    }

    fun saveRecipes (key:String ,value:String) {
        storage.edit().putString(SHARED_NAME_RECIPES + key, value).apply()
    }

    private fun getRecipes (key:String):String {
        return storage.getString(SHARED_NAME_RECIPES + key, "")!!
    }

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
}