package com.example.resume_net.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность сообщения в диалоге
 * Таблица: messages
 *
 * @property id уникальный идентификатор сообщения
 * @property conversationId ID диалога (внешний ключ к таблице conversations)
 * @property role роль отправителя: USER или ASSISTANT
 * @property content текст сообщения (резюме или ответ ассистента)
 * @property score оценка резюме (только для ASSISTANT, может быть null)
 * @property tagsJson JSON строка с тегами и вероятностями (только для ASSISTANT)
 * @property createdAt timestamp создания сообщения (Unix milliseconds)
 * @property isFavorite флаг избранного сообщения (для закрепления важных анализов)
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE  // При удалении диалога удаляются все его сообщения
        )
    ],
    indices = [
        Index(value = ["conversation_id"], name = "idx_messages_conversation_id"),
        Index(value = ["created_at"], name = "idx_messages_created_at"),
        Index(value = ["conversation_id", "created_at"], name = "idx_messages_conversation_created")
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,

    @ColumnInfo(name = "role")
    val role: String,  // "USER" или "ASSISTANT"

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "score")
    val score: Float? = null,  // Только для ASSISTANT

    @ColumnInfo(name = "tags_json")
    val tagsJson: String? = null,  // Только для ASSISTANT, JSON формат

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false
)

/**
 * Роли отправителей сообщений
 */
object MessageRole {
    const val USER = "USER"
    const val ASSISTANT = "ASSISTANT"
}