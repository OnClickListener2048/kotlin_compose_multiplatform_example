package org.example.project.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.watson.database.WatsonDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(WatsonDatabase.Schema, "watson.db")
//        throw NotImplementedError("Database driver is not implemented for iOS yet.")
    }
}