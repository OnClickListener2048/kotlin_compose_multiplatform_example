package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.SSEBufferPolicy
import io.ktor.client.plugins.sse.bufferPolicy
import io.ktor.client.plugins.sse.sse
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headersOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.bean.Content
import org.example.project.bean.DeepRequest
import org.example.project.bean.Message
import org.example.project.bean.Part
import org.example.project.bean.Post
import org.example.project.bean.TalkRequest
import org.example.project.bean.chat.ChatResponse


sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}

suspend fun <T> safeApiCall(block: suspend () -> T): UiState<T> {
    return try {
        val data = block()
        UiState.Success(data)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Unknown error", e)
    }
}


class ApiService(private val client: HttpClient) {

    val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getPosts(): List<Post> {
        return client.get("https://jsonplaceholder.typicode.com/posts").body()
    }

    suspend fun talk(content: String, onStop: () -> Unit, onResponse: (ChatResponse) -> Unit) {

        client.sse(
            "https://api.deepseek.com/chat/completions",
            request = {
                method = HttpMethod.Post
                header("Authorization", "Bearer sk-d85b8e862a504c73a56b3409a6f47103")
                contentType(ContentType.Application.Json)
                setBody(DeepRequest(messages = listOf(Message(content = content, role = "user"))))
                bufferPolicy(SSEBufferPolicy.Off)
                timeout {
                    requestTimeoutMillis = 60000L
                    connectTimeoutMillis = 60000L
                    socketTimeoutMillis = 60000L
                }
            },
        ) {
            incoming.collect { event ->
                println("event.data---"+event.data)
                if (event.data == "[DONE]") {
                    onStop.invoke()
                    return@collect
                } else {
                    val data = event.data
                    val chatResponse = json.decodeFromString<ChatResponse>(data ?: "")
                    onResponse.invoke(chatResponse)
                }

            }

        }
    }

    suspend fun postData(): String {
        val response: HttpResponse = client.post("https://jsonplaceholder.typicode.com/posts") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Hello","body":"KMP + Ktor","userId":1}""")
        }
        return response.bodyAsText()
    }
}