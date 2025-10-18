package org.example.project.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.bean.Post
import org.example.project.network.MainRepository
import org.example.project.network.UiState
import org.example.project.network.safeApiCall

class HomeViewModel : ScreenModel {


    private val repository = MainRepository
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _posts = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val posts: StateFlow<UiState<List<Post>>> = _posts

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _posts.value = UiState.Loading
            _posts.value = safeApiCall { repository.loadPosts() }
        }
    }

    override fun onDispose() {
        super.onDispose()
        viewModelScope.cancel()
    }

}