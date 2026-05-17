package com.example.resume_net.presentation.conversations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.resume_net.R
import com.example.resume_net.presentation.conversations.components.ConversationListItem
import com.example.resume_net.ui.theme.OnBackgroundLight
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreen(
    viewModel: ConversationListViewModel = koinViewModel(),
    onNavigateToChat: (Long) -> Unit,
    onNavigateToNewAnalysis: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Обработка эффектов
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ConversationListEffect.NavigateToChat -> {
                    onNavigateToChat(effect.conversationId)
                }
                is ConversationListEffect.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
                is ConversationListEffect.ShowSuccess -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }

    // Загрузка диалогов при первом открытии
    LaunchedEffect(Unit) {
        viewModel.onEvent(ConversationListEvent.LoadConversations)
    }

    // Определяем, какой список показывать (с фильтром или без)
    val displayConversations = if (state.searchQuery.isNotBlank()) {
        state.filteredConversations
    } else {
        state.conversations
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewAnalysis,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Новый анализ")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Заголовок + изображение в одной строке
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая часть: заголовок и счётчик
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "HireMind",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackgroundLight
                    )
                    Text(
                        text = "${state.conversations.size} ${getDeclension(state.conversations.size)}",
                        fontSize = 14.sp,
                        color = OnBackgroundLight.copy(alpha = 0.6f)
                    )
                }

                // Правая часть: изображение
                Image(
                    painter = painterResource(id = R.drawable.ic_app_clean),
                    contentDescription = "Логотип",
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-10).dp)
                        .size(120.dp)
                        .alpha(0.9f)
                )
            }

            // Поиск
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { query ->
                    viewModel.onEvent(ConversationListEvent.UpdateSearchQuery(query))
                },
                placeholder = { Text("Поиск по названию...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Список или пустое состояние
            when {
                state.isLoading && displayConversations.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                displayConversations.isEmpty() -> {
                    EmptyStateContent(
                        hasSearchQuery = state.searchQuery.isNotBlank(),
                        onClearSearch = {
                            viewModel.onEvent(ConversationListEvent.ClearSearch)
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = displayConversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationListItem(
                                conversation = conversation,
                                onClick = { viewModel.navigateToChat(conversation.id) },
                                onRename = { newTitle ->
                                    viewModel.onEvent(
                                        ConversationListEvent.RenameConversation(
                                            conversation.id,
                                            newTitle
                                        )
                                    )
                                },
                                onDelete = {
                                    viewModel.onEvent(
                                        ConversationListEvent.DeleteConversation(conversation.id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Склонение слова "анализ" в зависимости от числа
 */
private fun getDeclension(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "анализ"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "анализа"
        else -> "анализов"
    }
}

/**
 * Компонент пустого состояния
 */
@Composable
private fun EmptyStateContent(
    hasSearchQuery: Boolean,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasSearchQuery) Icons.Default.Search else Icons.Outlined.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = OnBackgroundLight.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasSearchQuery) "Ничего не найдено" else "Нет анализов",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackgroundLight.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (hasSearchQuery) {
            Text(
                text = "Попробуйте изменить поисковый запрос",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onClearSearch) {
                Text("Очистить поиск")
            }
        } else {
            Text(
                text = "Нажмите на кнопку +, чтобы создать первый анализ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Preview для визуального тестирования
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConversationsListScreenPreview() {
    com.example.resume_net.ui.theme.Resume_netTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Для превью используем статическое содержимое
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Моковые данные для превью
                val mockConversations = listOf(
                    com.example.resume_net.domain.model.Conversation(
                        id = 1,
                        title = "Frontend Developer Resume",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        lastMessagePreview = "Ваше резюме хорошее, но добавьте больше цифр и метрик...",
                        lastScore = 4.2f
                    ),
                    com.example.resume_net.domain.model.Conversation(
                        id = 2,
                        title = "Product Manager CV",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis() - 86400000,
                        lastMessagePreview = "Много воды, нет конкретных достижений",
                        lastScore = 2.5f
                    ),
                    com.example.resume_net.domain.model.Conversation(
                        id = 3,
                        title = "Без оценки",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis() - 172800000,
                        lastMessagePreview = null,
                        lastScore = null
                    )
                )

                mockConversations.forEach { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        onClick = {},
                        onRename = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}