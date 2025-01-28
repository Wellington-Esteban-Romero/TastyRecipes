package com.tasty.recipes.data.entities

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    var isChecked: Boolean = false
)