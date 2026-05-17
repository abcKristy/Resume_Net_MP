package com.example.resume_net.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.model.IssueSeverity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Компонент для отображения сообщения ассистента
 *
 * Структура:
 * 1. ScoreChip - оценка резюме
 * 2. Разделитель
 * 3. Список TagItem - релевантные теги (только >40% или топ-3)
 *    - Каждый тег кликабельный, разворачивает рекомендацию
 * 4. Время отправления
 *
 * @param message сообщение ассистента
 * @param modifier модификатор
 */
@Composable
fun MessageAssistant(
    message: ChatMessage.AssistantMessage,
    modifier: Modifier = Modifier
) {
    val analysisResult = message.analysisResult
    val formattedTime = formatTime(message.timestamp)

    // Получаем релевантные теги (порог 40% или топ-3)
    val relevantTags = getRelevantTags(analysisResult.allTags)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            // 1. Оценка (ScoreChip)
            ScoreChip(score = analysisResult.score)

            // Разделитель
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // 2. Заголовок секции тегов (с пояснением)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Ключевые проблемы",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Пояснение, почему показываются именно эти теги
                if (hasHighProbabilityTags(analysisResult.allTags)) {
                    Text(
                        text = "проблемы >40%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = "топ-3 рекомендации",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Список релевантных тегов (каждый кликабельный)
            if (relevantTags.isEmpty()) {
                // Если нет тегов (маловероятно, но на всякий случай)
                Text(
                    text = "✨ Резюме выглядит отлично! Нет критических проблем.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                relevantTags.forEach { issue ->
                    TagItem(issue = issue)
                }
            }

            // Время отправления
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
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