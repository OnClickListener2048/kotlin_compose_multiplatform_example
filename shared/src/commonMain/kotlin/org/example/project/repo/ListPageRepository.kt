package org.example.project.repo

import org.example.project.bean.ImageItem
import org.example.project.network.ApiService

class ListPageRepository(private val apiService: ApiService) {

    suspend fun getImages(): List<ImageItem> {
        return apiService.getImages(limit = 10)
    }
}