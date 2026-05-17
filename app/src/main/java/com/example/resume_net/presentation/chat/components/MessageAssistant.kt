package com.example.resume_net.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.model.IssueSeverity
import com.example.resume_net.ui.theme.assistantAvatarEnd
import com.example.resume_net.ui.theme.assistantAvatarStart
import java.text.SimpleDateFormat
import java.util.*

// MessageAssistant.kt — улучшенный дизайн
@Composable
fun MessageAssistant(
    message: ChatMessage.AssistantMessage,
    modifier: Modifier = Modifier
) {
    val analysisResult = message.analysisResult
    val formattedTime = formatTime(message.timestamp)
    val relevantTags = getRelevantTags(analysisResult.allTags)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // Аватар ассистента (новый элемент)
        AssistantAvatar()

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.assistantAvatarStart,
                            MaterialTheme.colorScheme.assistantAvatarEnd
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Имя отправителя и время
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Аналитик AI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Оценка
            ScoreChip(score = analysisResult.score)

            Spacer(modifier = Modifier.height(12.dp))

            // Заголовок секции
            SectionHeader(
                title = "Ключевые проблемы",
                subtitle = if (hasHighProbabilityTags(analysisResult.allTags)) ">40%" else "топ-3",
                icon = "📊"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Список тегов
            relevantTags.forEach { issue ->
                TagItem(issue = issue)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// Новый компонент: Аватар ассистента
@Composable
private fun AssistantAvatar() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = "AI Assistant",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Новый компонент: Заголовок секции
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: String = "•",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Получение релевантных тегов для отображения
 *
 * Логика:
 * 1. Если есть теги с вероятностью > 40% → показываем их
 * 2. Если нет ни одного тега > 40% → показываем топ-3 тега с наибольшей вероятностью
 *
 * @param allTags все 20 тегов из анализа
 * @return отфильтрованный список тегов
 */
private fun getRelevantTags(allTags: List<AnalysisIssue>): List<AnalysisIssue> {
    val highProbabilityTags = allTags.filter { it.probability > 0.4f }

    return if (highProbabilityTags.isNotEmpty()) {
        // Сортируем по убыванию вероятности и возвращаем все
        highProbabilityTags.sortedByDescending { it.probability }
    } else {
        // Берём топ-3 по вероятности
        allTags.sortedByDescending { it.probability }.take(3)
    }
}

/**
 * Проверка, есть ли теги с вероятностью выше 40%
 */
private fun hasHighProbabilityTags(allTags: List<AnalysisIssue>): Boolean {
    return allTags.any { it.probability > 0.4f }
}

/**
 * Горизонтальный разделитель
 */
@Composable
private fun HorizontalDivider() {
    Divider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}

/**
 * Форматирование времени в формате "HH:MM"
 */
private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Preview для визуального тестирования
 */
@Preview(showBackground = true)
@Composable
private fun MessageAssistantPreview() {
    com.example.resume_net.ui.theme.Resume_netTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                // Сценарий 1: Есть теги >40%
                val mockResultWithHighTags = AnalysisResult(
                    score = 3.2f,
                    issues = listOf(
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                            probability = 0.85f,
                            severity = IssueSeverity.CRITICAL,
                            recommendation = "Добавьте конкретные цифры и метрики: например, 'увеличил продажи на 30%', 'оптимизировал загрузку страницы с 3 до 1 секунды'."
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                            probability = 0.72f,
                            severity = IssueSeverity.CRITICAL,
                            recommendation = "Укажите используемые технологии и инструменты с указанием уровня владения."
                        )
                    ),
                    warnings = listOf(
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.TOO_SHORT,
                            probability = 0.45f,
                            severity = IssueSeverity.WARNING,
                            recommendation = "Добавьте больше информации о достижениях и конкретных результатах."
                        )
                    ),
                    allTags = listOf(
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                            probability = 0.85f,
                            severity = IssueSeverity.CRITICAL,
                            recommendation = "Добавьте конкретные цифры и метрики: например, 'увеличил продажи на 30%', 'оптимизировал загрузку страницы с 3 до 1 секунды'."
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                            probability = 0.72f,
                            severity = IssueSeverity.CRITICAL,
                            recommendation = "Укажите используемые технологии и инструменты с указанием уровня владения."
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.TOO_SHORT,
                            probability = 0.45f,
                            severity = IssueSeverity.WARNING,
                            recommendation = "Добавьте больше информации о достижениях и конкретных результатах."
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.BAD_STRUCTURE,
                            probability = 0.12f,
                            severity = IssueSeverity.OK,
                            recommendation = "Структура резюме в порядке, можно оставить как есть."
                        )
                    )
                )

                MessageAssistant(
                    message = ChatMessage.AssistantMessage(
                        id = 1,
                        conversationId = 1,
                        analysisResult = mockResultWithHighTags,
                        timestamp = System.currentTimeMillis()
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Сценарий 2: Нет тегов >40%, показываем топ-3
                val mockResultWithLowTags = AnalysisResult(
                    score = 4.5f,
                    issues = emptyList(),
                    warnings = emptyList(),
                    allTags = listOf(
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                            probability = 0.38f,
                            severity = IssueSeverity.OK,
                            recommendation = "Можно добавить пару цифр для усиления эффекта."
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.TOO_SHORT,
                            probability = 0.35f,
                            severity = IssueSeverity.OK,
                            recommendation = "Резюме хорошей длины, но можно добавить ещё одно достижение."
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                            probability = 0.32f,
                            severity = IssueSeverity.OK,
                            recommendation = "Стек технологий указан, но можно добавить уровень владения."
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.BAD_STRUCTURE,
                            probability = 0.28f,
                            severity = IssueSeverity.OK,
                            recommendation = "Структура хорошая"
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_ACHIEVEMENTS,
                            probability = 0.25f,
                            severity = IssueSeverity.OK,
                            recommendation = "Достижения есть, но можно их лучше выделить."
                        )
                    )
                )

                MessageAssistant(
                    message = ChatMessage.AssistantMessage(
                        id = 2,
                        conversationId = 1,
                        analysisResult = mockResultWithLowTags,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}