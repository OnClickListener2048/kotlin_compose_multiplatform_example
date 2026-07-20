package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.skydoves.landscapist.coil3.CoilImage
import org.example.project.bean.ImageItem
import org.example.project.components.CommonTopAppBar

class DetailPage(private val item: ImageItem) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val current = LocalNavigator.currentOrThrow
        Scaffold(topBar = {
            CommonTopAppBar(title = "Detail", showNavIcon = true, onNavClick = { current.pop() })
        }) { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)) {
                if (item.download_url != null) {
                    CoilImage(imageModel = { item.download_url })
                } else {
                    CircularProgressIndicator()
                }
                Text(text = "Author: " + (item.author ?: "Unknown"), modifier = Modifier.padding(top = 12.dp))
                Text(text = "Width: ${item.width ?: 0}  Height: ${item.height ?: 0}", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
