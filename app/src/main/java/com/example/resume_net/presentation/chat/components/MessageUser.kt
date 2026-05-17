package com.example.resume_net.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resume_net.domain.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageUser(
    message: ChatMessage.UserMessage,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isLongText = message.text.length > 300
    val formattedTime = formatTime(message.timestamp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp))  // ← изменённая форма
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = if (isExpanded) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis
            )

            if (isLongText) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Свернуть ↑" else "Читать далее ↓",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
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
private fun MessageUserPreview() {
    com.example.resume_net.ui.theme.Resume_netTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                // Короткое сообщение
                MessageUser(
                    message = ChatMessage.UserMessage(
                        id = 1,
                        conversationId = 1,
                        text = "Короткое сообщение",
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Длинное сообщение (свернуто)
                MessageUser(
                    message = ChatMessage.UserMessage(
                        id = 2,
                        conversationId = 1,
                        text = "Очень длинное сообщение, которое должно сворачиваться после 5 строк. " +
                                "Это тестовое сообщение для проверки функционала сворачивания текста. " +
                                "Оно должно отображать кнопку 'Показать полностью', а после нажатия - " +
                                "показывать весь текст целиком. Пользователь может скрыть текст обратно.",
                        timestamp = System.currentTimeMillis() - 3600000
                    )
                )
            }
        }
    }
}