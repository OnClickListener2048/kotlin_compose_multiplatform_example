package org.example.project.bean

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ChatItem @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    var content: String,
    val type: ChatItemType,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    var isLoading: Boolean = false
)

enum class ChatItemType {
    Question,
    Answer
}
