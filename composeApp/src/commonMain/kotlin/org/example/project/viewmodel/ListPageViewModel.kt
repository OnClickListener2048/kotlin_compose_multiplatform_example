package org.example.project.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.bean.ImageItem
import org.example.project.network.UiState
import org.example.project.network.safeApiCall
import org.example.project.repo.ListPageRepository

class ListPageViewModel(private val listPageRepository: ListPageRepository) : ScreenModel {

    private val _images = MutableStateFlow<UiState<List<ImageItem>>>(UiState.Loading)
    val images: StateFlow<UiState<List<ImageItem>>> = _images

    var page: Int = 1

    init {
        println("init ListPageViewModel")
        screenModelScope.launch {
            _images.value = UiState.Loading
            _images.value = safeApiCall { listPageRepository.getImages() }
        }
    }

    suspend fun refresh() {
        page = 1
        val imageItems = listPageRepository.getImages(page)
        _images.value = UiState.Success(imageItems)
    }

    suspend fun loadMoreImages() {
        page += 1
        val currentState = _images.value
        val imageItems = listPageRepository.getImages(page)
        val updatedList = when (currentState) {
            is UiState.Success -> currentState.data + imageItems // 追加到旧数据后面
            else -> imageItems // 如果不是 Success 状态，直接替换
        }

        _images.value = UiState.Success(updatedList)

    }
}