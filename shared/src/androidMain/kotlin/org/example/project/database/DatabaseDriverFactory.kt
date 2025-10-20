package org.example.project.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.watson.database.WatsonDatabase

// 实际的 Android 实现
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(WatsonDatabase.Companion.Schema, context, "app.db")
    }
}