package org.example.project.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.watson.database.WatsonDatabase
import java.io.File

// import app.cash.sqldelight.driver.sqlite.JdbcSqliteDriver // for Desktop
//import com.yourpackage.database._root_ide_package_.com.watson.database.WatsonDatabase
//import java.io.File // for Desktop, iOS不需要

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // --- 仅限 Desktop: 定义稳定的数据库文件路径 ---
        val userHome = System.getProperty("user.home")
        val dbFolder = File(userHome, ".my-app-data")
        if (!dbFolder.exists()) dbFolder.mkdirs()
        val databasePath = File(dbFolder, "app.db").absolutePath
        // -----------------------------------------

        // --- 对于 iOS，路径更简单 ---
        // val databasePath = "app.db"
        // -----------------------------

         val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath") // for Desktop
//        val driver = NativeSqliteDriver(_root_ide_package_.com.watson.database.WatsonDatabase.Schema, "app.db") // for iOS - Schema 作为参数传入

        // ✨ 健壮的 Schema 创建/迁移逻辑 ✨
        val currentVersion = driver.getCurrentVersion()

        if (currentVersion == 0L) {
            // 场景 1: 数据库是全新的 (版本为 0)
            println("Database not found. Creating schema version ${WatsonDatabase.Schema.version}...")
            WatsonDatabase.Schema.create(driver)
            driver.setVersion(WatsonDatabase.Schema.version)
            println("Schema created successfully.")
        } else if (currentVersion < WatsonDatabase.Schema.version) {
            // 场景 2: 数据库是旧版本，需要迁移
            println("Migrating database from version $currentVersion to ${WatsonDatabase.Schema.version}...")
            WatsonDatabase.Schema.migrate(driver, currentVersion, WatsonDatabase.Schema.version)
            driver.setVersion(WatsonDatabase.Schema.version)
            println("Migration completed.")
        } else if (currentVersion > WatsonDatabase.Schema.version) {
            // 场景 3 (警告): 数据库版本比代码还新，这通常是个问题
            println("Warning: Database version ($currentVersion) is newer than schema version (${WatsonDatabase.Schema.version}).")
        } else {
            // 场景 4: 数据库版本和 Schema 版本一致，无需操作
            println("Database schema is up to date (version ${WatsonDatabase.Schema.version}).")
        }

        return driver
    }
}

// 辅助函数，封装 PRAGMA 操作
private fun SqlDriver.getCurrentVersion(): Long {
    return 1
}

private fun SqlDriver.setVersion(version: Long) {
    execute(null, "PRAGMA user_version = $version;", 0, null)
}