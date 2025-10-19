package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class DeepRequest(
    var frequency_penalty: Int = 0,
    var logprobs: Boolean = false,
    var max_tokens: Int = 128,
    var messages: List<Message> = listOf(),
    var model: String = "deepseek-chat",
    var presence_penalty: Int = 0,
    var response_format: ResponseFormat = ResponseFormat(),
    var stop: String? = null,
    var stream: Boolean = true,
    var stream_options: String? = null,
    var temperature: Int = 1,
    var tool_choice: String = "none",
    var tools: String? = null,
    var top_logprobs: String? = null,
    var top_p: Int = 1
)