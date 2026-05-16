package com.example.resume_net.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность диалога (разговора) с ИИ-ассистентом
 * Таблица: conversations
 */
@Entity(
    tableName = "conversations",
    indices = [
        // Индексы для ускорения сортировки и поиска
        Index(value = ["created_at"], name = "idx_conversations_created_at"),
        Index(value = ["updated_at"], name = "idx_conversations_updated_at"),
        Index(value = ["resume_text_hash"], name = "idx_conversations_hash", unique = false)
    ]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,  // System.currentTimeMillis() или Clock.System.now().toEpochMilliseconds()

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "resume_text_hash", defaultValue = "")
    val resumeTextHash: String? = null  // SHA-256 хеш текста для поиска дубликатов
)