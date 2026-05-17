package com.example.resume_net.presentation.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.ui.theme.tagHigh
import com.example.resume_net.ui.theme.tagLow
import com.example.resume_net.ui.theme.tagMedium

@Composable
fun TagItem(
    issue: AnalysisIssue,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val progress = issue.probability

    val barColor = when {
        progress > 0.6f -> MaterialTheme.colorScheme.tagHigh
        progress > 0.3f -> MaterialTheme.colorScheme.tagMedium
        else -> MaterialTheme.colorScheme.tagLow
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(12.dp)
        ) {
            // Верхняя строка
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = issue.tag.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Тонкий прогресс-бар
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.7f))
                            )
                        )
                )
            }

            // Развёрнутая рекомендация
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(200)) + fadeIn(),
                exit = shrinkVertically(tween(200)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = issue.recommendation.ifBlank { "✨ Рекомендация будет добавлена в следующей версии" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
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