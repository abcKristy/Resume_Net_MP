package com.example.resume_net.presentation.newanalysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.resume_net.presentation.newanalysis.components.ExampleCard
import com.example.resume_net.presentation.newanalysis.components.ExpandableSection
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NewAnalysisScreen(
    viewModel: NewAnalysisViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onAnalysisComplete: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isExamplesExpanded by remember { mutableStateOf(false) }

    // Обработка эффектов
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NewAnalysisEffect.NavigateBack -> onNavigateBack()
                is NewAnalysisEffect.NavigateToChat -> onAnalysisComplete(effect.conversationId)
                is NewAnalysisEffect.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
                is NewAnalysisEffect.ShowTooltip -> {
                    // Тултип показываем через Snackbar (или можно использовать TooltipBox)
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }

    // ============= ДИАЛОГ: ОШИБКА ЗАГРУЗКИ МОДЕЛИ =============

    if (state.showModelLoadingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(NewAnalysisEvent.DismissModelLoadingDialog) },
            title = { Text("Загрузка модели") },
            text = {
                Text("Модель анализа загружается. Попробуйте через минуту или повторите попытку.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(NewAnalysisEvent.RetryAnalysis) }
                ) {
                    Text("Повторить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(NewAnalysisEvent.DismissModelLoadingDialog) }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // ============= ДИАЛОГ: ДУБЛИКАТ ТЕКСТА =============

    if (state.showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(NewAnalysisEvent.DismissDuplicateDialog) },
            title = { Text("Резюме уже анализировалось") },
            text = {
                Text("Вы уже анализировали это резюме. Открыть существующий анализ?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(NewAnalysisEvent.OpenExistingConversation) }
                ) {
                    Text("Открыть")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(NewAnalysisEvent.DismissDuplicateDialog) }
                ) {
                    Text("Создать новый")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Новый анализ",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(NewAnalysisEvent.Cancel) }) {
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
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Поле ввода текста
            OutlinedTextField(
                value = state.resumeText,
                onValueChange = { text ->
                    viewModel.onEvent(NewAnalysisEvent.UpdateResumeText(text))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                placeholder = {
                    Text(
                        text = "Вставьте текст резюме или сопроводительного письма...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                supportingText = {
                    Text(
                        text = "${state.charCount} / 50 (минимум)",
                        color = if (state.charCount >= 50)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                },
                isError = state.charCount < 50 && state.charCount > 0,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Аккордеон с примерами
            ExpandableSection(
                title = "📝 Примеры резюме",
                expanded = isExamplesExpanded,
                onExpandedChange = { isExamplesExpanded = it }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResumeExamples.examples.forEach { example ->
                        ExampleCard(
                            title = example.title,
                            description = example.description,
                            content = example.content,
                            onClick = { content ->
                                viewModel.onEvent(NewAnalysisEvent.UpdateResumeText(content))
                                isExamplesExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Анализировать"
            Button(
                onClick = { viewModel.onEvent(NewAnalysisEvent.Analyze) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.isAnalyzeEnabled && !state.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Анализируем...")
                } else {
                    Text("Анализировать", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Подсказка для неактивной кнопки
            if (!state.isAnalyzeEnabled && state.charCount > 0 && state.charCount < 50) {
                Text(
                    text = "Добавьте ещё ${50 - state.charCount} символов для анализа",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Информационная панель
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 О чём стоит помнить",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Чем подробнее резюме, тем точнее анализ\n" +
                                "• Добавляйте конкретные цифры и достижения\n" +
                                "• Указывайте технологии и стек\n" +
                                "• Пишите о результатах, а не только об обязанностях",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Отображение ошибки (если есть)
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(
                            onClick = { viewModel.onEvent(NewAnalysisEvent.ClearError) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Закрыть",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview для визуального тестирования
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NewAnalysisScreenPreview() {
    com.example.resume_net.ui.theme.Resume_netTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NewAnalysisScreen(
                onNavigateBack = {},
                onAnalysisComplete = {}
            )
        }
    }
}