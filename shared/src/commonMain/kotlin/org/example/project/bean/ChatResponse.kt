package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    var choices: List<Choice> = listOf(),
    var created: Int = 0,
    var id: String = "",
    var model: String = "",
    var `object`: String = "",
    var system_fingerprint: String = "",
    var usage: Usage = Usage()
)