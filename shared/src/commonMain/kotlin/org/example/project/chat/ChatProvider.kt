package org.example.project.chat

import kotlinx.coroutines.flow.Flow

interface ChatProvider {
    val type: ProviderType

    suspend fun chat(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): Flow<ChatStreamChunk>

    suspend fun chatSync(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): Result<String>
}
