package com.example.resume_net.presentation.conversations.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.resume_net.domain.model.Conversation
import com.example.resume_net.ui.theme.Resume_netTheme
import com.example.resume_net.ui.theme.tagScoreHigh
import com.example.resume_net.ui.theme.tagScoreLow
import com.example.resume_net.ui.theme.tagScoreMedium
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf(conversation.title) }
    var isDeleting by remember { mutableStateOf(false) }

    val iconColor = getColorByScore(conversation.lastScore)
    val formattedDate = formatDate(conversation.lastMessageTimestamp ?: conversation.updatedAt)

    // Анимированное удаление
    if (isDeleting) {
        // Элемент будет скрыт с анимацией
        return
    }

    SwipeToDeleteBox(
        onDelete = {
            isDeleting = true
            onDelete()
        },
        onDismiss = {
            // После удаления элемент исчезает
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка документа с цветом
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Диалог",
                    tint = iconColor,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Контент
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = conversation.lastMessagePreview?.take(60) ?: "Нет сообщений",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Дата и меню
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Действия",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Переименовать") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                isDeleting = true
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }

    // Диалог переименования
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Переименовать диалог") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Название") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onRename(newTitle)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Функции getColorByScore и formatDate остаются без изменений
@Composable
private fun getColorByScore(score: Float?): Color {
    return when {
        score == null -> MaterialTheme.colorScheme.onSurfaceVariant
        score >= 4.0f -> MaterialTheme.colorScheme.tagScoreLow
        score >= 3.0f -> MaterialTheme.colorScheme.tagScoreMedium
        else -> MaterialTheme.colorScheme.tagScoreHigh
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val date = Date(timestamp)
    val calendar = java.util.Calendar.getInstance().apply { time = date }
    val today = java.util.Calendar.getInstance()
    val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }

    return when {
        isSameDay(calendar, today) -> "Сегодня"
        isSameDay(calendar, yesterday) -> "Вчера"
        else -> SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
    }
}

private fun isSameDay(cal1: java.util.Calendar, cal2: java.util.Calendar): Boolean {
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}

@Preview(showBackground = true)
@Composable
private fun ConversationListItemPreview() {
    Resume_netTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                ConversationListItem(
                    conversation = Conversation(
                        id = 1,
                        title = "Frontend Developer Resume",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        lastMessagePreview = "Ваше резюме хорошее, но добавьте больше цифр и метрик...",
                        lastScore = 4.2f
                    ),
                    onClick = {},
                    onRename = {},
                    onDelete = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                ConversationListItem(
                    conversation = Conversation(
                        id = 2,
                        title = "Product Manager CV с очень длинным названием которое должно обрезаться",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis() - 86400000, // вчера
                        lastMessagePreview = "Много воды, нет конкретных достижений",
                        lastScore = 2.5f
                    ),
                    onClick = {},
                    onRename = {},
                    onDelete = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                ConversationListItem(
                    conversation = Conversation(
                        id = 3,
                        title = "Без оценки",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis() - 172800000, // 2 дня назад
                        lastMessagePreview = null,
                        lastScore = null
                    ),
                    onClick = {},
                    onRename = {},
                    onDelete = {}
                )
            }
        }
    }
}