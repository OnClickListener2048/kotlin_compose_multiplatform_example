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
    primary = androidx.compose.ui.graphics.Color(0xFF5B5BD6),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE8E7FF),
    secondary = androidx.compose.ui.graphics.Color(0xFF006C51),
    surface = androidx.compose.ui.graphics.Color(0xFFFCF9FF),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF1EFF7),
    background = androidx.compose.ui.graphics.Color(0xFFFCF9FF)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFC4C2FF),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF292878),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF41409A),
    secondary = androidx.compose.ui.graphics.Color(0xFF6DDBB3),
    surface = androidx.compose.ui.graphics.Color(0xFF121218),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF202027),
    background = androidx.compose.ui.graphics.Color(0xFF121218)
)

@Composable
fun AIAssistantTheme(content: @Composable () -> Unit) {
    val settings = koinInject<SettingsRepository>()
    val mode by settings.themeMode.collectAsState()
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
