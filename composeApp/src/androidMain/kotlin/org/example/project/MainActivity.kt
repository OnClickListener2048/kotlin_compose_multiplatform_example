package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.example.project.database.DatabaseDriverFactory
import org.example.project.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin {
            // 可选：添加 Android 特定的 Koin 配置
            androidLogger(Level.DEBUG) // 使用 Koin 的日志记录
            androidContext(this@MainActivity)
        }

        FileKit.init(this)
        setContent {
           App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}