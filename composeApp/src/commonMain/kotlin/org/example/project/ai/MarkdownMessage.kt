package org.example.project.ai

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography

/**
 * KMP-safe Markdown renderer for assistant messages.
 *
 * The renderer is intentionally isolated from chat state. Image, file, tool-result,
 * and thinking renderers can be added to the same message-content dispatch point.
 */
@Composable
internal fun MarkdownMessage(
    markdown: String,
    compactLayout: Boolean,
    modifier: Modifier = Modifier
) {
    val body = MaterialTheme.typography.bodyMedium.copy(
        fontSize = if (compactLayout) 14.sp else 15.sp,
        lineHeight = if (compactLayout) 20.sp else 22.sp
    )
    Markdown(
        content = markdown,
        modifier = modifier,
        typography = markdownTypography(
            h1 = MaterialTheme.typography.headlineSmall.copy(
                fontSize = if (compactLayout) 22.sp else 28.sp,
                lineHeight = if (compactLayout) 28.sp else 34.sp
            ),
            h2 = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (compactLayout) 19.sp else 23.sp,
                lineHeight = if (compactLayout) 25.sp else 30.sp
            ),
            h3 = MaterialTheme.typography.titleMedium.copy(
                fontSize = if (compactLayout) 17.sp else 19.sp,
                lineHeight = if (compactLayout) 23.sp else 26.sp
            ),
            h4 = MaterialTheme.typography.titleMedium.copy(fontSize = if (compactLayout) 16.sp else 18.sp),
            h5 = MaterialTheme.typography.titleSmall.copy(fontSize = if (compactLayout) 15.sp else 16.sp),
            h6 = MaterialTheme.typography.titleSmall.copy(fontSize = if (compactLayout) 14.sp else 15.sp),
            text = body,
            paragraph = body,
            ordered = body,
            bullet = body,
            list = body,
            quote = body,
            table = body,
            code = body.copy(fontFamily = FontFamily.Monospace, fontSize = if (compactLayout) 13.sp else 14.sp),
            inlineCode = body.copy(fontFamily = FontFamily.Monospace, fontSize = if (compactLayout) 13.sp else 14.sp),
            textLink = TextLinkStyles(
                style = body.copy(fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline).toSpanStyle()
            )
        )
    )
}
