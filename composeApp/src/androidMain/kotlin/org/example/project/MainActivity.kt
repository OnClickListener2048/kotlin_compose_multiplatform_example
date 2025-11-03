package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.util.DebugLogger
import com.skydoves.landscapist.coil3.LocalCoilImageLoader
import com.skydoves.landscapist.components.LocalImageComponent
import com.skydoves.landscapist.components.imageComponent
import com.skydoves.landscapist.crossfade.CrossfadePlugin
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.example.project.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(this, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.2)
                    .build()
            }
            .diskCachePolicy(CachePolicy.DISABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .logger(DebugLogger())
            .build()

        initKoin {
            // 可选：添加 Android 特定的 Koin 配置
            androidLogger(Level.DEBUG) // 使用 Koin 的日志记录
            androidContext(this@MainActivity)
        }

        FileKit.init(this)



        setContent {

            CompositionLocalProvider(LocalCoilImageLoader provides imageLoader) {
                val component = imageComponent {
                    +ShimmerPlugin(
                        Shimmer.Flash(
                            baseColor = Color.White,
                            highlightColor = Color.LightGray,
                        ),
                    )
                }

                CompositionLocalProvider(LocalImageComponent provides component) {
                    App()
                }

            }

        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

// 错误：`address` 不会影响 `equals()` 和 `hashCode()` 方法
data class Person(val name: String, val age: Int) {
    val address: String = "Unknown"

    override fun toString(): String {
        return super.toString()
    }
}

sealed class User {
    abstract val name: String
}

data class FreeUser(override val name: String) : User()
data class PremiumUser(override val name: String, val stars: Int) : User()

data class Response<T>(
    val data: T,
    val statusCode: Int,
    val message: String
)