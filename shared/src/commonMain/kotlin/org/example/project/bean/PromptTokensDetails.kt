package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class PromptTokensDetails(
    var cached_tokens: Int = 0
)