package org.example.project.network

import io.ktor.client.HttpClient

class MainRepository {
    private val api = ApiService(provideHttpClient())


    suspend fun loadPosts(): String {
        return api.getPosts()
    }
}