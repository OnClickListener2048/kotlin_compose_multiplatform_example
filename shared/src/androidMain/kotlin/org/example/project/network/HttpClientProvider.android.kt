package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.*
actual fun createHttpClient(): HttpClient {
    return HttpClient(OkHttp)
}