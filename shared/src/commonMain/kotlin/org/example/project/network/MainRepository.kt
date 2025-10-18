package org.example.project.network

import io.ktor.client.HttpClient
import org.example.project.bean.ChatResponse
import org.example.project.bean.Post

object MainRepository {
    private val api = ApiService(provideHttpClient())


    suspend fun loadPosts(): List<Post> {
        return api.getPosts()
    }
    suspend fun talk(content: String, onStop: () -> Unit, onResponse: (ChatResponse) -> Unit) {
        return api.talk(content, onStop, onResponse)
    }
}