package org.example.project.chat

import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.SSEBufferPolicy
import io.ktor.client.plugins.sse.bufferPolicy
import io.ktor.client.plugins.sse.sse
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class OpenAICompatibleProvider(
    override val type: ProviderType,
    private val client: HttpClient
) : ChatProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun chat(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): Flow<ChatStreamChunk> = flow {
        val request = OpenAIRequest(
            model = config.model,
            messages = messages.map { OpenAIMessage(role = it.role, content = it.content) },
            stream = true,
            max_tokens = config.maxTokens,
            temperature = config.temperature,
            top_p = config.topP
        )

        client.sse(
            urlString = "${config.baseUrl}/chat/completions",
            request = {
                method = HttpMethod.Post
                header("Authorization", "Bearer ${config.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(request)
                bufferPolicy(SSEBufferPolicy.Off)
                timeout {
                    requestTimeoutMillis = 120_000L
                    connectTimeoutMillis = 30_000L
                    socketTimeoutMillis = 120_000L
                }
            }
        ) {
            incoming.collect { event ->
                val data = event.data ?: return@collect
                if (data == "[DONE]") {
                    emit(ChatStreamChunk(content = "", isDone = true))
                    return@collect
                }
                try {
                    val response = json.decodeFromString<OpenAIStreamResponse>(data)
                    val choice = response.choices?.firstOrNull()
                    val delta = choice?.delta
                    val content = delta?.content ?: ""
                    val finishReason = choice?.finish_reason

                    emit(
                        ChatStreamChunk(
                            content = content,
                            isDone = finishReason != null,
                            finishReason = finishReason
                        )
                    )
                } catch (e: Exception) {
                    println("OpenAI SSE parse error: ${e.message}")
                }
            }
        }
    }

    override suspend fun chatSync(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): Result<String> {
        return try {
            val request = OpenAIRequest(
                model = config.model,
                messages = messages.map { OpenAIMessage(role = it.role, content = it.content) },
                stream = false,
                max_tokens = config.maxTokens,
                temperature = config.temperature,
                top_p = config.topP
            )

            val response = client.post("${config.baseUrl}/chat/completions") {
                header("Authorization", "Bearer ${config.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val body = response.bodyAsText()
            val result = json.decodeFromString<OpenAIResponse>(body)
            Result.success(result.choices?.firstOrNull()?.message?.content ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val stream: Boolean = true,
    val max_tokens: Int = 4096,
    val temperature: Float = 0.7f,
    val top_p: Float = 1.0f
)

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIStreamResponse(
    val choices: List<OpenAIStreamChoice>? = null,
    val id: String? = null,
    val model: String? = null
)

@Serializable
data class OpenAIStreamChoice(
    val delta: OpenAIDelta? = null,
    val finish_reason: String? = null,
    val index: Int? = null
)

@Serializable
data class OpenAIDelta(
    val content: String? = null,
    val role: String? = null
)

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIResponseChoice>? = null,
    val id: String? = null,
    val model: String? = null,
    val usage: OpenAIUsage? = null
)

@Serializable
data class OpenAIResponseChoice(
    val message: OpenAIMessage? = null,
    val finish_reason: String? = null
)

@Serializable
data class OpenAIUsage(
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val total_tokens: Int = 0
)
