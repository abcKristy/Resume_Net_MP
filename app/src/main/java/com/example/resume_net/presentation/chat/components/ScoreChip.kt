package com.example.resume_net.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.resume_net.ui.theme.tagScoreHigh
import com.example.resume_net.ui.theme.tagScoreLow
import com.example.resume_net.ui.theme.tagScoreMedium

/**
 * Компонент для отображения оценки резюме
 *
 * @param score оценка от 1.0 до 5.0
 */
@Composable
fun ScoreChip(
    score: Float,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when {
        score >= 4.0f -> MaterialTheme.colorScheme.tagScoreLow to Color.White
        score >= 3.0f -> MaterialTheme.colorScheme.tagScoreMedium to Color.Black
        else -> MaterialTheme.colorScheme.tagScoreHigh to Color.White            // Красный
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Оценка",
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = String.format("%.1f / 5.0", score),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Preview для визуального тестирования
 */
@Preview(showBackground = true)
@Composable
private fun ScoreChipPreview() {
    com.example.resume_net.ui.theme.Resume_netTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScoreChip(score = 4.8f)
            ScoreChip(score = 3.5f)
            ScoreChip(score = 2.1f)
        }
    }
}