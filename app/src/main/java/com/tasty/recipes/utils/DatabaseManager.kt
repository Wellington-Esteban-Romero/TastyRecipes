package com.tasty.recipes.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.tasty.recipes.data.entities.Category
import com.tasty.recipes.data.entities.Recipe
import com.tasty.recipes.data.entities.RecipeCategory

class DatabaseManager(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "RecipesDatabase.db"


        private const val SQL_CREATE_TABLE =
            "CREATE TABLE ${Recipe.TABLE_NAME} (" +
                    "${Recipe.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${Recipe.COLUMN_NAME} TEXT," +
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

        private const val SQL_INSERT_RECIPE_1_TABLE = "INSERT INTO ${Recipe.TABLE_NAME} " +
                "(id, name, ingredients, instructions, prepTimeMinutes, cookTimeMinutes, servings, difficulty, image) " +
                "VALUES " +
                "(1, 'Classic Margherita Pizza', 'Pizza dough, Tomato sauce, Fresh mozzarella cheese, Fresh basil leaves, Olive oil, Salt and pepper to taste', " +
                "'Preheat the oven to 475°F (245°C). Roll out the pizza dough and spread tomato sauce evenly. " +
                "Top with slices of fresh mozzarella and fresh basil leaves. Drizzle with olive oil and season with salt and pepper. " +
                "Bake in the preheated oven for 12-15 minutes or until the crust is golden brown. Slice and serve hot.', " +
                "20, 15, 4, 'Easy', 'https://cdn.dummyjson.com/recipe-images/1.webp')"

        private const val SQL_INSERT_RECIPE_2_TABLE = "INSERT INTO ${Recipe.TABLE_NAME} " +
                "(id, name, ingredients, instructions, prepTimeMinutes, cookTimeMinutes, servings, difficulty, image) " +
                "VALUES " +
                "(2, 'Vegetarian Stir-Fry', 'Tofu, cubed, Broccoli florets, Carrots, sliced, Bell peppers, sliced, Soy sauce, Ginger, minced, Garlic, minced, Sesame oil, Cooked rice for serving', " +
                "'In a wok, heat sesame oil over medium-high heat. Add minced ginger and garlic, sauté until fragrant. " +
                "Add cubed tofu and stir-fry until golden brown. Add broccoli, carrots, and bell peppers. Cook until vegetables are tender-crisp. " +
                "Pour soy sauce over the stir-fry and toss to combine. Serve over cooked rice.', " +
                "15, 20, 3, 'Medium', 'https://cdn.dummyjson.com/recipe-images/2.webp')"

        private const val SQL_INSERT_RECIPE_3_TABLE = "INSERT INTO ${Recipe.TABLE_NAME} " +
                "(id, name, ingredients, instructions, prepTimeMinutes, cookTimeMinutes, servings, difficulty, image) " +
                "VALUES " +
                "(3, 'Chocolate Chip Cookies', 'All-purpose flour, Butter, softened, Brown sugar, White sugar, Eggs, Vanilla extract, Baking soda, Salt, Chocolate chips', " +
                "'Preheat the oven to 350°F (175°C). In a bowl, cream together softened butter, brown sugar, and white sugar. Beat in eggs one at a time, then stir in vanilla extract. " +
                "Combine flour, baking soda, and salt. Gradually add to the wet ingredients. Fold in chocolate chips. " +
                "Drop rounded tablespoons of dough onto ungreased baking sheets. Bake for 10-12 minutes or until edges are golden brown. " +
                "Allow cookies to cool on the baking sheet for a few minutes before transferring to a wire rack.', " +
                "15, 10, 24, 'Easy', 'https://cdn.dummyjson.com/recipe-images/3.webp')"

        private const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${Recipe.TABLE_NAME}"
        private const val SQL_DELETE_TABLE_CATEGORIES =
            "DROP TABLE IF EXISTS ${Category.TABLE_NAME}"
        private const val SQL_DELETE_TABLE_RECIPES_CATEGORIES =
            "DROP TABLE IF EXISTS ${RecipeCategory.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE)
        db.execSQL(SQL_CREATE_TABLE_CATEGORIES)
        db.execSQL(SQL_CREATE_TABLE_RECIPES_CATEGORIES)

        db.execSQL(SQL_INSERT_RECIPE_1_TABLE)
        db.execSQL(SQL_INSERT_RECIPE_2_TABLE)
        db.execSQL(SQL_INSERT_RECIPE_3_TABLE)
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