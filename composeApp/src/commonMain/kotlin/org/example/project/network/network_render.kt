package org.example.project.network

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> UiState<T>.Render(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onError: @Composable (String) -> Unit = {
        Text("❌ $it", modifier = Modifier.padding(paddingValues))
    },
    onLoading: @Composable () -> Unit = {
        CircularProgressIndicator(
             modifier = Modifier.padding(paddingValues)
        )
    },
    onSuccess: @Composable (T) -> Unit,
) {
    when (this) {
        is UiState.Loading -> onLoading()
        is UiState.Error -> onError(message + throwable?.message)
        is UiState.Success -> onSuccess(data)
    }
}