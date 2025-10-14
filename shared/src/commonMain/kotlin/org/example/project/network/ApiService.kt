package org.example.project.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


class ApiService(private val client: HttpClient) {

    suspend fun getPosts(): String {
        val response: HttpResponse = client.get("https://jsonplaceholder.typicode.com/posts") {
            accept(ContentType.Application.Json)
        }
        return response.bodyAsText()
    }

    suspend fun postData(): String {
        val response: HttpResponse = client.post("https://jsonplaceholder.typicode.com/posts") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Hello","body":"KMP + Ktor","userId":1}""")
        }
        return response.bodyAsText()
    }
}