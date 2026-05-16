package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.repository.ConversationRepository
import com.example.resume_net.domain.repository.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class CreateConversationUseCase(
    private val conversationRepository: ConversationRepository,
    private val resumeRepository: ResumeRepository
) {

    companion object {
        private const val TAG = "CreateConversationUseCase"
    }

    data class Params(
        val resumeText: String,
        val useCache: Boolean = true
    )

    data class Result(
        val conversationId: Long,
        val analysisResult: AnalysisResult,
        val fromCache: Boolean
    )

    suspend operator fun invoke(params: Params): Result = withContext(Dispatchers.IO) {
        val text = params.resumeText.trim()
        Log.d(TAG, "=== Starting analysis ===")
        Log.d(TAG, "Text length: ${text.length}")

        // 1. Выполняем анализ
        val analysisResult = analyzeResume(text)
        Log.d(TAG, "Analysis completed, score: ${analysisResult.score}")

        // 2. Создаём диалог с результатами
        val conversationId = conversationRepository.createConversation(
            resumeText = text,
            analysisResult = analysisResult
        )
        Log.d(TAG, "Conversation created with ID: $conversationId")

        Result(
            conversationId = conversationId,
            analysisResult = analysisResult,
            fromCache = false  // Всегда false для новых диалогов
        )
    }

    private suspend fun analyzeResume(text: String): AnalysisResult {
        val result = resumeRepository.analyze(text)
        if (result.isSuccess) {
            return result.getOrNull() ?: throw Exception("Analysis returned null")
        } else {
            throw result.exceptionOrNull() ?: Exception("Analysis failed")
        }
    }
}