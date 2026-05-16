package com.example.resume_net.domain.model.mapper

import com.example.resume_net.data.db.ConversationEntity
import com.example.resume_net.data.db.MessageEntity
import com.example.resume_net.data.db.MessageRole
import com.example.resume_net.domain.model.Conversation

/**
 * Маппер для конвертации между Entity (Room) и Domain моделью Conversation
 */
object ConversationMapper {

    /**
     * Конвертация Entity -> Domain
     *
     * @param entity сущность из БД
     * @param lastMessage последнее сообщение в диалоге (опционально, для превью)
     * @return domain модель диалога
     */
    fun toDomain(
        entity: ConversationEntity,
        lastMessage: MessageEntity? = null
    ): Conversation {
        return Conversation(
            id = entity.id,
            title = entity.title,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            lastMessagePreview = lastMessage?.let { getMessagePreview(it) },
            lastMessageTimestamp = lastMessage?.createdAt,
            lastScore = if (lastMessage?.role == MessageRole.ASSISTANT) lastMessage.score else null
        )
    }

    /**
     * Конвертация Domain -> Entity (для сохранения в БД)
     *
     * @param domain domain модель диалога
     * @return сущность для Room
     */
    fun toEntity(domain: Conversation): ConversationEntity {
        return ConversationEntity(
            id = domain.id,
            title = domain.title,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            resumeTextHash = null  // Хеш устанавливается отдельно при создании
        )
    }

    /**
     * Создание новой Entity для диалога (без ID)
     *
     * @param title название диалога
     * @param resumeTextHash хеш текста резюме (опционально)
     * @return новая сущность для вставки в БД
     */
    fun newEntity(
        title: String,
        resumeTextHash: String? = null
    ): ConversationEntity {
        val now = System.currentTimeMillis()
        return ConversationEntity(
            title = title,
            createdAt = now,
            updatedAt = now,
            resumeTextHash = resumeTextHash
        )
    }

    /**
     * Обновление существующей Entity (для обновления timestamp)
     */
    fun updateTimestamp(entity: ConversationEntity): ConversationEntity {
        return entity.copy(updatedAt = System.currentTimeMillis())
    }

    /**
     * Получение превью сообщения для отображения в списке диалогов
     */
    private fun getMessagePreview(message: MessageEntity): String {
        val text = message.content
        return when {
            text.length <= 60 -> text
            else -> text.substring(0, 60) + "..."
        }
    }
}