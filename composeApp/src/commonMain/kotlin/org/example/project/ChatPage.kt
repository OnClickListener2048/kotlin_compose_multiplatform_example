package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.FeatherIcons
import compose.icons.feathericons.Image
import compose.icons.feathericons.Send
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.example.project.bean.ChatItem
import org.example.project.bean.ChatItemType
import org.example.project.components.CommonTopAppBar
import org.example.project.viewmodel.ChatViewModel

class ChatPage : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        println("ChatPage---$this")
        val currentOrThrow = LocalNavigator.currentOrThrow
        val chatViewModel =
            currentOrThrow.rememberNavigatorScreenModel { ChatViewModel() }
        val chatList by chatViewModel.chatList.collectAsStateWithLifecycle()

        val listState = rememberLazyListState()
        Scaffold(
            topBar = { CommonTopAppBar(title = "Chat") },
            bottomBar = { ChatBottomAppBar(chatViewModel, listState) }
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(10.dp),
                reverseLayout = false
            ) {
                items(
                    chatList.size,
                    key = { chatList[it].id },
                    contentType = { chatList[it].type }) { index ->
                    ChatItemView(chatList[index], chatViewModel)
                    Spacer(
                        modifier = Modifier.padding(vertical = 5.dp)
                            .height(20.dp)
                    )
                }
            }

        }
    }

    @Composable
    fun ChatItemView(chatItem: ChatItem, chatViewModel: ChatViewModel) {
        println("ChatItemView---$chatItem")
        if (chatItem.type == ChatItemType.Question) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chatItem.content,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp)) // 设置圆角
                        .widthIn(min = Dp.Unspecified)
                        .fillMaxWidth(0.75f)
                        .background(Color.LightGray)   // 背景颜色
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.Black,
                ) // 内边距)
            }
        } else if (chatItem.type == ChatItemType.Answer) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (chatItem.isLoading) {
                    CircularProgressIndicator(Modifier.size(16.dp).padding(5.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                }
                val answer by chatViewModel.answerFlow.collectAsStateWithLifecycle()
                Text(
                    text = if (chatItem.isLoading) answer else chatItem.content,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp)) // 设置圆角
                        .widthIn(min = Dp.Unspecified)
                        .fillMaxWidth(0.75f)
                        .background(Color.LightGray) // 背景颜色
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.Black,
                )
            }
        }
    }

    @Composable
    fun ChatBottomAppBar(chatViewModel: ChatViewModel = ChatViewModel(), listState: LazyListState) {
        println("ChatBottomAppBar---$chatViewModel")

        val question by chatViewModel.question.collectAsStateWithLifecycle()

        val coroutineScope = rememberCoroutineScope()

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = question,
            onValueChange = {
                println(it)
                chatViewModel.updateQuestion(it)
            },
            label = { Text("请输入你的问题") },
            leadingIcon = {
                Icon(
                    imageVector = FeatherIcons.Image,
                    contentDescription = "Send",
                    modifier = Modifier.clickable {
                        println("Send")
                        coroutineScope.launch {
                            val platformFile = FileKit.openFilePicker(FileKitType.Image)
                            println(platformFile)
                        }

                    }
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = FeatherIcons.Send,
                    contentDescription = "Send",
                    modifier = Modifier.clickable {
                        println("Send")
                        coroutineScope.launch {
                            chatViewModel.talk(question)
                            listState.animateScrollToItem(chatViewModel.chatList.value.size)
                        }

                    }
                )
            },
            singleLine = true
        )
    }
}