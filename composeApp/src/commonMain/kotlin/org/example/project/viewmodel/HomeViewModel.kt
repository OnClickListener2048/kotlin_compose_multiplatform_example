package org.example.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.network.MainRepository

class HomeViewModel {
    private val repository = MainRepository()
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _posts = MutableStateFlow<String?>(null)
    val posts: StateFlow<String?> = _posts

    fun loadPosts() {
        viewModelScope.launch {
            try {
                val data = repository.loadPosts()
                _posts.value = data
            } catch (e: Exception) {
                _posts.value = "Error: ${e.message}"
            }
        }
    }

    fun clear() {
        viewModelScope.cancel()
    }
}