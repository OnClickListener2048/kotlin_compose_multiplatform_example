package org.example.project.bean.chat

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val delta: Delta? = null,
    val finish_reason: String? = null,
    val index: Int? = null,
    val logprobs: String? = null
)