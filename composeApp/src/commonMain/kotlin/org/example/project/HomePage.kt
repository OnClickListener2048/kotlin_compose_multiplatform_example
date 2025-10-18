package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.components.CommonTopAppBar
import org.example.project.network.Render
import org.example.project.viewmodel.HomeViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

class HomePage : Screen {
    @Preview
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    "Home Page",
                )
            }
        ) { paddingValues ->
            val homeViewModel = rememberScreenModel {
                HomeViewModel()
            }

            DisposableEffect(Unit) {
                onDispose {
                }
            }
            val currentOrThrow = LocalNavigator.currentOrThrow
            val posts by homeViewModel.posts.collectAsStateWithLifecycle()
            posts.Render(paddingValues) { it ->
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(it.size, key = { index ->
                        it[index].id ?: 0
                    }) { index ->
                        ListItem(
                            headlineContent = { Text(it[index].title ?: "") },
                            supportingContent = { Text(it[index].body ?: "") },
                            trailingContent = { Text("ID: ${it[index].id ?: 0}") },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clickable {
                                    currentOrThrow.push(ListPage)
                                }
                        )
                    }
                }

            }



        }

    }
}