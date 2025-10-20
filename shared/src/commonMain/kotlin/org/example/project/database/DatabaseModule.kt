package org.example.project.database

import app.cash.sqldelight.ColumnAdapter
import com.watson.database.WatsonDatabase
import com.watson.database.sqldelight.ChatItem
import com.watson.database.sqldelight.ChatItemQueries
import org.example.project.bean.ChatItemType


class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = WatsonDatabase(
        driver = databaseDriverFactory.createDriver(),
        // ✨ 在这里，为 ChatItem 表提供包含我们自定义适配器的 Adapter ✨
        ChatItemAdapter = ChatItem.Adapter(
            typeAdapter = chatItemTypeAdapter
        )
    )

    // 暴露生成的查询接口
    val chatItemQueries: ChatItemQueries = database.chatItemQueries
}

/**
 * ChatItemType 的列适配器。
 * 将 ChatItemType 枚举与数据库中的 TEXT 类型进行相互转换。
 */
val chatItemTypeAdapter = object : ColumnAdapter<ChatItemType, String> {
    /**
     * 将 Kotlin 的 ChatItemType 枚举编码为数据库的 String。
     * 例如：ChatItemType.Question -> "Question"
     */
    override fun encode(value: ChatItemType): String = value.name

    /**
     * 将数据库的 String 解码为 Kotlin 的 ChatItemType 枚举。
     * 例如："Question" -> ChatItemType.Question
     */
    override fun decode(databaseValue: String): ChatItemType = ChatItemType.valueOf(databaseValue)
}