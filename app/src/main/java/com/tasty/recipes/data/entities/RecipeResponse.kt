package com.tasty.recipes.data.entities

import com.google.gson.annotations.SerializedName


data class RecipeResponse(
    @SerializedName("recipes") val recipes: MutableList<Recipe>,
    @SerializedName("total") val total: Int,
) {
}

data class Recipe(
    @SerializedName("id") val id: Long,
    @SerializedName("name") var name: String,
    @SerializedName("ingredients") var ingredients: List<String> = mutableListOf(),
    @SerializedName("instructions") var instructions: List<String> = mutableListOf(),
    @SerializedName("prepTimeMinutes") var prepTimeMinutes: Int = 0,
    @SerializedName("cookTimeMinutes") var cookTimeMinutes: Int = 0,
    @SerializedName("servings") var servings: String = "",
    @SerializedName("difficulty") var difficulty: String = "",
    //@SerializedName("cuisine") val cuisine: String,
    //@SerializedName("caloriesPerServing") val caloriesPerServing: Int,
    @SerializedName("image") var image: String = "",
){

    companion object {
        const val TABLE_NAME = "Recipes"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_INGREDIENTS = "ingredients"
        const val COLUMN_INSTRUCTIONS = "instructions"
        const val COLUMN_PREP_TIME_MINUTES = "prepTimeMinutes"
        const val COLUMN_COOK_TIME_MINUTES = "cookTimeMinutes"
        const val COLUMN_SERVINGS = "servings"
        const val COLUMN_DIFFICULTY = "difficulty"
        const val COLUMN_IMG = "img"
        //const val COLUMN_CATEGORY = "category"
        val COLUMN_NAMES = arrayOf(
            COLUMN_ID,
            COLUMN_NAME_TITLE,
            COLUMN_INGREDIENTS,
            COLUMN_INSTRUCTIONS,
            COLUMN_PREP_TIME_MINUTES,
            COLUMN_COOK_TIME_MINUTES,
            COLUMN_SERVINGS,
            COLUMN_DIFFICULTY,
            COLUMN_IMG
            //COLUMN_CATEGORY
        )
    }
}