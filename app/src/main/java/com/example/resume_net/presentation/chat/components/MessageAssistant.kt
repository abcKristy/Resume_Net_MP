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
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Компонент для отображения сообщения ассистента
 *
 * Структура:
 * 1. ScoreChip - оценка резюме
 * 2. Разделитель
 * 3. Список TagItem - теги с вероятностями
 * 4. Разделитель
 * 5. Общая рекомендация
 *
 * @param message сообщение ассистента
 * @param onTagInfoClick колбэк при нажатии на информацию по тегу
 * @param modifier модификатор
 */
@Composable
fun MessageAssistant(
    message: ChatMessage.AssistantMessage,
    onTagInfoClick: (tagName: String, recommendation: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val analysisResult = message.analysisResult
    val formattedTime = formatTime(message.timestamp)

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

            // 2. Заголовок секции тегов
            Text(
                text = "📊 Теги и вероятности",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Список тегов
            analysisResult.allTags.forEach { issue ->
                TagItem(
                    issue = issue,
                    onInfoClick = { onTagInfoClick(issue.tag.displayName, issue.recommendation) }
                )
            }

            // Разделитель (если есть рекомендация)
            if (analysisResult.issues.isNotEmpty() || analysisResult.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // 3. Общая рекомендация
                Text(
                    text = "💡 Рекомендации",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Критические проблемы
                if (analysisResult.issues.isNotEmpty()) {
                    Text(
                        text = "Критические проблемы:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    analysisResult.issues.forEach { issue ->
                        Text(
                            text = "• ${issue.recommendation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Предупреждения
                if (analysisResult.warnings.isNotEmpty()) {
                    Text(
                        text = "Рекомендации:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    analysisResult.warnings.forEach { issue ->
                        Text(
                            text = "• ${issue.recommendation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
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
                // Создаем моковые данные для превью
                val mockAnalysisResult = com.example.resume_net.domain.model.AnalysisResult(
                    score = 4.2f,
                    issues = listOf(
                        com.example.resume_net.domain.model.AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                            probability = 0.8f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                            recommendation = "Добавьте конкретные цифры и метрики"
                        ),
                        com.example.resume_net.domain.model.AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                            probability = 0.7f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                            recommendation = "Укажите используемые технологии"
                        )
                    ),
                    warnings = listOf(
                        com.example.resume_net.domain.model.AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.TOO_SHORT,
                            probability = 0.45f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.WARNING,
                            recommendation = "Добавьте больше информации о достижениях"
                        )
                    ),
                    allTags = listOf(
                        com.example.resume_net.domain.model.AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                            probability = 0.8f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                            recommendation = "Добавьте конкретные цифры и метрики"
                        ),
                        com.example.resume_net.domain.model.AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                            probability = 0.7f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                            recommendation = "Укажите используемые технологии"
                        ),
                        com.example.resume_net.domain.model.AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.TOO_SHORT,
                            probability = 0.45f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.WARNING,
                            recommendation = "Добавьте больше информации о достижениях"
                        )
                    )
                )

                val mockMessage = com.example.resume_net.domain.model.ChatMessage.AssistantMessage(
                    id = 1,
                    conversationId = 1,
                    analysisResult = mockAnalysisResult,
                    timestamp = System.currentTimeMillis()
                )

                MessageAssistant(
                    message = mockMessage,
                    onTagInfoClick = { tagName, recommendation -> }
                )
            }
        }
    }
}