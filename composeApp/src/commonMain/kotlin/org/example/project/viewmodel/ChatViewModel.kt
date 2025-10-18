package org.example.project.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.bean.ChatItem
import org.example.project.bean.ChatItemType
import org.example.project.bean.ChatResponse
import org.example.project.network.MainRepository
import org.example.project.network.safeApiCall

class ChatViewModel : ScreenModel {
    private val repository = MainRepository

    private val _question = MutableStateFlow("如何使用AI软件")
    val question = _question.asStateFlow()

    private val _chatList = MutableStateFlow(listOf<ChatItem>())

    private val answer = MutableStateFlow("")
     val answerFlow = answer.asStateFlow()

    init {
        repeat(10) {
            val questionItem = ChatItem(
                type = ChatItemType.Question,
                content = "Question $it: What is the meaning of life?"
            )
            val answerItem = ChatItem(
                type = ChatItemType.Answer,
                content = "Answer $it: The meaning of life is a philosophical question."
            )
            _chatList.value = _chatList.value + questionItem + answerItem
        }
    }

    val chatList = _chatList.asStateFlow()

    fun updateQuestion(newQuestion: String) {
        _question.value = newQuestion
    }

    suspend fun talk(question: String) {
        _chatList.value = _chatList.value + ChatItem(
            type = ChatItemType.Question,
            content = question
        )
        val chatItem = ChatItem(
            type = ChatItemType.Answer,
            content = "",
            isLoading = true
        )
        _chatList.value = _chatList.value + chatItem
        safeApiCall {
            repository.talk(question, onStop = {
                chatItem.isLoading = false
            }, onResponse = { response: ChatResponse ->
                answer.value +=response.choices[0].delta.content
            })
        }
    }

}