package org.example.project.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.watson.database.WatsonDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val userHome = System.getProperty("user.home")
        val dbFolder = File(userHome, ".ai-assistant")
        if (!dbFolder.exists()) dbFolder.mkdirs()
        val path = File(dbFolder, "app.db").absolutePath

        val driver = JdbcSqliteDriver("jdbc:sqlite:$path")

        try {
            WatsonDatabase.Schema.migrate(driver, 0, WatsonDatabase.Schema.version)
        } catch (e: Exception) {
            WatsonDatabase.Schema.create(driver)
        }

        return driver
    }
}
