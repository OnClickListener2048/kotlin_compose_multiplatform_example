package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    var delta: Delta = Delta(),
    var finish_reason: String = "",
    var index: Int = 0,
    var logprobs: String = ""
)