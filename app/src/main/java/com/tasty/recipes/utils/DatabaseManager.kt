package com.tasty.recipes.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.entities.RecipeCategory

class DatabaseManager(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "RecipesDatabase.db"


        private const val SQL_CREATE_TABLE =
            "CREATE TABLE ${Recipe.TABLE_NAME} (" +
                    "${Recipe.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${Recipe.COLUMN_NAME_TITLE} TEXT," +
                    "${Recipe.COLUMN_INGREDIENTS} TEXT," +
                    "${Recipe.COLUMN_INSTRUCTIONS} TEXT," +
                    "${Recipe.COLUMN_PREP_TIME_MINUTES} TEXT," +
                    "${Recipe.COLUMN_COOK_TIME_MINUTES} TEXT," +
                    "${Recipe.COLUMN_SERVINGS} TEXT," +
                    "${Recipe.COLUMN_DIFFICULTY} TEXT," +
                    "${Recipe.COLUMN_IMG} TEXT)"

        private const val SQL_CREATE_TABLE_CATEGORIES =
            "CREATE TABLE ${Category.TABLE_NAME} (" +
                    "${Category.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${Category.COLUMN_NAME} TEXT," +
                    "${Category.COLUMN_DESCRIPTION} TEXT)"

        private const val SQL_CREATE_TABLE_RECIPES_CATEGORIES =
            "CREATE TABLE ${RecipeCategory.TABLE_NAME} (" +
                    "${RecipeCategory.COLUMN_RECIPE_ID} INTEGER NOT NULL," +
                    "${RecipeCategory.COLUMN_CATEGORY_ID} INTEGER NOT NULL," +
                    "PRIMARY KEY (${RecipeCategory.COLUMN_RECIPE_ID}, ${RecipeCategory.COLUMN_CATEGORY_ID})," +
                    "FOREIGN KEY (${RecipeCategory.COLUMN_RECIPE_ID}) REFERENCES Recipe(${Recipe.COLUMN_ID})," +
                    "FOREIGN KEY (${RecipeCategory.COLUMN_CATEGORY_ID}) REFERENCES Recipe(${Category.COLUMN_ID}))"

        private const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${Recipe.TABLE_NAME}"
        private const val SQL_DELETE_TABLE_CATEGORIES = "DROP TABLE IF EXISTS ${Category.TABLE_NAME}"
        private const val SQL_DELETE_TABLE_RECIPES_CATEGORIES = "DROP TABLE IF EXISTS ${RecipeCategory.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE)
        db.execSQL(SQL_CREATE_TABLE_CATEGORIES)
        db.execSQL(SQL_CREATE_TABLE_RECIPES_CATEGORIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onDestroy(db)
        onCreate(db)
    }

    private fun onDestroy(db: SQLiteDatabase) {
        db.execSQL(SQL_DELETE_TABLE)
        db.execSQL(SQL_DELETE_TABLE_CATEGORIES)
        db.execSQL(SQL_DELETE_TABLE_RECIPES_CATEGORIES)
    }
}