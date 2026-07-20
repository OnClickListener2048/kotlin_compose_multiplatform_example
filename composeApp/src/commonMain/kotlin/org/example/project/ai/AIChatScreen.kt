package org.example.project.ai

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
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
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                    ConversationSidebar(
                        conversations = state.conversations.filter { !it.isArchived },
                        currentId = state.currentConversationId,
                        onSelect = {
                            viewModel.selectConversation(it)
                            scope.launch { drawerState.close() }
                        },
                        onNew = {
                            viewModel.newConversation()
                            scope.launch { drawerState.close() }
                        },
                        onDelete = { viewModel.deleteConversation(it) },
                        onTogglePin = { viewModel.togglePin(it) },
                        onToggleArchive = { viewModel.toggleArchive(it) },
                        onSearch = { viewModel.searchConversations(it) },
                        onSettings = {
                            scope.launch { drawerState.close() }
                            navigator.push(AISettingsScreen())
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            val convId = state.currentConversationId
                            val title = state.conversations.find { it.id == convId }?.title
                            Text(title ?: "AI Assistant", maxLines = 1)
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Text("\u2630", fontSize = 22.sp)
                            }
                        },
                        actions = {
                            Text(
                                state.activeProvider.displayName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(onClick = { viewModel.newConversation() }) {
                                Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
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
                        HorizontalDivider()
                        ChatInputBar(
                            text = state.inputText,
                            onTextChange = { viewModel.updateInputText(it) },
                            onSend = { viewModel.sendMessage() },
                            isStreaming = state.isStreaming,
                            onStop = { viewModel.stopGeneration() },
                            onRegenerate = { viewModel.regenerate() },
                            onContinue = { viewModel.continueGeneration() },
                            enabled = state.activeConfig != null,
                            modifier = Modifier.navigationBarsPadding()
                        )
                    }
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
        Text("AI Assistant", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Powered by $providerName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNewChat) {
            Text("New Chat")
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

    Column(modifier = Modifier.fillMaxHeight().padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Conversations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = onNew) { Text("+", fontSize = 22.sp) }
                IconButton(onClick = onSettings) { Text("\u2699", fontSize = 20.sp) }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; onSearch(it) },
            placeholder = { Text("Search...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true
        )

        val sorted = conversations.sortedWith(
            compareByDescending<Conversation> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sorted, key = { it.id }) { conv ->
                val isSelected = conv.id == currentId
                var showMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .clickable { onSelect(conv.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (conv.isPinned) "\uD83D\uDCCC ${conv.title}" else conv.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                            Text(
                                "${conv.providerType.displayName}  ${conv.model}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Text("\u22EE", fontSize = 18.sp)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
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
                                    onClick = { showMenu = false; showDeleteDialog = conv.id }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete") },
                text = { Text("Delete this conversation permanently?") },
                confirmButton = {
                    TextButton(onClick = { onDelete(showDeleteDialog!!); showDeleteDialog = null }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
                }
            )
        }
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
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        items(messages, key = { it.id }) { msg -> ChatBubble(msg) }
        item { Spacer(Modifier.height(4.dp)) }
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
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isQuestion)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp, topEnd = 12.dp,
                bottomStart = if (isQuestion) 12.dp else 4.dp,
                bottomEnd = if (isQuestion) 4.dp else 12.dp
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SelectionContainer {
                    Text(msg.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                }
                if (msg.isLoading) {
                    Spacer(Modifier.height(4.dp))
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                }
            }
        }

        if (isQuestion) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text("U", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        if (isStreaming) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onStop) { Text("\u25A0 Stop") }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onRegenerate) { Text("\u21BB", fontSize = 14.sp) }
                TextButton(onClick = onContinue) { Text("\u25B6", fontSize = 14.sp) }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(if (enabled) "Message..." else "Set API key first") },
            enabled = enabled && !isStreaming,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = onSend,
                    enabled = enabled && text.isNotBlank() && !isStreaming
                ) {
                    Text("\u27A4", fontSize = 18.sp)
                }
            },
            shape = RoundedCornerShape(20.dp),
            maxLines = 4
        )
    }
}
