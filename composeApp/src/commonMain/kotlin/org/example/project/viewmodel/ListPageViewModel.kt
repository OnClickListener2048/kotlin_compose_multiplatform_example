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

    init {
        println("init ListPageViewModel")
        screenModelScope.launch {
            _images.value = UiState.Loading
            _images.value = safeApiCall { listPageRepository.getImages() }
        }
    }
}