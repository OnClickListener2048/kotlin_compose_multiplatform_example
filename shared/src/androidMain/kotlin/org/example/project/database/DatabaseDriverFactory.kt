package org.example.project.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.watson.database.WatsonDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val name = "app.db"

        val dbFile = context.getDatabasePath(name)
        if (dbFile.exists()) {
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            val version = db.use { it.version }
            if (version.toLong() != WatsonDatabase.Schema.version) {
                context.deleteDatabase(name)
            }
        }

        return AndroidSqliteDriver(WatsonDatabase.Companion.Schema, context, name)
    }
}