package com.tasty.recipes.data.entities

data class Extra(
    var countFavourite: Long = 0L,
    var countShare: Long = 0L,
    var recipeId: String = "",
    var userId: String = ""
) {
}