package com.example.resume_net.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.ui.theme.tagScoreHigh
import com.example.resume_net.ui.theme.tagScoreLow
import com.example.resume_net.ui.theme.tagScoreMedium

/**
 * Компонент для отображения одного тега с прогресс-баром
 *
 * @param issue тег с вероятностью
 * @param onInfoClick колбэк при нажатии на иконку информации
 */
@Composable
fun TagItem(
    issue: AnalysisIssue,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = issue.probability
    val barColor = when {
        progress > 0.6f -> MaterialTheme.colorScheme.tagScoreHigh
        progress > 0.3f -> MaterialTheme.colorScheme.tagScoreMedium
        else -> MaterialTheme.colorScheme.tagScoreLow
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Название тега
        Text(
            text = issue.tag.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(140.dp)
        )

        // Прогресс-бар
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Процент
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(40.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Кнопка информации
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Информация",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Preview для визуального тестирования
 */
@Preview(showBackground = true)
@Composable
private fun TagItemPreview() {
    com.example.resume_net.ui.theme.Resume_netTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Создаем моковые данные для превью
            val mockIssue = com.example.resume_net.domain.model.AnalysisIssue(
                tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                probability = 0.8f,
                severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                recommendation = "Добавьте цифры и метрики в резюме"
            )
            TagItem(issue = mockIssue, onInfoClick = {})

            val mockIssue2 = com.example.resume_net.domain.model.AnalysisIssue(
                tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                probability = 0.5f,
                severity = com.example.resume_net.domain.model.IssueSeverity.WARNING,
                recommendation = "Укажите технологии и инструменты"
            )
            TagItem(issue = mockIssue2, onInfoClick = {})
        }
    }
}