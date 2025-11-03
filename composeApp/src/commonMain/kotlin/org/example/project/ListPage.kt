package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.imageComponent
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import com.skydoves.landscapist.placeholder.thumbnail.ThumbnailPlugin
import org.example.project.components.CommonTopAppBar
import org.example.project.network.Render
import org.example.project.viewmodel.ListPageViewModel
import org.koin.compose.koinInject

class ListPage : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val currentOrThrow = LocalNavigator.currentOrThrow
        val listPageViewModel = koinInject<ListPageViewModel>()
        val images by listPageViewModel.images.collectAsStateWithLifecycle()
        val component = imageComponent {
            +ShimmerPlugin(
                Shimmer.Flash(
                    baseColor = Color.White,
                    highlightColor = Color.LightGray,
                ),
            )
            +ThumbnailPlugin()
        }
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    title = "List Page",
                    showNavIcon = true,
                    onNavClick = {
                        currentOrThrow.pop()
                    }
                )

            }

        ) { paddingValues ->
            val currentOrThrow = LocalNavigator.currentOrThrow
            images.Render(paddingValues) { it ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    items(it, key = { item -> item.id ?: 0 }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = {}),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            CoilImage(
                                modifier = Modifier.width(100.dp).width(100.dp),
                                imageModel = { it.download_url }, // loading a network image or local resource using an URL.
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                ),
                                component = component
                            )
                        }

                    }
                }
            }

        }

    }

}