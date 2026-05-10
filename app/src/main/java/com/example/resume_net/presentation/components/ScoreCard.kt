package com.example.resume_net.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScoreCard(score: Float) {
    val color = when {
        score >= 4.0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        score >= 3.0 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
        else -> androidx.compose.ui.graphics.Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%.1f", score),
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}