package com.tasty.recipes.data.entities

data class Category(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    var isChecked: Boolean = false
)