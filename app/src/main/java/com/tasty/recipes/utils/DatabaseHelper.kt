package com.tasty.recipes.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DatabaseHelper(private val context: Context) {


    private val databaseName = "recipe.db"
    private val databasePath = context.getDatabasePath(databaseName).path

    fun openDatabase(): SQLiteDatabase {
        if (!databaseExists()) {
            copyDatabaseFromAssets()
        }
        return SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    private fun databaseExists(): Boolean {
        val dbFile = File(databasePath)
        return dbFile.exists()
    }

    private fun copyDatabaseFromAssets() {
        val inputStream: InputStream = context.assets.open(databaseName)
        val outputStream = FileOutputStream(databasePath)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }
}