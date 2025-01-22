package com.tasty.recipes.data.entities

data class Recipe(
    val id: String = "",
    var name: String = "",
    var ingredients: List<String> = mutableListOf(),
    var instructions: String = "",
    var prepTimeMinutes: Int = 0,
    var cookTimeMinutes: Int = 0,
    var servings: Int = 0,
    var difficulty: String = "",
    var image: String = "",
    var categoryIds: List<Long> = mutableListOf(),
    var userId: String = ""
)