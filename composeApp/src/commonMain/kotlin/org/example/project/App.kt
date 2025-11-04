package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.example.project.di.initKoin
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalVoyagerApi::class)
@Composable
@Preview
fun App() {

    MaterialTheme(colorScheme = MaterialTheme.colorScheme) {
        Navigator(ListPage()) { navigator ->
            SlideTransition(navigator)
        }
    }

}

fun initKoinApp() {
    // 初始化 Koin 应用程序模块
    initKoin {
        // 在这里可以添加其他 Koin 配置
    }
}

