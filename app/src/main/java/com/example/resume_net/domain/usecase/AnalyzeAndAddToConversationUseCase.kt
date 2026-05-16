package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Композитный UseCase для анализа резюме и добавления результата в существующий диалог
 *
 * Объединяет:
 * 1. Анализ резюме через AnalyzeResumeUseCase
 * 2. Сохранение сообщения пользователя
 * 3. Сохранение ответа ассистента
 *
 * @param analyzeResumeUseCase существующий UseCase для анализа
 * @param conversationRepository репозиторий для работы с диалогами
 */
class AnalyzeAndAddToConversationUseCase(
    private val analyzeResumeUseCase: AnalyzeResumeUseCase,
    private val conversationRepository: ConversationRepository
) {

    /**
     * Параметры для выполнения
     *
     * @param conversationId ID диалога, куда добавляем анализ
     * @param resumeText текст резюме для анализа
     * @param addUserMessage добавить ли сообщение пользователя (по умолчанию true)
     */
    data class Params(
        val conversationId: Long,
        val resumeText: String,
        val addUserMessage: Boolean = true
    )

    /**
     * Результат выполнения
     *
     * @param analysisResult результат анализа
     * @param userMessage сообщение пользователя (если было добавлено)
     * @param assistantMessage ответ ассистента
     */
    data class Result(
        val analysisResult: AnalysisResult,
        val userMessage: ChatMessage.UserMessage?,
        val assistantMessage: ChatMessage.AssistantMessage
    )

    /**
     * Выполнение композитного UseCase
     *
     * @param params параметры
     * @return Result с результатами
     * @throws Exception если анализ не удался или не удалось сохранить сообщения
     */
    suspend operator fun invoke(params: Params): Result = withContext(Dispatchers.IO) {
        val text = params.resumeText.trim()

        // 1. Проверка валидности текста
        if (text.isEmpty()) {
            throw AnalysisError.EmptyResume
        }
        if (text.length < 50) {
            throw AnalysisError.TooShort
        }

        // 2. Выполняем анализ резюме (возвращает Result<AnalysisResult>)
        val analysisResult = when (val result = analyzeResumeUseCase(text)) {
            is kotlin.Result -> {
                if (result.isSuccess) {
                    result.getOrNull() ?: throw AnalysisError.InferenceError("Analysis returned null")
                } else {
                    throw result.exceptionOrNull() ?: AnalysisError.InferenceError("Analysis failed")
                }
            }
            else -> throw AnalysisError.InferenceError("Unexpected result type")
        }

        // 3. Добавляем сообщение пользователя (опционально)
        val userMessage = if (params.addUserMessage) {
            val addResult = conversationRepository.addUserMessage(
                conversationId = params.conversationId,
                text = text
            )
            addResult.getOrThrow()
        } else {
            null
        }

        // 4. Добавляем ответ ассистента
        val assistantResult = conversationRepository.addAssistantMessage(
            conversationId = params.conversationId,
            analysisResult = analysisResult
        )
        val assistantMessage = assistantResult.getOrThrow()

        // 5. Обновляем название диалога, если это первое сообщение
        updateConversationTitleIfNeeded(params.conversationId, text)

        Result(
            analysisResult = analysisResult,
            userMessage = userMessage,
            assistantMessage = assistantMessage
        )
    }

    /**
     * Обновление названия диалога на основе текста резюме
     * (если текущее название не было изменено пользователем)
     */
    private suspend fun updateConversationTitleIfNeeded(conversationId: Long, resumeText: String) {
        val conversation = conversationRepository.getConversationById(conversationId)
        if (conversation != null) {
            // Получаем количество сообщений в диалоге
            val messages = conversationRepository.getMessages(conversationId, limit = 2, offset = 0)
            if (messages.size <= 2) {
                // Это первый или второй анализ в диалоге, обновляем название
                val newTitle = generateTitle(resumeText)
                if (conversation.title != newTitle) {
                    conversationRepository.renameConversation(conversationId, newTitle)
                }
            }
        }
    }

    /**
     * Генерация названия из текста резюме
     */
    private fun generateTitle(resumeText: String): String {
        val firstLine = resumeText.lines()
            .firstOrNull { it.isNotBlank() }
            ?.take(30)
            ?: resumeText.take(30)

        return if (firstLine.length < resumeText.length && resumeText.length > 30) {
            "$firstLine..."
        } else {
            firstLine
        }
    }
}