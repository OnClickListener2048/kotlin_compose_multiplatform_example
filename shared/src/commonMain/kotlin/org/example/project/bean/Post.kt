package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val body: String?,
    val id: Int?,
    val title: String?,
    val userId: Int?
) : A {
    override fun a(): String {
        return "Post Title: $title"
    }

    data class B(
        val info: String
    )
}

interface A {
    fun a(): String
}