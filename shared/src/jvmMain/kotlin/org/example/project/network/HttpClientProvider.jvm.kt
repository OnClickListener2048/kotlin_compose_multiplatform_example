package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createHttpClient(): HttpClient {
    return HttpClient(CIO)
}