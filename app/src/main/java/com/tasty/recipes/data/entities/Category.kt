package com.tasty.recipes.data.entities

data class Category(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    var isChecked: Boolean = false

) {

    companion object {
        const val TABLE_NAME = "Categories"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"

        val COLUMN_NAMES = arrayOf(
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_DESCRIPTION
        )
    }
}