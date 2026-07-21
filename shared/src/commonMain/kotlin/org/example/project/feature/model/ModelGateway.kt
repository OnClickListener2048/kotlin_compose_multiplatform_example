package org.example.project.feature.model

import kotlinx.coroutines.flow.Flow
import org.example.project.chat.ChatMessage
import org.example.project.chat.ChatProvider
import org.example.project.chat.ChatStreamChunk
import org.example.project.chat.ProviderConfig

/** Model feature boundary. New providers only need to implement this gateway or a ChatProvider adapter. */
interface ModelGateway {
    suspend fun stream(messages: List<ChatMessage>, config: ProviderConfig): Flow<ChatStreamChunk>
}

class ChatProviderModelGateway(private val provider: ChatProvider) : ModelGateway {
    override suspend fun stream(messages: List<ChatMessage>, config: ProviderConfig): Flow<ChatStreamChunk> =
        provider.chat(messages, config)
}
