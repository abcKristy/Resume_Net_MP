package com.example.resume_net.presentation.chat

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.presentation.chat.components.ChatTopAppBar
import com.example.resume_net.presentation.chat.components.DeleteConfirmationDialog
import com.example.resume_net.presentation.chat.components.MessageAssistant
import com.example.resume_net.presentation.chat.components.MessageInputField
import com.example.resume_net.presentation.chat.components.MessageUser
import com.example.resume_net.presentation.chat.components.RenameDialog
import com.example.resume_net.presentation.chat.components.TagDetailDialog
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatScreen(
    conversationId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: ChatViewModel = koinViewModel(
        parameters = { parametersOf(conversationId) }
    )

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Состояние для диалога тега
    var showTagDialog by remember { mutableStateOf(false) }
    var selectedTagName by remember { mutableStateOf("") }
    var selectedRecommendation by remember { mutableStateOf("") }

    // Автопрокрутка
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // Обработка эффектов
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatEffect.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is ChatEffect.ShowSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is ChatEffect.NavigateBack -> onNavigateBack()
                is ChatEffect.ScrollToBottom -> {
                    if (state.messages.isNotEmpty()) {
                        listState.animateScrollToItem(state.messages.size - 1)
                    }
                }
            }
        }
    }

    // Диалог тега
    if (showTagDialog) {
        TagDetailDialog(
            tagName = selectedTagName,
            recommendation = selectedRecommendation,
            onDismiss = { showTagDialog = false }
        )
    }

    // Диалог переименования
    if (state.showRenameDialog) {
        RenameDialog(
            currentTitle = state.conversationTitle,
            onRename = { newTitle ->
                viewModel.onEvent(ChatEvent.RenameConversation(newTitle))
            },
            onDismiss = { viewModel.onEvent(ChatEvent.DismissRenameDialog) }
        )
    }

    // Диалог подтверждения удаления
    if (state.showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = { viewModel.onEvent(ChatEvent.DeleteConversation) },
            onDismiss = { viewModel.onEvent(ChatEvent.DismissDeleteDialog) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopAppBar(
                title = state.conversationTitle,
                onNavigateBack = onNavigateBack,
                onRename = { viewModel.onEvent(ChatEvent.ShowRenameDialog) },
                onDelete = { viewModel.onEvent(ChatEvent.ShowDeleteConfirmation) },
                onExport = { viewModel.onEvent(ChatEvent.ShowExportDialog) }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }

                items(
                    items = state.messages,
                    key = { it.id }
                ) { message ->
                    when (message) {
                        is ChatMessage.UserMessage -> {
                            MessageUser(message = message)
                        }
                        is ChatMessage.AssistantMessage -> {
                            MessageAssistant(
                                message = message,
                                onTagInfoClick = { tagName, recommendation ->
                                    selectedTagName = tagName
                                    selectedRecommendation = recommendation
                                    showTagDialog = true
                                }
                            )
                        }
                    }
                }

                if (state.isTyping) {
                    item { TypingIndicator() }
                }
            }

            MessageInputField(
                text = state.inputText,
                onTextChange = { viewModel.onEvent(ChatEvent.UpdateInputText(it)) },
                onSend = {
                    if (state.inputText.length >= 50) {
                        viewModel.onEvent(ChatEvent.SendResumeForAnalysis)
                    } else {
                        viewModel.onEvent(ChatEvent.SendMessage)
                    }
                },
                isEnabled = !state.isLoading,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Компонент анимации "печатания" ассистента
 */
@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 80.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedTypingDot(delay = 0)
                AnimatedTypingDot(delay = 300)
                AnimatedTypingDot(delay = 600)
            }
        }
    }
}

/**
 * Анимированная точка для индикатора печати
 */
@Composable
private fun AnimatedTypingDot(delay: Int) {
    val transition = rememberInfiniteTransition(label = "typing")
    val scale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delay)
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(8.dp * scale)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.small
            )
    )
}

/**
 * Preview для визуального тестирования
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChatScreenPreview() {
    com.example.resume_net.ui.theme.Resume_netTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatScreen(
                conversationId = 1,
                onNavigateBack = {}
            )
        }
    }
}