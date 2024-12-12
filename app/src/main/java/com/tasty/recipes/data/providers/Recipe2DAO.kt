package com.tasty.recipes.data.providers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.utils.DatabaseHelper
import com.tasty.recipes.utils.DatabaseManager

class Recipe2DAO(val context: Context) {

    private lateinit var db: SQLiteDatabase
    private lateinit var dh: SQLiteDatabase

    private fun open() {
        db = DatabaseManager(context).writableDatabase
        dh = DatabaseHelper(context).openDatabase()
    }

    private fun close() {
        db.close()
        dh.close()
    }

    private fun getContentValues(recipe: Recipe): ContentValues {
        return ContentValues().apply {
            put(Recipe.COLUMN_NAME_TITLE, recipe.name)
            put(Recipe.COLUMN_INGREDIENTS, recipe.ingredients.joinToString(", "))
            put(Recipe.COLUMN_INSTRUCTIONS, recipe.instructions.joinToString(", "))
            put(Recipe.COLUMN_PREP_TIME_MINUTES, recipe.prepTimeMinutes)
            put(Recipe.COLUMN_COOK_TIME_MINUTES, recipe.cookTimeMinutes)
            put(Recipe.COLUMN_SERVINGS, recipe.servings)
            put(Recipe.COLUMN_DIFFICULTY, recipe.difficulty)
            put(Recipe.COLUMN_IMG, recipe.image)
        }
    }

    private fun cursorToEntity(cursor: Cursor): Recipe {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(Recipe.COLUMN_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(Recipe.COLUMN_NAME_TITLE))
        val ingredients = cursor.getString(cursor.getColumnIndexOrThrow(Recipe.COLUMN_INGREDIENTS))
        val instructions =
            cursor.getString(cursor.getColumnIndexOrThrow(Recipe.COLUMN_INSTRUCTIONS))
        val prepTimeMinutes =
            cursor.getInt(cursor.getColumnIndexOrThrow(Recipe.COLUMN_PREP_TIME_MINUTES))
        val cookTimeMinutes =
            cursor.getInt(cursor.getColumnIndexOrThrow(Recipe.COLUMN_COOK_TIME_MINUTES))
        val servings = cursor.getString(cursor.getColumnIndexOrThrow(Recipe.COLUMN_SERVINGS))
        val difficulty = cursor.getString(cursor.getColumnIndexOrThrow(Recipe.COLUMN_DIFFICULTY))
        val image = cursor.getString(cursor.getColumnIndexOrThrow(Recipe.COLUMN_IMG))

        return Recipe(
            id,
            name,
            ingredients.split("-"),
            instructions.split("-"),
            prepTimeMinutes,
            cookTimeMinutes,
            servings,
            difficulty,
            image
        )
    }

    fun findAll() : List<Recipe> {
        open()

        val list: MutableList<Recipe> = mutableListOf()

        try {
            val cursor = dh.query(
                Recipe.TABLE_NAME,                    // The table to query
                Recipe.COLUMN_NAMES,                  // The array of columns to return (pass null to get all)
                null,// The columns for the WHERE clause
                null,                   // The values for the WHERE clause
                null,                       // don't group the rows
                null,                         // don't filter by row groups
                null                         // The sort order
            )

            while (cursor.moveToNext()) {
                val category = cursorToEntity(cursor)
                list.add(category)
            }
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
        return list
    }

    fun insert(recipe: Recipe) {
        open()

        // Create a new map of values, where column names are the keys
        val values = getContentValues(recipe)

        try {
            // Insert the new row, returning the primary key value of the new row
            val id = db.insert(Recipe.TABLE_NAME, null, values)
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
    }

    fun update(recipe: Recipe) {
        open()

        // Create a new map of values, where column names are the keys
        val values = getContentValues(recipe)

        try {
            // Update the existing rows, returning the number of affected rows
            val updatedRows = db.update(
                Recipe.TABLE_NAME, values,
                "${Recipe.COLUMN_ID} = ${recipe.id}",
                null
            )
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
    }

    fun deleteAll() {
        open()

        try {
            // Insert the new row, returning the primary key value of the new row
            val id = db.delete(Recipe.TABLE_NAME, null, null)
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
    }

    fun deleteRecipe(idRecipe: String) {
        open()

        try {
            // Insert the new row, returning the primary key value of the new row
            val id = db.delete(
                Recipe.TABLE_NAME,
                "${Recipe.COLUMN_ID} = ?",
                arrayOf(idRecipe)
            )
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
    }

    fun findAllByCategory(idCategory:String): List<Recipe> {
        open()

        val list: MutableList<Recipe> = mutableListOf()

        try {
            val cursor = db.query(
                Recipe.TABLE_NAME,                    // The table to query
                Recipe.COLUMN_NAMES,                  // The array of columns to return (pass null to get all)
                "${Recipe.COLUMN_ID} = ?",    // The columns for the WHERE clause
                null,                   // The values for the WHERE clause
                null,                       // don't group the rows
                null,                         // don't filter by row groups
                null                         // The sort order
            )

            while (cursor.moveToNext()) {
                val task = cursorToEntity(cursor)
                list.add(task)
            }
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
        return list
    }

    fun findRecipeById(idRecipe: String): Recipe? {
        open()

        var recipe: Recipe? = null

        try {
            val cursor = db.query(
                Recipe.TABLE_NAME,                // The table to query
                Recipe.COLUMN_NAMES,              // The array of columns to return (pass null to get all)
                "${Recipe.COLUMN_ID} = ?",        // The columns for the WHERE clause
                arrayOf(idRecipe),                // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                null                              // The sort order
            )

            if (cursor.moveToFirst()) {
                recipe = cursorToEntity(cursor)
            }
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
        return recipe
    }
}