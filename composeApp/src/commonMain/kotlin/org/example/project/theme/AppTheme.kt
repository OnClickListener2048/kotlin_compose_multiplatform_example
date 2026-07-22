package org.example.project.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.example.project.feature.settings.SettingsRepository
import org.example.project.feature.settings.ThemeMode
import org.koin.compose.koinInject

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF18181B),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE5E5E7),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF18181B),
    secondary = androidx.compose.ui.graphics.Color(0xFF52525B),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF4F4F5),
    background = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
    outline = androidx.compose.ui.graphics.Color(0xFFE4E4E7)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFF4F4F5),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF18181B),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF27272A),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFF4F4F5),
    secondary = androidx.compose.ui.graphics.Color(0xFFA1A1AA),
    surface = androidx.compose.ui.graphics.Color(0xFF18181B),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF27272A),
    background = androidx.compose.ui.graphics.Color(0xFF09090B),
    outline = androidx.compose.ui.graphics.Color(0xFF3F3F46)
)

@Composable
fun FatAITheme(content: @Composable () -> Unit) {
    val settings = koinInject<SettingsRepository>()
    val mode by settings.themeMode.collectAsState()
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
