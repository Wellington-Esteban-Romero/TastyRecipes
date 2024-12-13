package com.tasty.recipes.data.entities

data class Recipe(
        val id: Long,
        var name: String,
        var ingredients: List<String> = mutableListOf(),
        var instructions: List<String> = mutableListOf(),
        var prepTimeMinutes: Int = 0,
        var cookTimeMinutes: Int = 0,
        var servings: Int = 0,
        var difficulty: String = "",
        var image: String = "",
    ){

        companion object {
            const val TABLE_NAME = "Recipes"
            const val COLUMN_ID = "id"
            const val COLUMN_NAME = "name"
            const val COLUMN_INGREDIENTS = "ingredients"
            const val COLUMN_INSTRUCTIONS = "instructions"
            const val COLUMN_PREP_TIME_MINUTES = "prepTimeMinutes"
            const val COLUMN_COOK_TIME_MINUTES = "cookTimeMinutes"
            const val COLUMN_SERVINGS = "servings"
            const val COLUMN_DIFFICULTY = "difficulty"
            const val COLUMN_IMG = "image"
            val COLUMN_NAMES = arrayOf(
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_INGREDIENTS,
                COLUMN_INSTRUCTIONS,
                COLUMN_PREP_TIME_MINUTES,
                COLUMN_COOK_TIME_MINUTES,
                COLUMN_SERVINGS,
                COLUMN_DIFFICULTY,
                COLUMN_IMG
            )
        }
}