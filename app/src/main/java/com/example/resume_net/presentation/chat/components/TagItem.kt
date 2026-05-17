package com.example.resume_net.presentation.chat.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.ui.theme.tagScoreHigh
import com.example.resume_net.ui.theme.tagScoreLow
import com.example.resume_net.ui.theme.tagScoreMedium

/**
 * Компонент для отображения одного тега с прогресс-баром
 * При нажатии разворачивает/сворачивает рекомендацию
 *
 * @param issue тег с вероятностью
 * @param modifier модификатор
 */
@Composable
fun TagItem(
    issue: AnalysisIssue,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val progress = issue.probability
    val barColor = when {
        progress > 0.6f -> MaterialTheme.colorScheme.tagScoreHigh
        progress > 0.3f -> MaterialTheme.colorScheme.tagScoreMedium
        else -> MaterialTheme.colorScheme.tagScoreLow
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Верхняя строка: название, прогресс-бар, процент, иконка разворота
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Название тега (кликабельное)
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

            // Иконка разворота/сворачивания
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Свернуть рекомендацию" else "Развернуть рекомендацию",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Анимированная рекомендация (появляется при разворачивании)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 200)
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 200)
            ) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Заголовок рекомендации
                    Text(
                        text = "💡 Совет по улучшению",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Текст рекомендации
                    Text(
                        text = issue.recommendation.ifBlank { "Рекомендация не добавлена" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
            // Тег с вероятностью 80% (критический) - развернут по умолчанию для превью
            val mockIssueCritical = AnalysisIssue(
                tag = com.example.resume_net.domain.model.ResumeTag.NO_NUMBERS,
                probability = 0.8f,
                severity = com.example.resume_net.domain.model.IssueSeverity.CRITICAL,
                recommendation = "Добавьте в резюме конкретные цифры и метрики: например, 'увеличил продажи на 30%', 'оптимизировал загрузку страницы с 3 до 1 секунды'."
            )
            TagItem(issue = mockIssueCritical)

            // Тег с вероятностью 50% (предупреждение)
            val mockIssueWarning = AnalysisIssue(
                tag = com.example.resume_net.domain.model.ResumeTag.NO_SKILLS,
                probability = 0.5f,
                severity = com.example.resume_net.domain.model.IssueSeverity.WARNING,
                recommendation = "Укажите используемые технологии и инструменты с указанием уровня владения."
            )
            TagItem(issue = mockIssueWarning)

            // Тег с вероятностью 25% (OK)
            val mockIssueOk = AnalysisIssue(
                tag = com.example.resume_net.domain.model.ResumeTag.TOO_SHORT,
                probability = 0.25f,
                severity = com.example.resume_net.domain.model.IssueSeverity.OK,
                recommendation = "Резюме хорошей длины, можете добавить ещё пару достижений."
            )
            TagItem(issue = mockIssueOk)
        }
    }
}