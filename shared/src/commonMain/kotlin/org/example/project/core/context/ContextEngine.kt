package org.example.project.core.context

import org.example.project.chat.ChatMessage
import org.example.project.feature.files.FileAssetRepository
import org.example.project.feature.memory.MemoryRepository
import org.example.project.feature.prompt.PromptTemplateRepository
import org.example.project.feature.workspace.Workspace

/** A deterministic, inspectable prompt assembly pipeline. */
data class ContextRequest(
    val workspace: Workspace?,
    val conversationId: String?,
    val history: List<ChatMessage>
)

interface PromptProvider {
    val order: Int
    fun provide(request: ContextRequest): List<ChatMessage>
}

class ContextEngine(providers: Set<PromptProvider>) {
    private val providers = providers.sortedBy { it.order }

    fun build(request: ContextRequest): List<ChatMessage> =
        providers.flatMap { it.provide(request) }
}

class SystemPromptProvider : PromptProvider {
    override val order = 10

    override fun provide(request: ContextRequest) = listOf(
        ChatMessage(
            role = "system",
            content = "You are AI Assistant. Be accurate, concise, and explicit about uncertainty."
        )
    )
}

class TemplatePromptProvider(
    private val templates: PromptTemplateRepository
) : PromptProvider {
    override val order = 20

    override fun provide(request: ContextRequest): List<ChatMessage> =
        templates.enabledFor(request.workspace?.id).map {
            ChatMessage(role = "system", content = "Application instruction (${it.name}):\n${it.content}")
        }
}

class WorkspacePromptProvider : PromptProvider {
    override val order = 30

    override fun provide(request: ContextRequest): List<ChatMessage> {
        val workspace = request.workspace ?: return emptyList()
        val description = buildString {
            append("Current workspace: ${workspace.name}.")
            if (workspace.systemPrompt.isNotBlank()) append("\nWorkspace instruction:\n${workspace.systemPrompt}")
        }
        return listOf(ChatMessage(role = "system", content = description))
    }
}

class MemoryPromptProvider(
    private val memories: MemoryRepository
) : PromptProvider {
    override val order = 40

    override fun provide(request: ContextRequest): List<ChatMessage> {
        val entries = memories.recall(request.workspace?.id, request.conversationId)
        if (entries.isEmpty()) return emptyList()
        val content = entries.joinToString(separator = "\n") { "- ${it.content}" }
        return listOf(ChatMessage(role = "system", content = "Relevant memory (use only when applicable):\n$content"))
    }
}

class FilePromptProvider(
    private val files: FileAssetRepository
) : PromptProvider {
    override val order = 50

    override fun provide(request: ContextRequest): List<ChatMessage> {
        val conversationId = request.conversationId ?: return emptyList()
        val assets = files.forConversation(conversationId)
        if (assets.isEmpty()) return emptyList()
        val manifest = assets.joinToString("\n") { "- ${it.displayName} (${it.mimeType}, ${it.sizeBytes} bytes)" }
        return listOf(ChatMessage(role = "system", content = "Attached files:\n$manifest"))
    }
}

class HistoryPromptProvider(private val messageLimit: Int = 20) : PromptProvider {
    override val order = 90
    override fun provide(request: ContextRequest): List<ChatMessage> = request.history.takeLast(messageLimit)
}
