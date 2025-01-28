package com.tasty.recipes.data.entities

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
    var categoryIds: List<String> = mutableListOf(),
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