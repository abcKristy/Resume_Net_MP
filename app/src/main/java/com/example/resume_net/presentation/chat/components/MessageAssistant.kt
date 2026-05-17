package com.example.resume_net.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.resume_net.ui.theme.AssistantAvatarIcn
import com.example.resume_net.ui.theme.AssistantGradientEnd
import com.example.resume_net.ui.theme.AssistantGradientStart
import com.example.resume_net.ui.theme.OnBackgroundLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Компонент для отображения сообщения ассистента
 */
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
        // Аватар ассистента
        AssistantAvatar()

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AssistantGradientStart,
                            AssistantGradientEnd
                        )
                    ),
                    shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
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
                    color = OnBackgroundLight,  // ← бордовый цвет (AccentDark)
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnBackgroundLight.copy(alpha = 0.6f),
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

/**
 * Аватар ассистента
 */
@Composable
private fun AssistantAvatar() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AssistantAvatarIcn,
                        AssistantAvatarIcn.copy(alpha = 0.7f)
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

/**
 * Заголовок секции
 */
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
            style = MaterialTheme.typography.titleMedium,
            color = OnBackgroundLight  // ← бордовый цвет
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnBackgroundLight  // ← бордовый цвет
        )
        if (subtitle != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OnBackgroundLight.copy(alpha = 0.1f)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnBackgroundLight,  // ← бордовый цвет
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Получение релевантных тегов для отображения
 */
private fun getRelevantTags(allTags: List<AnalysisIssue>): List<AnalysisIssue> {
    val highProbabilityTags = allTags.filter { it.probability > 0.4f }

    return if (highProbabilityTags.isNotEmpty()) {
        highProbabilityTags.sortedByDescending { it.probability }
    } else {
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
        color = OnBackgroundLight.copy(alpha = 0.2f),
        thickness = 1.dp
    )
}

/**
 * Форматирование времени
 */
private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Preview
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
                val mockResultWithHighTags = AnalysisResult(
                    score = 3.2f,
                    issues = emptyList(),
                    warnings = emptyList(),
                    allTags = listOf(
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                            probability = 0.45f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                            recommendation = "Добавьте конкретные цифры и метрики"
                        ),
                        AnalysisIssue(
                            tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                            probability = 0.72f,
                            severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                            recommendation = "Укажите используемые технологии"
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
            }
        }
    }
}