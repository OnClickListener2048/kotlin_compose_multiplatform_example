package org.example.project

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.example.project.components.CommonTopAppBar
import org.example.project.viewmodel.HomeViewModel

class HomePage : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Scaffold (
            topBar = {
                CommonTopAppBar(
                    "Home Page",
                )
            }
        ){ paddingValues ->

            val homeViewModel = remember { HomeViewModel() }
            LaunchedEffect(Unit){
                homeViewModel.loadPosts()
            }

            DisposableEffect(Unit){
                onDispose {
                    homeViewModel.clear()
                }
            }
            val currentOrThrow = LocalNavigator.current
            val posts by homeViewModel.posts.collectAsState()
            Text(posts ?: "Loading...", modifier = Modifier.padding(paddingValues))
            LazyColumn {

            }
        }

    }


}