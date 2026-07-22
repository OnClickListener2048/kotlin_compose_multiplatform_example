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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import kotlinx.coroutines.launch
import org.example.project.bean.ChatItemType
import org.example.project.repo.Conversation
import org.example.project.feature.workspace.Workspace
import org.example.project.feature.files.FileAsset
import org.example.project.viewmodel.AIChatViewModel
import org.koin.compose.koinInject
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.Folder
import compose.icons.feathericons.Menu
import compose.icons.feathericons.MoreVertical
import compose.icons.feathericons.Plus
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.Send
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Square
import compose.icons.feathericons.Paperclip

class AIChatScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(onSettings: () -> Unit) {
        val viewModel = koinInject<AIChatViewModel>()
        var state by remember { mutableStateOf(viewModel.state.value) }

        LaunchedEffect(viewModel) {
            viewModel.state.collect { state = it }
        }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val filePicker = rememberFilePickerLauncher(
            type = FileKitType.File(listOf("pdf", "doc", "docx", "xls", "xlsx", "md", "txt", "png", "jpg", "jpeg", "webp")),
            title = "Attach file"
        ) { file ->
            if (file != null) {
                viewModel.attachFile(
                    displayName = file.name,
                    mimeType = file.mimeType()?.toString() ?: "application/octet-stream",
                    localPath = file.toString(),
                    sizeBytes = file.size()
                )
            }
        }

        LaunchedEffect(Unit) {
            viewModel.toastEvents.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                    ConversationSidebar(
                        conversations = state.conversations.filter { !it.isArchived },
                        workspaces = state.workspaces,
                        currentWorkspaceId = state.currentWorkspaceId,
                        currentId = state.currentConversationId,
                        onSelectWorkspace = { viewModel.selectWorkspace(it) },
                        onCreateWorkspace = { name, prompt -> viewModel.createWorkspace(name, prompt) },
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
                            onSettings()
                        }
                    )
                }
            }
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            val convId = state.currentConversationId
                            val title = state.conversations.find { it.id == convId }?.title
                            Text(title ?: "FatAI", maxLines = 1)
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(FeatherIcons.Menu, contentDescription = "Open conversations")
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
                                Icon(FeatherIcons.Plus, contentDescription = "New conversation")
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
                            attachments = state.attachments,
                            onAttach = { filePicker.launch() },
                            onRemoveAttachment = { viewModel.removeAttachment(it) },
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
        Text("FatAI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your context-first AI workspace",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Open the sidebar (\u2630) and go to Settings (\u2699) to configure an API key.",
            style = MaterialTheme.typography.bodySmall,
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
    workspaces: List<Workspace>,
    currentWorkspaceId: String,
    currentId: String?,
    onSelectWorkspace: (String) -> Unit,
    onCreateWorkspace: (String, String) -> Unit,
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
    var workspaceMenuExpanded by remember { mutableStateOf(false) }
    var showCreateWorkspace by remember { mutableStateOf(false) }
    val activeWorkspace = workspaces.find { it.id == currentWorkspaceId }

    Column(modifier = Modifier.fillMaxHeight().padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Workspace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = onNew) { Icon(FeatherIcons.Plus, contentDescription = "New conversation") }
                IconButton(onClick = onSettings) { Icon(FeatherIcons.Settings, contentDescription = "Settings") }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { workspaceMenuExpanded = true },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(FeatherIcons.Folder, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(activeWorkspace?.name ?: "Personal", modifier = Modifier.weight(1f), maxLines = 1)
                    Icon(FeatherIcons.ChevronDown, contentDescription = "Switch workspace", modifier = Modifier.size(16.dp))
                }
            }
            DropdownMenu(expanded = workspaceMenuExpanded, onDismissRequest = { workspaceMenuExpanded = false }) {
                workspaces.forEach { workspace ->
                    DropdownMenuItem(
                        text = { Text(workspace.name) },
                        onClick = { onSelectWorkspace(workspace.id); workspaceMenuExpanded = false }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Create workspace") },
                    leadingIcon = { Icon(FeatherIcons.Plus, contentDescription = null) },
                    onClick = { workspaceMenuExpanded = false; showCreateWorkspace = true }
                )
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
                                Icon(FeatherIcons.MoreVertical, contentDescription = "Conversation actions")
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

        if (showCreateWorkspace) {
            CreateWorkspaceDialog(
                onDismiss = { showCreateWorkspace = false },
                onCreate = { name, prompt ->
                    onCreateWorkspace(name, prompt)
                    showCreateWorkspace = false
                }
            )
        }
    }
}

@Composable
private fun CreateWorkspaceDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New workspace") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(prompt, { prompt = it }, label = { Text("Workspace instruction") }, minLines = 3)
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name, prompt) }, enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
    attachments: List<FileAsset>,
    onAttach: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        if (attachments.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                items(attachments, key = { it.id }) { asset ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, end = 2.dp)) {
                            Icon(FeatherIcons.Paperclip, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(asset.displayName, style = MaterialTheme.typography.labelSmall, maxLines = 1, modifier = Modifier.widthIn(max = 130.dp))
                            TextButton(onClick = { onRemoveAttachment(asset.id) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)) {
                                Text("×")
                            }
                        }
                    }
                }
            }
        }
        if (isStreaming) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onStop) { Icon(FeatherIcons.Square, "Stop", modifier = Modifier.size(14.dp)); Spacer(Modifier.width(5.dp)); Text("Stop") }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onRegenerate) { Icon(FeatherIcons.RefreshCw, "Regenerate", modifier = Modifier.size(17.dp)) }
                TextButton(onClick = onContinue) { Text("Continue", fontSize = 14.sp) }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(if (enabled) "Message..." else "Set API key first") },
            enabled = enabled && !isStreaming,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAttach, enabled = enabled && !isStreaming) {
                        Icon(FeatherIcons.Paperclip, contentDescription = "Attach file")
                    }
                    IconButton(
                        onClick = onSend,
                        enabled = enabled && text.isNotBlank() && !isStreaming
                    ) {
                        Icon(FeatherIcons.Send, contentDescription = "Send")
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            maxLines = 4
        )
    }
}
