package com.tasty.recipes.utils

import android.content.Context

class SessionManager(context: Context) {

    private  val PREF_NAME = "user_session"
    private  val EMAIL_KEY = "user_email"

    private  val PREF_LAST_PROVIDER = "last_provider_session"
    private  val LAST_PROVIDER_KEY = "last_provider"

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

    //session last_provider

    fun saveLastProvider(context: Context, provider: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_LAST_PROVIDER, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LAST_PROVIDER_KEY, provider)
        editor.apply()
    }

    fun getLastProvider(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_LAST_PROVIDER, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAST_PROVIDER_KEY, null)
    }

    fun clearSessionLastProvider(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_LAST_PROVIDER, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}