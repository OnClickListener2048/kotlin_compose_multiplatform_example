package org.example.project.feature.prompt

import com.watson.database.sqldelight.WatsonQueries
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class PromptTemplate(
    val id: String,
    val name: String,
    val content: String,
    val workspaceId: String?,
    val priority: Long,
    val isEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

class PromptTemplateRepository(private val queries: WatsonQueries) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun now() = Clock.System.now().toEpochMilliseconds()

    fun enabledFor(workspaceId: String?): List<PromptTemplate> =
        queries.selectEnabledPromptTemplates(workspaceId).executeAsList().map { it.toPromptTemplate() }

    @OptIn(ExperimentalUuidApi::class)
    fun create(name: String, content: String, workspaceId: String? = null, priority: Long = 0): PromptTemplate {
        require(name.isNotBlank() && content.isNotBlank()) { "Prompt name and content cannot be blank" }
        val time = now()
        val template = PromptTemplate(Uuid.random().toString(), name.trim(), content.trim(), workspaceId, priority, true, time, time)
        queries.insertPromptTemplate(
            id = template.id,
            name = template.name,
            content = template.content,
            workspaceId = template.workspaceId,
            priority = template.priority,
            isEnabled = 1L,
            createdAt = time,
            updatedAt = time
        )
        return template
    }

    fun update(id: String, name: String, content: String, priority: Long, enabled: Boolean) {
        queries.updatePromptTemplate(name.trim(), content.trim(), priority, if (enabled) 1L else 0L, now(), id)
    }

    fun delete(id: String) = queries.deletePromptTemplate(id)
}

private fun com.watson.database.sqldelight.PromptTemplate.toPromptTemplate() = PromptTemplate(
    id = id,
    name = name,
    content = content,
    workspaceId = workspaceId,
    priority = priority,
    isEnabled = isEnabled != 0L,
    createdAt = createdAt,
    updatedAt = updatedAt
)
