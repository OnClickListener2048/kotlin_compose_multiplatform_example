package org.example.project.bean.chat

import kotlinx.serialization.Serializable

@Serializable
data class Delta(
    val content: String? = null
)