package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft

object ListPage : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val currentOrThrow = LocalNavigator.currentOrThrow
        MaterialTheme {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(title = { Text("List Page") }, navigationIcon = {
                        Icon(
                            imageVector = FeatherIcons.ArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                currentOrThrow.pop()
                            }.padding(10.dp)
                        )
                    })

                }

            ) {
                LazyColumn {
                    items(100) {
                        ListItem(
                            headlineContent = {
                                Text("headlineContent #$it")
                            },

                            supportingContent = { Text("supportingContent #$it") },
                            overlineContent = { Text("overlineContent #$it") },
                            leadingContent ={  AsyncImage(
                                modifier = Modifier.width(100.dp).height(100.dp),
                                model = "https://pic.pngsucai.com/00/93/02/7078c187bed38f26.webp",
                                contentDescription = null,
                            ) },
                            trailingContent = { Text("trailingContent #$it") },
                            modifier = Modifier.padding(5.dp)
                        )

                    }
                }
            }
        }

    }

}