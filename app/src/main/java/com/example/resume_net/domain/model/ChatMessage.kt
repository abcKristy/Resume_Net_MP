package com.example.resume_net.domain.model

/**
 * Запечатанный класс для всех типов сообщений в чате
 */
sealed class ChatMessage {

    /**
     * Абстрактные свойства, общие для всех сообщений
     */
    abstract val id: Long
    abstract val conversationId: Long
    abstract val timestamp: Long
    abstract val isFavorite: Boolean

    /**
     * Сообщение от пользователя
     *
     * @property id уникальный идентификатор сообщения
     * @property conversationId ID диалога
     * @property text текст сообщения (резюме или вопрос)
     * @property timestamp timestamp отправления
     * @property isFavorite флаг избранного
     */
    data class UserMessage(
        override val id: Long,
        override val conversationId: Long,
        val text: String,
        override val timestamp: Long,
        override val isFavorite: Boolean = false
    ) : ChatMessage()

    /**
     * Сообщение от ассистента (ИИ)
     *
     * @property id уникальный идентификатор сообщения
     * @property conversationId ID диалога
     * @property analysisResult полный результат анализа (оценка + теги + рекомендации)
     * @property timestamp timestamp отправления
     * @property isFavorite флаг избранного
     */
    data class AssistantMessage(
        override val id: Long,
        override val conversationId: Long,
        val analysisResult: AnalysisResult,
        override val timestamp: Long,
        override val isFavorite: Boolean = false
    ) : ChatMessage()
}