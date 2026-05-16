package com.example.resume_net.domain.repository

import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с диалогами и сообщениями
 */
interface ConversationRepository {

    /**
     * Создание нового диалога с результатами анализа
     *
     * @param resumeText текст резюме пользователя
     * @param analysisResult результат анализа нейросети
     * @return ID созданного диалога
     */
    suspend fun createConversation(
        resumeText: String,
        analysisResult: AnalysisResult
    ): Long

    /**
     * Получение всех диалогов с пагинацией
     *
     * @param limit количество диалогов на страницу
     * @param offset смещение от начала
     * @return список диалогов
     */
    suspend fun getConversations(
        limit: Int = 20,
        offset: Int = 0
    ): List<Conversation>

    /**
     * Получение всех диалогов в виде Flow (для реактивного UI)
     */
    fun observeConversations(): Flow<List<Conversation>>

    /**
     * Получение диалога по ID
     */
    suspend fun getConversationById(id: Long): Conversation?

    /**
     * Удаление диалога по ID
     */
    suspend fun deleteConversation(id: Long)

    /**
     * Обновление названия диалога
     */
    suspend fun renameConversation(id: Long, newTitle: String)

    /**
     * Добавление нового сообщения в существующий диалог
     *
     * @param conversationId ID диалога
     * @param text текст сообщения (резюме или вопрос)
     * @return true если успешно
     */
    suspend fun addUserMessage(
        conversationId: Long,
        text: String
    ): Result<ChatMessage.UserMessage>

    /**
     * Добавление ответа ассистента в диалог
     */
    suspend fun addAssistantMessage(
        conversationId: Long,
        analysisResult: AnalysisResult
    ): Result<ChatMessage.AssistantMessage>

    /**
     * Получение всех сообщений диалога с пагинацией
     */
    suspend fun getMessages(
        conversationId: Long,
        limit: Int = 20,
        offset: Int = 0
    ): List<ChatMessage>

    /**
     * Получение всех сообщений диалога в виде Flow
     */
    fun observeMessages(conversationId: Long): Flow<List<ChatMessage>>

    /**
     * Получение последнего сообщения в диалоге
     */
    suspend fun getLastMessage(conversationId: Long): ChatMessage?
}