package org.example.project.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.example.project.bean.ChatItemType
import org.example.project.repo.Conversation
import org.example.project.viewmodel.AIChatViewModel
import org.koin.compose.koinInject

class AIChatScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinInject<AIChatViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = state.showSidebar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ConversationSidebar(
                    conversations = state.conversations.filter { !it.isArchived },
                    currentId = state.currentConversationId,
                    onSelect = { viewModel.selectConversation(it) },
                    onNew = { viewModel.newConversation() },
                    onDelete = { viewModel.deleteConversation(it) },
                    onTogglePin = { viewModel.togglePin(it) },
                    onToggleArchive = { viewModel.toggleArchive(it) },
                    onSearch = { viewModel.searchConversations(it) },
                    onSettings = { navigator.push(AISettingsScreen()) }
                )
            }

            Box(
                modifier = Modifier.fillMaxHeight().width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TopAppBar(
                    title = {
                        val convId = state.currentConversationId
                        val title = state.conversations.find { it.id == convId }?.title ?: "AI Assistant"
                        Text(title, maxLines = 1)
                    },
                    actions = {
                        val providerLabel = state.activeProvider.displayName
                        Text(providerLabel, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                HorizontalDivider()

                if (state.currentConversationId == null) {
                    WelcomeScreen(
                        onNewChat = { viewModel.newConversation() },
                        providerName = state.activeProvider.displayName
                    )
                } else {
                    ChatMessagesArea(
                        messages = state.messages,
                        isStreaming = state.isStreaming,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.currentConversationId != null) {
                    ChatInputBar(
                        text = state.inputText,
                        onTextChange = { viewModel.updateInputText(it) },
                        onSend = { viewModel.sendMessage() },
                        isStreaming = state.isStreaming,
                        onStop = { viewModel.stopGeneration() },
                        onRegenerate = { viewModel.regenerate() },
                        onContinue = { viewModel.continueGeneration() },
                        enabled = state.activeConfig != null
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeScreen(onNewChat: () -> Unit, providerName: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AI Assistant", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Powered by $providerName", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNewChat) {
            Text("+ New Chat")
        }
    }
}

@Composable
private fun ConversationSidebar(
    conversations: List<Conversation>,
    currentId: String?,
    onSelect: (String) -> Unit,
    onNew: () -> Unit,
    onDelete: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleArchive: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSettings: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.width(280.dp).fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Conversations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                TextButton(onClick = onNew) { Text("+") }
                TextButton(onClick = onSettings) { Text("\u2699") }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it)
            },
            placeholder = { Text("Search...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        val sorted = conversations.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.updatedAt })

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sorted, key = { it.id }) { conv ->
                val isSelected = conv.id == currentId
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .clickable { onSelect(conv.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (conv.isPinned) {
                                        Text("\uD83D\uDCCC ", fontSize = 12.sp)
                                    }
                                    Text(conv.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                                Text(
                                    "${conv.providerType.displayName}  ${conv.model}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { showMenu = true }) {
                                Text("\u22EE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (conv.isPinned) "Unpin" else "Pin") },
                            onClick = { onTogglePin(conv.id); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = { onToggleArchive(conv.id); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = conv.id
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Conversation") },
            text = { Text("This will permanently delete this conversation and all its messages.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(showDeleteDialog!!)
                    showDeleteDialog = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ChatMessagesArea(
    messages: List<org.example.project.repo.ChatItem>,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.content) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        items(messages, key = { it.id }) { msg ->
            ChatBubble(msg)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ChatBubble(msg: org.example.project.repo.ChatItem) {
    val isQuestion = msg.type == ChatItemType.Question

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isQuestion) Arrangement.End else Arrangement.Start
    ) {
        if (!isQuestion) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isQuestion)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isQuestion) 16.dp else 4.dp,
                bottomEnd = if (isQuestion) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SelectionContainer {
                    Text(
                        text = msg.content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
                if (msg.isLoading) {
                    Spacer(Modifier.height(4.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        if (isQuestion) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text("U", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isStreaming: Boolean,
    onStop: () -> Unit,
    onRegenerate: () -> Unit,
    onContinue: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (isStreaming) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onStop) { Text("\u25A0 Stop") }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRegenerate) { Text("\u21BB Regenerate") }
                TextButton(onClick = onContinue) { Text("\u25B6 Continue") }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(if (enabled) "Type a message..." else "Configure API key first...") },
            enabled = enabled && !isStreaming,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                TextButton(onClick = onSend, enabled = enabled && text.isNotBlank() && !isStreaming) {
                    Text("\u27A4 Send")
                }
            },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4
        )
    }
}
