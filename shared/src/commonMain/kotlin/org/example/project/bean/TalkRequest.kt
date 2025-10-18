package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class TalkRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)