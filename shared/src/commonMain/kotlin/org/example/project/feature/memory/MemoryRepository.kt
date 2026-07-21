package org.example.project.feature.memory

import com.watson.database.sqldelight.WatsonQueries
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class MemoryScope { GLOBAL, WORKSPACE, CONVERSATION }
enum class MemoryKind { FACT, SUMMARY }

data class MemoryEntry(
    val id: String,
    val scope: MemoryScope,
    val workspaceId: String?,
    val conversationId: String?,
    val kind: MemoryKind,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

class MemoryRepository(private val queries: WatsonQueries) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun now() = Clock.System.now().toEpochMilliseconds()

    fun recall(workspaceId: String?, conversationId: String?, limit: Long = 20): List<MemoryEntry> =
        queries.selectMemoriesForContext(workspaceId, conversationId, limit).executeAsList().map { it.toMemoryEntry() }

    @OptIn(ExperimentalUuidApi::class)
    fun save(
        content: String,
        scope: MemoryScope,
        workspaceId: String? = null,
        conversationId: String? = null,
        kind: MemoryKind = MemoryKind.FACT
    ): MemoryEntry {
        require(content.isNotBlank()) { "Memory content cannot be blank" }
        val time = now()
        val entry = MemoryEntry(Uuid.random().toString(), scope, workspaceId, conversationId, kind, content.trim(), time, time)
        queries.insertMemory(
            id = entry.id,
            scope = entry.scope.name,
            workspaceId = entry.workspaceId,
            conversationId = entry.conversationId,
            kind = entry.kind.name,
            content = entry.content,
            createdAt = time,
            updatedAt = time,
            isArchived = 0L
        )
        return entry
    }

    fun archive(id: String) = queries.archiveMemory(1L, now(), id)
}

private fun com.watson.database.sqldelight.MemoryEntry.toMemoryEntry() = MemoryEntry(
    id = id,
    scope = MemoryScope.valueOf(scope),
    workspaceId = workspaceId,
    conversationId = conversationId,
    kind = MemoryKind.valueOf(kind),
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)
