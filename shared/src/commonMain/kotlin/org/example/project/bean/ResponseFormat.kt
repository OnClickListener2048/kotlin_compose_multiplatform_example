package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class ResponseFormat(
    var type: String = "text"
)