package org.example.project.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatStreamChunk(
    val content: String,
    val isDone: Boolean = false,
    val finishReason: String? = null,
    val usage: ChatUsage? = null
)

@Serializable
data class ChatUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

data class ProviderConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val maxTokens: Int = 4096,
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val systemPrompt: String? = null
)
