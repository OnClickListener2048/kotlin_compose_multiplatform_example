package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class Usage(
    var completion_tokens: Int = 0,
    var prompt_cache_hit_tokens: Int = 0,
    var prompt_cache_miss_tokens: Int = 0,
    var prompt_tokens: Int = 0,
    var prompt_tokens_details: PromptTokensDetails = PromptTokensDetails(),
    var total_tokens: Int = 0
)