package com.tasty.recipes.data.providers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.entities.RecipeCategory
import com.tasty.recipes.utils.DatabaseManager

class RecipeCategoryDAO (val context: Context) {

    private lateinit var db: SQLiteDatabase

    private fun open() {
        db = DatabaseManager(context).writableDatabase
    }

    private fun openBeginTransaction() {
        db = DatabaseManager(context).writableDatabase
        db.beginTransaction()
    }

    private fun close() {
        db.close()
    }

    private fun getContentValues(recipeCategory: RecipeCategory): ContentValues {
        return ContentValues().apply {
            put(RecipeCategory.COLUMN_RECIPE_ID, recipeCategory.recipeId)
            put(RecipeCategory.COLUMN_CATEGORY_ID, recipeCategory.categoryId)
        }
    }

    fun insertRecipeWithCategories(recipeName: String, categoryNames: List<String>) {
        open()
        try {
            // Insertar receta
            val recipeValues = ContentValues()
            recipeValues.put("name", recipeName)
            val recipeId = db.insert("Recipe", null, recipeValues)

            // Insertar categorías y relaciones
            for (categoryName in categoryNames) {
                val categoryId = getOrInsertCategory(db, categoryName)
                val recipeCategoryValues = ContentValues()
                recipeCategoryValues.put("recipeId", recipeId)
                recipeCategoryValues.put("categoryId", categoryId)
                db.insert("RecipeCategory", null, recipeCategoryValues)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun getOrInsertCategory(db: SQLiteDatabase, categoryName: String): Long {
        val cursor = db.query(
            "Category",
            arrayOf("id"),
            "name = ?",
            arrayOf(categoryName),
            null, null, null
        )

        val categoryId = if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        } else {
            val categoryValues = ContentValues()
            categoryValues.put("name", categoryName)
            db.insert("Category", null, categoryValues)
        }
        cursor.close()
        return categoryId
    }

    fun insert(recipeCategory: RecipeCategory) {
        open()

        val values = getContentValues(recipeCategory)

        try {
            db.insert(RecipeCategory.TABLE_NAME, null, values)
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
    }
}