package org.example.project.feature.workspace

import com.watson.database.sqldelight.WatsonQueries
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val INBOX_WORKSPACE_ID = "inbox"

data class Workspace(
    val id: String,
    val name: String,
    val systemPrompt: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean
)

class WorkspaceRepository(private val queries: WatsonQueries) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun now() = Clock.System.now().toEpochMilliseconds()

    fun ensureInbox(): Workspace {
        return getById(INBOX_WORKSPACE_ID) ?: Workspace(
            id = INBOX_WORKSPACE_ID,
            name = "Personal",
            systemPrompt = "",
            createdAt = now(),
            updatedAt = now(),
            isArchived = false
        ).also { workspace ->
            queries.insertWorkspace(
                id = workspace.id,
                name = workspace.name,
                systemPrompt = workspace.systemPrompt,
                createdAt = workspace.createdAt,
                updatedAt = workspace.updatedAt,
                isArchived = 0L
            )
        }
    }

    fun getAll(): List<Workspace> = queries.selectWorkspaces().executeAsList().map { it.toWorkspace() }

    fun getById(id: String): Workspace? = queries.selectWorkspaceById(id).executeAsOneOrNull()?.toWorkspace()

    @OptIn(ExperimentalUuidApi::class)
    fun create(name: String, systemPrompt: String = ""): Workspace {
        val time = now()
        val workspace = Workspace(Uuid.random().toString(), name.trim(), systemPrompt.trim(), time, time, false)
        queries.insertWorkspace(
            id = workspace.id,
            name = workspace.name,
            systemPrompt = workspace.systemPrompt,
            createdAt = time,
            updatedAt = time,
            isArchived = 0L
        )
        return workspace
    }

    fun update(id: String, name: String, systemPrompt: String) {
        queries.updateWorkspace(name.trim(), systemPrompt.trim(), now(), id)
    }

    fun archive(id: String, archived: Boolean) {
        if (id != INBOX_WORKSPACE_ID) queries.archiveWorkspace(if (archived) 1L else 0L, now(), id)
    }
}

private fun com.watson.database.sqldelight.Workspace.toWorkspace() = Workspace(
    id = id,
    name = name,
    systemPrompt = systemPrompt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isArchived = isArchived != 0L
)
