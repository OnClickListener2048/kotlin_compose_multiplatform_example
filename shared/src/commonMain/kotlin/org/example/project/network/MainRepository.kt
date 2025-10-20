package org.example.project.network

import com.watson.database.sqldelight.ChatItemQueries
import org.example.project.bean.ChatItemType
import org.example.project.bean.Post
import org.example.project.bean.chat.ChatResponse

class MainRepository(
    private val api: ApiService,
    val chatItemQueries: ChatItemQueries
) {

    init {
        println("MainRepository init---$chatItemQueries")
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