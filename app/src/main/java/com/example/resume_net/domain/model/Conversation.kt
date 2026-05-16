package com.example.resume_net.domain.model

/**
 * Domain модель диалога (разговора) с ИИ-ассистентом
 *
 * @property id уникальный идентификатор диалога
 * @property title название диалога (пользовательское или автоматическое)
 * @property createdAt timestamp создания (Unix milliseconds)
 * @property updatedAt timestamp последнего обновления (Unix milliseconds)
 * @property lastMessagePreview превью последнего сообщения (для отображения в списке)
 * @property lastMessageTimestamp timestamp последнего сообщения (для отображения даты)
 * @property lastScore последняя оценка резюме (для цветовой индикации)
 */
data class Conversation(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessagePreview: String? = null,
    val lastMessageTimestamp: Long? = null,
    val lastScore: Float? = null
)