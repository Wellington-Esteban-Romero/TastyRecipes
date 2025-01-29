package com.tasty.recipes.data.entities

data class Follower (
    val followerId: String,
    val followedId: String,
    val recipeId: String
) {
}