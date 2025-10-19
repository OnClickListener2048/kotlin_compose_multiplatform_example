package org.example.project.bean.chat

import kotlinx.serialization.Serializable

@Serializable
data class Usage(
    val completion_tokens: Int? = null,
    val prompt_cache_hit_tokens: Int? = null,
    val prompt_cache_miss_tokens: Int? = null,
    val prompt_tokens: Int? = null,
    val prompt_tokens_details: PromptTokensDetails? = null,
    val total_tokens: Int? = null
)