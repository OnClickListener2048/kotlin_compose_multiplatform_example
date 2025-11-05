package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.LocalImageComponent
import kotlinx.coroutines.launch
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
        var refreshing by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        val lazyGridState = rememberLazyGridState()
        val pullToRefreshState = rememberPullToRefreshState()
        val coroutineScope = rememberCoroutineScope()

        // 检测滚动到达底部
        val shouldLoadMore by remember {
            derivedStateOf {
                val lastVisible = lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = lazyGridState.layoutInfo.totalItemsCount
                total > 0 && lastVisible == total -3
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore && !isLoading) {

                println("Load more items...")
                coroutineScope.launch {
                    isLoading = true
                    listPageViewModel.loadMoreImages()
                    isLoading = false
                }

            }
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
                PullToRefreshBox(
                    modifier = Modifier.padding(paddingValues),
                    isRefreshing = refreshing,
                    state = pullToRefreshState,
                    onRefresh = {
                        println("onRefresh called")
                        coroutineScope.launch {
                            refreshing = true
                            listPageViewModel.refresh()
                            refreshing = false
                            pullToRefreshState.animateToHidden()
                        }
                    }) {
                    LazyVerticalGrid(
                        state = lazyGridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(it, key = { item -> item.id ?: 0 }) {
                            var imageSize by remember { mutableStateOf(IntSize.Zero) }
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = {}),
                                elevation = CardDefaults.cardElevation(40.dp)
                            ) {
                                CoilImage(
                                    modifier = Modifier.fillMaxWidth()
                                        .onSizeChanged { size ->
                                            println(size)
                                            if (imageSize != IntSize.Zero) {
                                                imageSize = size
                                            }

                                        }
                                        .height(200.dp)
                                        .aspectRatio(1f),
                                    imageModel = { it.download_url },
                                    imageOptions = ImageOptions(
                                        requestSize = IntSize(imageSize.width, imageSize.height),
                                        contentScale = ContentScale.Crop
                                    ),
                                    failure = {
                                        Text(text = "image request failed.")
                                    },
                                    component = LocalImageComponent.current

                                )
                                Text(text = "${it.author}", modifier = Modifier.padding(8.dp))
                            }

                        }
                        if (isLoading) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }

        }

    }

}