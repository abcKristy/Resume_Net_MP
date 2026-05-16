package com.example.resume_net.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    // Форматирование времени
    val formattedTime = formatTime(message.timestamp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Текст сообщения
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis
            )

            // Кнопка "Показать полностью" (только для длинного текста)
            if (isLongText) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Свернуть" else "Показать полностью",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Время отправления
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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