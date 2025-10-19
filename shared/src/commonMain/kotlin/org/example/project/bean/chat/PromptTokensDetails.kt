package org.example.project.bean.chat
import kotlinx.serialization.Serializable
@Serializable
data class PromptTokensDetails(
    val cached_tokens: Int? = null
)