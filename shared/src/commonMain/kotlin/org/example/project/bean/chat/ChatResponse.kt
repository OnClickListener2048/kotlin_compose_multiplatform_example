package org.example.project.bean.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val choices: List<Choice?>? = null,
    val created: Int? = null,
    val id: String? = null,
    val model: String? = null,
    val `object`: String? = null,
    val system_fingerprint: String? = null,
    val usage: Usage? = null
)