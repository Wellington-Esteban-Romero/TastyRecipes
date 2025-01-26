package com.tasty.recipes.data.entities

import org.checkerframework.checker.units.qual.A

data class Recipe(
    var id: String = "",
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
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "ingredients" to ingredients,
            "instructions" to instructions,
            "prepTimeMinutes" to prepTimeMinutes,
            "cookTimeMinutes" to cookTimeMinutes,
            "servings" to servings,
            "difficulty" to difficulty,
            "image" to image,
            "categoryIds" to categoryIds,
            "userId" to userId
        )
    }
}