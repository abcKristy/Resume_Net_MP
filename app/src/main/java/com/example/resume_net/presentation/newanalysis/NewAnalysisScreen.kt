package com.example.resume_net.presentation.newanalysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.resume_net.presentation.newanalysis.components.ExampleCard
import com.example.resume_net.presentation.newanalysis.components.ExpandableSection
import com.example.resume_net.ui.theme.OnBackgroundLight
import kotlinx.coroutines.delay
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

    // Анимация ошибки валидации
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorHeight by remember { mutableStateOf(0.dp) }

    val validationErrorAlpha by animateFloatAsState(
        targetValue = if (showValidationError) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "validationErrorAlpha"
    )

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
                    // Показываем анимацию ошибки валидации
                    validationErrorHeight = 8.dp
                    showValidationError = true
                    delay(2000)
                    showValidationError = false
                    delay(300)
                    validationErrorHeight = 0.dp
                }
            }
        }
    }

    // Диалоги (без изменений)
    if (state.showModelLoadingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(NewAnalysisEvent.DismissModelLoadingDialog) },
            title = { Text("Загрузка модели") },
            text = { Text("Модель анализа загружается. Попробуйте через минуту или повторите попытку.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(NewAnalysisEvent.RetryAnalysis) }) {
                    Text("Повторить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(NewAnalysisEvent.DismissModelLoadingDialog) }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (state.showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(NewAnalysisEvent.DismissDuplicateDialog) },
            title = { Text("Резюме уже анализировалось") },
            text = { Text("Вы уже анализировали это резюме. Открыть существующий анализ?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(NewAnalysisEvent.OpenExistingConversation) }) {
                    Text("Открыть")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(NewAnalysisEvent.DismissDuplicateDialog) }) {
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
                title = { Text("Новый анализ", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(NewAnalysisEvent.Cancel) }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
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
        ) {
            // Scrollable контент (занимает вес, чтобы кнопка была внизу)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Поле названия чата (опционально)
                OutlinedTextField(
                    value = state.conversationTitle,
                    onValueChange = { viewModel.onEvent(NewAnalysisEvent.UpdateConversationTitle(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Название чата (опционально)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Поле ввода резюме
                Column(
                    modifier = Modifier.animateContentSize()
                ) {
                    OutlinedTextField(
                        value = state.resumeText,
                        onValueChange = { text ->
                            viewModel.onEvent(NewAnalysisEvent.UpdateResumeText(text))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        placeholder = {
                            Text(
                                text = "Вставьте текст резюме или сопроводительного письма...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        isError = showValidationError,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = Color(0xFFE57373).copy(alpha = 0.7f),
                            errorContainerColor = Color(0xFFE57373).copy(alpha = 0.05f)
                        )
                    )

                    // Анимированная ошибка валидации
                    AnimatedVisibility(
                        visible = showValidationError,
                        enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE57373).copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Минимум 50 символов. Сейчас: ${state.charCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE57373)
                                )
                            }
                        }
                    }
                }

                // Счётчик символов (незаметный, меняется при ошибке)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.charCount} / 50 символов",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.charCount >= 50 || !showValidationError) {
                        OnBackgroundLight.copy(alpha = 0.4f)  // незаметный тёмно-голубой
                    } else {
                        Color(0xFFE57373).copy(alpha = 0.7f)  // мягкий красный при ошибке
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Аккордеон с примерами
                ExpandableSection(
                    title = "📝 Примеры резюме",
                    expanded = isExamplesExpanded,
                    onExpandedChange = { isExamplesExpanded = it }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                // Информационная панель
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка анализировать (внизу экрана)
            Button(
                onClick = { viewModel.onEvent(NewAnalysisEvent.Analyze) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
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

            // Отображение ошибки (если есть)
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(
                            onClick = { viewModel.onEvent(NewAnalysisEvent.ClearError) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Закрыть",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview
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