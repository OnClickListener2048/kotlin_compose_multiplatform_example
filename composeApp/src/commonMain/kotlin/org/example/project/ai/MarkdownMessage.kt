package org.example.project.ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown

/**
 * KMP-safe Markdown renderer for assistant messages.
 *
 * The renderer is intentionally isolated from chat state. Image, file, tool-result,
 * and thinking renderers can be added to the same message-content dispatch point.
 */
@Composable
internal fun MarkdownMessage(markdown: String, modifier: Modifier = Modifier) {
    Markdown(content = markdown, modifier = modifier)
}
