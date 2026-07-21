package org.example.project.repo

import com.watson.database.sqldelight.WatsonQueries
import org.example.project.bean.ChatItemType
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

data class Conversation(
    val id: String,
    val title: String,
    val providerType: org.example.project.chat.ProviderType,
    val model: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val isArchived: Boolean
)

data class ChatItem(
    val id: String,
    val conversationId: String,
    val content: String,
    val type: ChatItemType,
    val createdAt: Long,
    val isLoading: Boolean = false
)

class ChatRepository(
    private val queries: WatsonQueries
) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun now() = Clock.System.now().toEpochMilliseconds()

    @OptIn(ExperimentalUuidApi::class)
    fun createConversation(
        title: String = "New Chat",
        providerType: org.example.project.chat.ProviderType,
        model: String = providerType.defaultModel
    ): Conversation {
        val id = Uuid.random().toString()
        val time = now()
        queries.insertConversation(
            id = id,
            title = title,
            providerType = providerType,
            model = model,
            createdAt = time,
            updatedAt = time,
            isPinned = 0L,
            isArchived = 0L
        )
        return Conversation(id, title, providerType, model, time, time, false, false)
    }

    fun getConversations(): List<Conversation> {
        return queries.selectConversations().executeAsList().map { it.toConversation() }
    }

    fun getArchivedConversations(): List<Conversation> {
        return queries.selectArchivedConversations().executeAsList().map { it.toConversation() }
    }

    fun searchConversations(query: String): List<Conversation> {
        return queries.searchConversations(query).executeAsList().map { it.toConversation() }
    }

    fun getConversationById(id: String): Conversation? {
        return queries.selectConversationById(id).executeAsOneOrNull()?.toConversation()
    }

    fun updateConversationTitle(id: String, title: String) {
        queries.updateConversationTitle(id = id, title = title, updatedAt = now())
    }

    fun toggleConversationPin(id: String, isPinned: Boolean) {
        queries.updateConversationPin(id = id, isPinned = if (isPinned) 1L else 0L, updatedAt = now())
    }

    fun toggleConversationArchive(id: String, isArchived: Boolean) {
        queries.updateConversationArchive(id = id, isArchived = if (isArchived) 1L else 0L, updatedAt = now())
    }

    fun deleteConversation(id: String) {
        queries.deleteByConversationId(id)
        queries.deleteConversation(id)
    }

    fun updateConversationTimestamp(id: String) {
        queries.updateConversationUpdatedAt(id = id, updatedAt = now())
    }

    fun getMessages(conversationId: String): List<ChatItem> {
        return queries.selectAllOrderedByTime(conversationId).executeAsList().map { row ->
            ChatItem(
                id = row.id,
                conversationId = row.conversationId,
                content = row.content,
                type = row.type,
                createdAt = row.createdAt
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun insertMessage(conversationId: String, content: String, type: ChatItemType): ChatItem {
        val id = Uuid.random().toString()
        val time = now()
        queries.insertItem(
            id = id,
            conversationId = conversationId,
            content = content,
            type = type,
            createdAt = time
        )
        updateConversationTimestamp(conversationId)
        return ChatItem(id, conversationId, content, type, time)
    }

    fun updateMessageContent(id: String, content: String) {
        queries.updateContentById(content = content, id = id)
    }

    fun deleteMessage(id: String) {
        queries.deleteById(id)
    }

    fun getMessageCount(conversationId: String): Int {
        return queries.selectAllOrderedByTime(conversationId).executeAsList().size
    }
}

private fun com.watson.database.sqldelight.Conversation.toConversation() = Conversation(
    id = id,
    title = title,
    providerType = providerType,
    model = model,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isPinned = isPinned != 0L,
    isArchived = isArchived != 0L
)
