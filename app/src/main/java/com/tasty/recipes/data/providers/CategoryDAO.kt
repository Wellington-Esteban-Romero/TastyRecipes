package com.tasty.recipes.data.providers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.utils.DatabaseManager

class CategoryDAO (val context: Context) {

    private lateinit var db: SQLiteDatabase

    private fun open() {
        db = DatabaseManager(context).writableDatabase
    }

    private fun close() {
        db.close()
    }

    private fun getContentValues(category: Category): ContentValues {
        return ContentValues().apply {
            put(Category.COLUMN_NAME, category.name)
            put(Category.COLUMN_DESCRIPTION, category.description)
        }
    }

    private fun cursorToEntity(cursor: Cursor): Category {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(Category.COLUMN_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(Category.COLUMN_NAME))
        val description = cursor.getString(cursor.getColumnIndexOrThrow(Category.COLUMN_DESCRIPTION))

        return Category(id, name, description)
    }

    fun insert(category: Category) {
        open()

        // Create a new map of values, where column names are the keys
        val values = getContentValues(category)

        try {
            // Insert the new row, returning the primary key value of the new row
            db.insert(Category.TABLE_NAME, null, values)
        } catch (e: Exception) {
            Log.e("DB", e.stackTraceToString())
        } finally {
            close()
        }
    }

    fun findAll() : List<Category> {
        open()

        val list: MutableList<Category> = mutableListOf()

        try {
            val cursor = db.query(
                Category.TABLE_NAME,                    // The table to query
                Category.COLUMN_NAMES,                  // The array of columns to return (pass null to get all)
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
}