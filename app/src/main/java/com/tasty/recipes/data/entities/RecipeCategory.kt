package com.tasty.recipes.data.entities

class RecipeCategory (
    val recipeId: Long,
    val categoryId: Long
) {

    companion object {
        const val TABLE_NAME = "RecipeCategory"
        const val COLUMN_RECIPE_ID = "recipeId"
        const val COLUMN_CATEGORY_ID = "categoryId"
    }
}