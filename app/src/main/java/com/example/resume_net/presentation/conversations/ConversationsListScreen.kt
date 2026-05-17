package com.example.resume_net.presentation.conversations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.resume_net.presentation.conversations.components.ConversationListItem
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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Новый анализ"
                )
            }
        },
        topBar = {
            SearchTopAppBar(
                searchQuery = state.searchQuery,
                isSearchActive = state.isSearchActive,
                onSearchQueryChange = { query ->
                    viewModel.onEvent(ConversationListEvent.UpdateSearchQuery(query))
                },
                onSearchActivate = { isActive ->
                    viewModel.onEvent(ConversationListEvent.SetSearchActive(isActive))
                },
                onClearSearch = {
                    viewModel.onEvent(ConversationListEvent.ClearSearch)
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && displayConversations.isEmpty() -> {
                    // Загрузка
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                displayConversations.isEmpty() -> {
                    // Пустое состояние
                    EmptyStateContent(
                        hasSearchQuery = state.searchQuery.isNotBlank(),
                        onClearSearch = {
                            viewModel.onEvent(ConversationListEvent.ClearSearch)
                        }
                    )
                }

                else -> {
                    // Список диалогов
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = displayConversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationListItem(
                                conversation = conversation,
                                onClick = {
                                    viewModel.navigateToChat(conversation.id)
                                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    searchQuery: String,
    isSearchActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchActivate: (Boolean) -> Unit,
    onClearSearch: () -> Unit
) {
    if (isSearchActive) {
        // Режим поиска - используем TopAppBar с полем ввода
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Поиск...") },
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 40.dp, max = 40.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { onSearchActivate(false) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    } else {
        // Обычный режим - CenterAlignedTopAppBar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Анализы резюме",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            actions = {
                IconButton(onClick = { onSearchActivate(true) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
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
            imageVector = if (hasSearchQuery) Icons.Default.Search else Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasSearchQuery) "Ничего не найдено" else "Нет анализов",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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