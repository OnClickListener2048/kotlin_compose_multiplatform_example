package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.bean.Post


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

    suspend fun getPosts(): List<Post> {
        return client.get("https://jsonplaceholder.typicode.com/posts").body()
    }

    suspend fun postData(): String {
        val response: HttpResponse = client.post("https://jsonplaceholder.typicode.com/posts") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Hello","body":"KMP + Ktor","userId":1}""")
        }
        return response.bodyAsText()
    }
}