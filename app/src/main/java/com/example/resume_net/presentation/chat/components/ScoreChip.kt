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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.resume_net.ui.theme.scoreHighGradientEnd
import com.example.resume_net.ui.theme.scoreHighGradientStart
import com.example.resume_net.ui.theme.scoreLowGradientEnd
import com.example.resume_net.ui.theme.scoreLowGradientStart
import com.example.resume_net.ui.theme.scoreMediumGradientEnd
import com.example.resume_net.ui.theme.scoreMediumGradientStart

@Composable
fun ScoreChip(
    score: Float,
    modifier: Modifier = Modifier
) {
    val (startColor, endColor) = when {
        score >= 4.0f -> MaterialTheme.colorScheme.scoreHighGradientStart to MaterialTheme.colorScheme.scoreHighGradientEnd
        score >= 3.0f -> MaterialTheme.colorScheme.scoreMediumGradientStart to MaterialTheme.colorScheme.scoreMediumGradientEnd
        else -> MaterialTheme.colorScheme.scoreLowGradientStart to MaterialTheme.colorScheme.scoreLowGradientEnd
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp)),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(startColor, endColor)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Оценка",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = String.format("%.1f", score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "/ 5.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
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