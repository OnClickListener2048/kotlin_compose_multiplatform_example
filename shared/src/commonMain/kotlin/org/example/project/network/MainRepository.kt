package org.example.project.network

import io.ktor.client.HttpClient
import org.example.project.bean.Post

class MainRepository {
    private val api = ApiService(provideHttpClient())


    suspend fun loadPosts(): List<Post> {
        return api.getPosts()
    }
}