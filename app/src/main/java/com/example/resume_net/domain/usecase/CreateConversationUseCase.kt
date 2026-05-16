package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.repository.ConversationRepository
import com.example.resume_net.domain.repository.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UseCase для создания нового диалога с анализом резюме
 *
 * @param conversationRepository репозиторий для работы с диалогами
 * @param resumeRepository репозиторий для анализа резюме
 */
class CreateConversationUseCase(
    private val conversationRepository: ConversationRepository,
    private val resumeRepository: ResumeRepository
) {

    /**
     * Параметры для создания диалога
     *
     * @param resumeText текст резюме для анализа
     * @param useCache использовать ли кэширование результатов
     */
    data class Params(
        val resumeText: String,
        val useCache: Boolean = true
    )

    /**
     * Результат создания диалога
     *
     * @param conversationId ID созданного диалога
     * @param analysisResult результат анализа
     * @param fromCache был ли результат взят из кэша
     */
    data class Result(
        val conversationId: Long,
        val analysisResult: AnalysisResult,
        val fromCache: Boolean
    )

    /**
     * Выполнение use case
     *
     * @param params параметры
     * @return Result с ID диалога и результатом анализа
     * @throws Exception при ошибках анализа или сохранения
     */
    suspend operator fun invoke(params: Params): Result = withContext(Dispatchers.IO) {
        val text = params.resumeText.trim()

        // 1. Выполняем анализ резюме
        val analysisResult = when (params.useCache) {
            true -> {
                val cached = checkCache(text)
                if (cached != null) {
                    return@withContext Result(
                        conversationId = -1L,
                        analysisResult = cached,
                        fromCache = true
                    )
                }
                analyzeResume(text)
            }
            false -> analyzeResume(text)
        }

        // 2. Создаём диалог с результатами
        val conversationId = conversationRepository.createConversation(
            resumeText = text,
            analysisResult = analysisResult
        )

        Result(
            conversationId = conversationId,
            analysisResult = analysisResult,
            fromCache = false
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

    private suspend fun checkCache(text: String): AnalysisResult? {
        // TODO: Реализовать проверку кэша через AnalysisCache
        return null
    }
}