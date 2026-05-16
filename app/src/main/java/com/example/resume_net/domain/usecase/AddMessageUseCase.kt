package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.repository.ConversationRepository
import com.example.resume_net.domain.repository.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UseCase для добавления нового сообщения в существующий диалог
 * Если сообщение содержит резюме, выполняется анализ
 *
 * @param conversationRepository репозиторий для работы с диалогами
 * @param resumeRepository репозиторий для анализа резюме
 */
class AddMessageUseCase(
    private val conversationRepository: ConversationRepository,
    private val resumeRepository: ResumeRepository
) {

    /**
     * Параметры для добавления сообщения
     *
     * @param conversationId ID диалога
     * @param text текст сообщения
     * @param isResumeAnalysis нужно ли выполнять анализ текста как резюме
     */
    data class Params(
        val conversationId: Long,
        val text: String,
        val isResumeAnalysis: Boolean = true
    )

    /**
     * Результат добавления сообщения
     *
     * @param userMessage добавленное сообщение пользователя
     * @param assistantMessage ответ ассистента (если был анализ)
     */
    data class Result(
        val userMessage: ChatMessage.UserMessage,
        val assistantMessage: ChatMessage.AssistantMessage?
    )

    /**
     * Выполнение добавления сообщения
     */
    suspend operator fun invoke(params: Params): Result = withContext(Dispatchers.IO) {
        // 1. Добавляем сообщение пользователя
        val userMessageResult = conversationRepository.addUserMessage(
            conversationId = params.conversationId,
            text = params.text
        )

        val userMessage = userMessageResult.getOrThrow()

        // 2. Если нужно, выполняем анализ и добавляем ответ ассистента
        val assistantMessage = if (params.isResumeAnalysis && params.text.length >= 50) {
            val analysisResult = analyzeResume(params.text)

            val assistantResult = conversationRepository.addAssistantMessage(
                conversationId = params.conversationId,
                analysisResult = analysisResult
            )

            assistantResult.getOrThrow()
        } else {
            null
        }

        Result(
            userMessage = userMessage,
            assistantMessage = assistantMessage
        )
    }

    /**
     * Анализ резюме с обработкой ошибок
     */
    private suspend fun analyzeResume(text: String): AnalysisResult {
        return try {
            val result = resumeRepository.analyze(text)
            if (result.isSuccess) {
                result.getOrNull() ?: throw Exception("Analysis returned null")
            } else {
                throw result.exceptionOrNull() ?: Exception("Analysis failed")
            }
        } catch (e: Exception) {
            throw Exception("Analysis failed: ${e.message}", e)
        }
    }
}