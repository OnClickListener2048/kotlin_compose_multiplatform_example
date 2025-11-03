package org.example.project.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.bean.ChatItem
import org.example.project.bean.ChatItemType
import org.example.project.bean.chat.ChatResponse
import org.example.project.repo.MainRepository
import org.example.project.network.safeApiCall

class ChatViewModel(val repository: MainRepository) : ScreenModel {

    private val _question = MutableStateFlow("如何使用AI软件")
    val question = _question.asStateFlow()

    private val _chatList = MutableStateFlow(listOf<ChatItem>())

    init {
        _chatList.value = repository.searchAll().map { chatItem -> ChatItem(chatItem.id, chatItem.content, chatItem.type, chatItem.createdAt) }
    }

    val chatList = _chatList.asStateFlow()

    fun updateQuestion(newQuestion: String) {
        _question.value = newQuestion
    }

    suspend fun talk(question: String, onUpdating: (ChatItem) -> Unit) {
        val questionItem = ChatItem(
            type = ChatItemType.Question,
            content = question
        )
        repository.insertChatItem(
            questionItem.id,
            questionItem.type,
            questionItem.content,
            questionItem.createdAt
        )
        _chatList.value = _chatList.value + questionItem
        onUpdating.invoke(questionItem)
        var chatItem = ChatItem(
            type = ChatItemType.Answer,
            content = "",
        )
        _chatList.value = _chatList.value + chatItem
        chatItem = chatItem.copy(isLoading = true)
        updateChatItem(chatItem)
        safeApiCall {
            repository.talk(question, onStop = {
                chatItem = chatItem.copy(isLoading = false)
                updateChatItem(chatItem)
                repository.insertChatItem(
                    chatItem.id,
                    chatItem.type,
                    chatItem.content,
                    chatItem.createdAt
                )
            }, onResponse = { response: ChatResponse ->
                println("ChatResponse---" + response.choices?.get(0)?.delta?.content)
                chatItem =
                    chatItem.copy(content = chatItem.content + response.choices?.get(0)?.delta?.content)
                updateChatItem(chatItem)
                onUpdating.invoke(chatItem)
            })
        }
    }

    fun clearQuestion() {
        _question.value = ""
    }


    fun updateChatItem(updatedItem: ChatItem) {
        _chatList.value = _chatList.value.map {
            if (it.id == updatedItem.id) {
                updatedItem
            } else {
                it
            }
        }
    }

}