package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.components.CommonTopAppBar

class LoginPage : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var username by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }

        Scaffold(
            topBar = { CommonTopAppBar(title = "Login", showNavIcon = false, onNavClick = {}) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome", modifier = Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.headlineSmall)
                TextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.padding(bottom = 8.dp))
                TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.padding(bottom = 16.dp))
                Button(onClick = {
                    // 简单本地验证：非空即视为成功
                    if (username.isNotBlank() && password.isNotBlank()) {
                        navigator.push(ListPage())
                    }
                }) {
                    Text("Login")
                }
            }
        }
    }
}
