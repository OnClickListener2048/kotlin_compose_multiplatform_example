package org.example.project.repo

import org.example.project.bean.ImageItem
import org.example.project.network.ApiService

class ListPageRepository(private val apiService: ApiService) {

    suspend fun getImages(page: Int = 1, limit: Int = 10): List<ImageItem> {
        return apiService.getImages(page = page, limit = limit)
    }
}