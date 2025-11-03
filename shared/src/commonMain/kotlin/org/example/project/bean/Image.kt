package org.example.project.bean

import kotlinx.serialization.Serializable

@Serializable
data class ImageItem(
    val author: String?,
    val download_url: String?,
    val height: Int?,
    val id: String?,
    val url: String?,
    val width: Int?
)