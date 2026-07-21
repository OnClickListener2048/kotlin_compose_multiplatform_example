package org.example.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.bean.ChatItemType
import org.example.project.chat.ChatMessage
import org.example.project.chat.ChatStreamChunk
import org.example.project.chat.ProviderConfig
import org.example.project.chat.ProviderType
import org.example.project.core.context.ContextEngine
import org.example.project.core.context.ContextRequest
import org.example.project.feature.model.ModelGateway
import org.example.project.feature.files.FileAsset
import org.example.project.feature.files.FileAssetRepository
import org.example.project.feature.memory.ConversationMemoryService
import org.example.project.feature.workspace.INBOX_WORKSPACE_ID
import org.example.project.feature.workspace.Workspace
import org.example.project.feature.workspace.WorkspaceRepository
import org.example.project.repo.ChatItem
import org.example.project.repo.ChatRepository
import org.example.project.repo.Conversation
import org.example.project.repo.ApiKeyRepository
import org.example.project.repo.ApiKeyInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.time.Clock

data class ChatScreenState(
    val conversations: List<Conversation> = emptyList(),
    val workspaces: List<Workspace> = emptyList(),
    val currentWorkspaceId: String = INBOX_WORKSPACE_ID,
    val attachments: List<FileAsset> = emptyList(),
    val messages: List<ChatItem> = emptyList(),
    val currentConversationId: String? = null,
    val isStreaming: Boolean = false,
    val isLoading: Boolean = false,
    val inputText: String = "",
    val activeProvider: ProviderType = ProviderType.OpenAI,
    val activeConfig: ProviderConfig? = null
)

class AIChatViewModel(
    private val chatRepository: ChatRepository,
    private val apiKeyRepository: ApiKeyRepository,
    private val modelGateway: ModelGateway,
    private val contextEngine: ContextEngine,
    private val workspaceRepository: WorkspaceRepository,
    private val fileAssetRepository: FileAssetRepository,
    private val conversationMemoryService: ConversationMemoryService
) {

    private val screenModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(ChatScreenState())
    val state: StateFlow<ChatScreenState> = _state.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    private var streamJob: Job? = null
    private var shouldStopStream = false

    init {
        workspaceRepository.ensureInbox()
        loadWorkspaces()
        loadConversations()
        loadActiveConfig()
        val conversations = _state.value.conversations
        if (conversations.isNotEmpty()) {
            val lastConv = conversations.maxByOrNull { it.updatedAt }
            if (lastConv != null) {
                selectConversation(lastConv.id)
            }
        }
    }

    fun loadConversations() {
        val conversations = chatRepository.getConversations(_state.value.currentWorkspaceId)
        _state.value = _state.value.copy(conversations = conversations)
    }

    fun loadWorkspaces() {
        _state.value = _state.value.copy(workspaces = workspaceRepository.getAll())
    }

    fun selectWorkspace(workspaceId: String) {
        if (workspaceRepository.getById(workspaceId) == null) return
        _state.value = _state.value.copy(
            currentWorkspaceId = workspaceId,
            currentConversationId = null,
            messages = emptyList(),
            attachments = emptyList(),
            inputText = ""
        )
        loadConversations()
    }

    fun createWorkspace(name: String, systemPrompt: String = "") {
        if (name.isBlank()) return
        val workspace = workspaceRepository.create(name, systemPrompt)
        loadWorkspaces()
        selectWorkspace(workspace.id)
    }

    fun loadArchivedConversations(): List<Conversation> {
        return chatRepository.getArchivedConversations()
    }

    fun searchConversations(query: String) {
        if (query.isBlank()) {
            loadConversations()
        } else {
            val results = chatRepository.searchConversations(query)
            _state.value = _state.value.copy(conversations = results)
        }
    }

    private fun loadActiveConfig() {
        val activeKey = apiKeyRepository.getActiveKey()
        if (activeKey != null) {
            _state.value = _state.value.copy(
                activeProvider = activeKey.providerType,
                activeConfig = ProviderConfig(
                    apiKey = activeKey.apiKey,
                    baseUrl = activeKey.baseUrl.ifBlank { activeKey.providerType.defaultBaseUrl },
                    model = activeKey.model.ifBlank { activeKey.providerType.defaultModel }
                )
            )
        }
    }

    fun updateInputText(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    fun setActiveApiKey(keyInfo: ApiKeyInfo) {
        apiKeyRepository.setActiveKey(keyInfo.id)
        _state.value = _state.value.copy(
            activeProvider = keyInfo.providerType,
            activeConfig = ProviderConfig(
                apiKey = keyInfo.apiKey,
                baseUrl = keyInfo.baseUrl.ifBlank { keyInfo.providerType.defaultBaseUrl },
                model = keyInfo.model.ifBlank { keyInfo.providerType.defaultModel }
            )
        )
    }

    fun newConversation() {
        val config = _state.value.activeConfig ?: run {
            screenModelScope.launch { _toastEvents.emit("Please configure an API key first") }
            return
        }
        val provider = _state.value.activeProvider
        val conversation = chatRepository.createConversation(
            workspaceId = _state.value.currentWorkspaceId,
            providerType = provider,
            model = config.model
        )
        _state.value = _state.value.copy(
            currentConversationId = conversation.id,
            messages = emptyList(),
            attachments = emptyList(),
            inputText = ""
        )
        loadConversations()
    }

    fun selectConversation(conversationId: String) {
        val messages = chatRepository.getMessages(conversationId)
        _state.value = _state.value.copy(
            currentConversationId = conversationId,
            messages = messages,
            attachments = fileAssetRepository.forConversation(conversationId),
            inputText = ""
        )
    }

    fun attachFile(displayName: String, mimeType: String, localPath: String, sizeBytes: Long) {
        val conversationId = _state.value.currentConversationId ?: run {
            newConversation()
            _state.value.currentConversationId
        } ?: return
        fileAssetRepository.attach(
            displayName = displayName,
            mimeType = mimeType,
            localPath = localPath,
            sizeBytes = sizeBytes,
            workspaceId = _state.value.currentWorkspaceId,
            conversationId = conversationId
        )
        _state.value = _state.value.copy(attachments = fileAssetRepository.forConversation(conversationId))
    }

    fun removeAttachment(id: String) {
        fileAssetRepository.delete(id)
        val conversationId = _state.value.currentConversationId ?: return
        _state.value = _state.value.copy(attachments = fileAssetRepository.forConversation(conversationId))
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isStreaming) return

        val config = _state.value.activeConfig ?: run {
            screenModelScope.launch { _toastEvents.emit("Please configure an API key first") }
            return
        }

        var conversationId = _state.value.currentConversationId
        if (conversationId == null) {
            val conversation = chatRepository.createConversation(
                workspaceId = _state.value.currentWorkspaceId,
                providerType = _state.value.activeProvider,
                model = config.model
            )
            conversationId = conversation.id
        }

        val userMsg = chatRepository.insertMessage(conversationId, text, ChatItemType.Question)
        val messages = _state.value.messages + userMsg
        _state.value = _state.value.copy(
            currentConversationId = conversationId,
            inputText = "",
            messages = messages
        )
        loadConversations()
        streamChat(conversationId, messages)
    }

    private fun streamChat(conversationId: String, messages: List<ChatItem>) {
        val config = _state.value.activeConfig ?: return
        @OptIn(ExperimentalUuidApi::class, kotlin.time.ExperimentalTime::class)
        var assistantMsg = ChatItem(
            id = Uuid.random().toString(),
            conversationId = conversationId,
            content = "",
            type = ChatItemType.Answer,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            isLoading = true
        )

        _state.value = _state.value.copy(
            messages = messages + assistantMsg,
            isStreaming = true
        )

        shouldStopStream = false
        streamJob = screenModelScope.launch {
            try {
                var streamCompleted = false
                val history = messages
                    .filter { !it.isLoading && !it.content.startsWith("Error:") }
                    .map {
                    ChatMessage(role = if (it.type == ChatItemType.Question) "user" else "assistant", content = it.content)
                }

                val prompt = contextEngine.build(
                    ContextRequest(
                        workspace = workspaceRepository.getById(_state.value.currentWorkspaceId),
                        conversationId = conversationId,
                        history = history
                    )
                )
                modelGateway.stream(prompt, config).collect { chunk ->
                    if (shouldStopStream || streamCompleted) return@collect

                    if (chunk.isDone) {
                        streamCompleted = true
                        chatRepository.insertMessage(
                            conversationId,
                            assistantMsg.content,
                            ChatItemType.Answer
                        )
                        if (chatRepository.getMessageCount(conversationId) <= 2) {
                            val firstMsg = messages.firstOrNull { it.type == ChatItemType.Question }
                            if (firstMsg != null) {
                                val title = generateTitle(firstMsg.content)
                                chatRepository.updateConversationTitle(conversationId, title)
                            }
                        }
                        _state.value = _state.value.copy(isStreaming = false)
                        loadConversations()
                        screenModelScope.launch {
                            conversationMemoryService.summarizeIfNeeded(
                                workspaceId = chatRepository.getConversationById(conversationId)?.workspaceId
                                    ?: INBOX_WORKSPACE_ID,
                                conversationId = conversationId,
                                messages = (messages + assistantMsg).map {
                                    ChatMessage(
                                        role = if (it.type == ChatItemType.Question) "user" else "assistant",
                                        content = it.content
                                    )
                                },
                                config = config
                            )
                        }
                    } else {
                        assistantMsg = assistantMsg.copy(
                            content = assistantMsg.content + chunk.content,
                            isLoading = false
                        )
                        updateMessageInState(assistantMsg)
                    }
                }
            } catch (e: Exception) {
                assistantMsg = assistantMsg.copy(
                    content = "Error: ${e.message}",
                    isLoading = false
                )
                updateMessageInState(assistantMsg)
                _state.value = _state.value.copy(isStreaming = false)
            }
        }
    }

    fun stopGeneration() {
        shouldStopStream = true
        streamJob?.cancel()
        val messages = _state.value.messages.map {
            if (it.isLoading) it.copy(isLoading = false) else it
        }
        _state.value = _state.value.copy(messages = messages, isStreaming = false)

        val convId = _state.value.currentConversationId ?: return
        val lastAssistant = messages.lastOrNull { it.type == ChatItemType.Answer }
        if (lastAssistant != null && lastAssistant.content.isNotBlank()) {
            chatRepository.insertMessage(convId, lastAssistant.content, ChatItemType.Answer)
        }
    }

    fun regenerate() {
        val convId = _state.value.currentConversationId ?: return
        val msgs = _state.value.messages
        if (msgs.isEmpty()) return

        val lastAssistant = msgs.lastOrNull { it.type == ChatItemType.Answer }
        val lastQuestion = msgs.lastOrNull { it.type == ChatItemType.Question } ?: return

        if (lastAssistant != null) {
            chatRepository.deleteMessage(lastAssistant.id)
        }

        val filteredMsgs = msgs.filter { it.id != lastAssistant?.id }
        _state.value = _state.value.copy(
            messages = filteredMsgs,
            isLoading = false
        )
        streamChat(convId, filteredMsgs)
    }

    fun continueGeneration() {
        val config = _state.value.activeConfig ?: return
        val messages = _state.value.messages
        if (messages.isEmpty() || !_state.value.isStreaming) {
            val lastAssistant = _state.value.messages.lastOrNull { it.type == ChatItemType.Answer }
            if (lastAssistant != null) {
                val convId = _state.value.currentConversationId ?: return
                @OptIn(ExperimentalUuidApi::class, kotlin.time.ExperimentalTime::class)
                var assistantMsg = ChatItem(
                    id = Uuid.random().toString(),
                    conversationId = convId,
                    content = "",
                    type = ChatItemType.Answer,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    isLoading = true
                )
                _state.value = _state.value.copy(
                    messages = _state.value.messages + assistantMsg,
                    isStreaming = true
                )

                shouldStopStream = false
                streamJob = screenModelScope.launch {
                    try {
                        var streamCompleted = false
                        val history = _state.value.messages
                            .filter { !it.isLoading && !it.content.startsWith("Error:") }
                            .map {
                            ChatMessage(role = if (it.type == ChatItemType.Question) "user" else "assistant", content = it.content)
                        } + ChatMessage(role = "user", content = "Please continue from where you left off.")

                val prompt = contextEngine.build(
                    ContextRequest(
                        workspace = workspaceRepository.getById(_state.value.currentWorkspaceId),
                        conversationId = convId,
                        history = history
                    )
                )
                modelGateway.stream(prompt, config).collect { chunk ->
                            if (shouldStopStream || streamCompleted) return@collect
                            if (chunk.isDone) {
                                streamCompleted = true
                                chatRepository.insertMessage(convId, assistantMsg.content, ChatItemType.Answer)
                                _state.value = _state.value.copy(isStreaming = false)
                                loadConversations()
                            } else {
                                assistantMsg = assistantMsg.copy(
                                    content = assistantMsg.content + chunk.content,
                                    isLoading = false
                                )
                                updateMessageInState(assistantMsg)
                            }
                        }
                    } catch (e: Exception) {
                        assistantMsg = assistantMsg.copy(
                            content = "Error: ${e.message}",
                            isLoading = false
                        )
                        updateMessageInState(assistantMsg)
                        _state.value = _state.value.copy(isStreaming = false)
                    }
                }
            }
        }
    }

    fun togglePin(conversationId: String) {
        val conv = chatRepository.getConversationById(conversationId) ?: return
        chatRepository.toggleConversationPin(conversationId, !conv.isPinned)
        loadConversations()
    }

    fun toggleArchive(conversationId: String) {
        val conv = chatRepository.getConversationById(conversationId) ?: return
        chatRepository.toggleConversationArchive(conversationId, !conv.isArchived)
        if (conv.id == _state.value.currentConversationId && !conv.isArchived) {
            _state.value = _state.value.copy(currentConversationId = null, messages = emptyList())
        }
        loadConversations()
    }

    fun deleteConversation(conversationId: String) {
        chatRepository.deleteConversation(conversationId)
        if (conversationId == _state.value.currentConversationId) {
            _state.value = _state.value.copy(currentConversationId = null, messages = emptyList())
        }
        loadConversations()
    }

    fun renameConversation(conversationId: String, newTitle: String) {
        chatRepository.updateConversationTitle(conversationId, newTitle)
        loadConversations()
    }

    private fun updateMessageInState(updatedMsg: ChatItem) {
        _state.value = _state.value.copy(
            messages = _state.value.messages.map {
                if (it.id == updatedMsg.id) updatedMsg else it
            }
        )
    }

    private fun generateTitle(firstMessage: String): String {
        val cleaned = firstMessage.take(40).replace("\n", " ").trim()
        return if (cleaned.length >= 40) "$cleaned..." else cleaned
    }
}
