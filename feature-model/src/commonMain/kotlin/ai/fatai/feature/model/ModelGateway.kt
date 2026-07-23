package ai.fatai.feature.model

import kotlinx.coroutines.flow.Flow
import ai.fatai.chat.ChatMessage
import ai.fatai.chat.ChatProvider
import ai.fatai.chat.ChatStreamChunk
import ai.fatai.chat.ProviderConfig

/** Model feature boundary. New providers only need to implement this gateway or a ChatProvider adapter. */
interface ModelGateway {
    suspend fun stream(messages: List<ChatMessage>, config: ProviderConfig): Flow<ChatStreamChunk>
}

class ChatProviderModelGateway(private val provider: ChatProvider) : ModelGateway {
    override suspend fun stream(messages: List<ChatMessage>, config: ProviderConfig): Flow<ChatStreamChunk> =
        provider.chat(messages, config)
}
