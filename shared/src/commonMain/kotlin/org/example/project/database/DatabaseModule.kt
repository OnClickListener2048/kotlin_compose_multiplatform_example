package org.example.project.database

import app.cash.sqldelight.ColumnAdapter
import com.watson.database.WatsonDatabase
import com.watson.database.sqldelight.WatsonQueries
import com.watson.database.sqldelight.ChatItem
import com.watson.database.sqldelight.Conversation
import com.watson.database.sqldelight.ApiKey
import org.example.project.bean.ChatItemType
import org.example.project.chat.ProviderType

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    val watsonQueries: WatsonQueries

    init {
        val driver = databaseDriverFactory.createDriver()
        try {
            WatsonDatabase.Schema.migrate(driver, 0, WatsonDatabase.Schema.version)
        } catch (e: Exception) {
            driver.execute(null, "DROP TABLE IF EXISTS ChatItem", 0)
            driver.execute(null, "DROP TABLE IF EXISTS Conversation", 0)
            driver.execute(null, "DROP TABLE IF EXISTS ApiKey", 0)
            WatsonDatabase.Schema.create(driver)
        }
        watsonQueries = WatsonDatabase(
            driver = driver,
            ChatItemAdapter = ChatItem.Adapter(typeAdapter = chatItemTypeAdapter),
            ConversationAdapter = Conversation.Adapter(providerTypeAdapter = providerTypeAdapter),
            ApiKeyAdapter = ApiKey.Adapter(providerTypeAdapter = providerTypeAdapter)
        ).watsonQueries
    }
}

val chatItemTypeAdapter = object : ColumnAdapter<ChatItemType, String> {
    override fun encode(value: ChatItemType): String = value.name
    override fun decode(databaseValue: String): ChatItemType = ChatItemType.valueOf(databaseValue)
}

val providerTypeAdapter = object : ColumnAdapter<ProviderType, String> {
    override fun encode(value: ProviderType): String = value.name
    override fun decode(databaseValue: String): ProviderType = ProviderType.valueOf(databaseValue)
}
