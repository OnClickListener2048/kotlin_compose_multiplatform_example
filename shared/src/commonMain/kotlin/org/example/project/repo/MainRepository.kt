package org.example.project.repo

import com.watson.database.sqldelight.ChatItemQueries
import org.example.project.bean.ChatItemType
import org.example.project.bean.Post
import org.example.project.bean.chat.ChatResponse
import org.example.project.network.ApiService

class MainRepository(
    private val api: ApiService,
    val chatItemQueries: ChatItemQueries
) {

    init {
        println("MainRepository init---$chatItemQueries")
        println("MainRepository init---$api")
    }

    suspend fun loadPosts(): List<Post> {
        return api.getPosts()
    }

    suspend fun talk(content: String, onStop: () -> Unit, onResponse: (ChatResponse) -> Unit) {
        return api.talk(content, onStop, onResponse)
    }

    fun insertChatItem(id: String, type: ChatItemType, content: String, createdAt: Long) {
        chatItemQueries.insertItem(
            id = id,
            type = type,
            content = content,
            createdAt = createdAt
        )


    }

    fun searchAll() = chatItemQueries.selectAllOrderedByTime().executeAsList().toMutableList()




}