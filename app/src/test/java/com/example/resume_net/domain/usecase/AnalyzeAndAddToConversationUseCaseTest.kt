package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.model.IssueSeverity
import com.example.resume_net.domain.model.ResumeTag
import com.example.resume_net.domain.repository.ConversationRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class AnalyzeAndAddToConversationUseCaseTest {

    private lateinit var analyzeResumeUseCase: AnalyzeResumeUseCase
    private lateinit var conversationRepository: ConversationRepository
    private lateinit var analyzeAndAddToConversationUseCase: AnalyzeAndAddToConversationUseCase

    @Before
    fun setUp() {
        analyzeResumeUseCase = mockk(relaxed = true)
        conversationRepository = mockk(relaxed = true)
        analyzeAndAddToConversationUseCase = AnalyzeAndAddToConversationUseCase(
            analyzeResumeUseCase = analyzeResumeUseCase,
            conversationRepository = conversationRepository
        )
    }

    private fun createMockAnalysisResult(): AnalysisResult {
        return AnalysisResult(
            score = 4.2f,
            issues = listOf(
                AnalysisIssue(
                    tag = ResumeTag.NO_NUMBERS,
                    probability = 0.8f,
                    severity = IssueSeverity.CRITICAL,
                    recommendation = "Добавьте цифры"
                )
            ),
            warnings = emptyList(),
            allTags = emptyList()
        )
    }

    private fun createMockUserMessage(id: Long, conversationId: Long, text: String): ChatMessage.UserMessage {
        return ChatMessage.UserMessage(
            id = id,
            conversationId = conversationId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun createMockAssistantMessage(id: Long, conversationId: Long, analysisResult: AnalysisResult): ChatMessage.AssistantMessage {
        return ChatMessage.AssistantMessage(
            id = id,
            conversationId = conversationId,
            analysisResult = analysisResult,
            timestamp = System.currentTimeMillis()
        )
    }

    @Test
    fun `analyzeAndAdd - success with valid resume`() = runTest {
        // Arrange
        val conversationId = 123L
        val resumeText = "Опытный разработчик Android с 5-летним стажем работы в крупной компании..."
        val analysisResult = createMockAnalysisResult()
        val userMessage = createMockUserMessage(1L, conversationId, resumeText)
        val assistantMessage = createMockAssistantMessage(2L, conversationId, analysisResult)

        coEvery { analyzeResumeUseCase(resumeText) } returns Result.success(analysisResult)
        coEvery { conversationRepository.addUserMessage(conversationId, resumeText) } returns Result.success(userMessage)
        coEvery { conversationRepository.addAssistantMessage(conversationId, analysisResult) } returns Result.success(assistantMessage)
        coEvery { conversationRepository.getConversationById(conversationId) } returns null
        coEvery { conversationRepository.getMessages(conversationId, 2, 0) } returns emptyList()

        // Act
        val params = AnalyzeAndAddToConversationUseCase.Params(
            conversationId = conversationId,
            resumeText = resumeText,
            addUserMessage = true
        )
        val result = analyzeAndAddToConversationUseCase(params)

        // Assert
        assertNotNull(result)
        assertEquals(analysisResult.score, result.analysisResult.score)
        assertNotNull(result.userMessage)
        assertEquals(resumeText, result.userMessage?.text)
        assertNotNull(result.assistantMessage)

        coVerify(exactly = 1) { analyzeResumeUseCase(resumeText) }
        coVerify(exactly = 1) { conversationRepository.addUserMessage(conversationId, resumeText) }
        coVerify(exactly = 1) { conversationRepository.addAssistantMessage(conversationId, analysisResult) }
    }

    @Test
    fun `analyzeAndAdd - should throw exception for empty resume`() = runTest {
        // Arrange
        val conversationId = 123L
        val emptyText = "   "

        // Act & Assert
        val params = AnalyzeAndAddToConversationUseCase.Params(
            conversationId = conversationId,
            resumeText = emptyText
        )

        try {
            analyzeAndAddToConversationUseCase(params)
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: Exception) {
            assertTrue(e is AnalysisError.EmptyResume)
        }

        coVerify(exactly = 0) { analyzeResumeUseCase(any()) }
    }

    @Test
    fun `analyzeAndAdd - should throw exception for short resume`() = runTest {
        // Arrange
        val conversationId = 123L
        val shortText = "Короткое резюме"

        // Act & Assert
        val params = AnalyzeAndAddToConversationUseCase.Params(
            conversationId = conversationId,
            resumeText = shortText
        )

        try {
            analyzeAndAddToConversationUseCase(params)
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: Exception) {
            assertTrue(e is AnalysisError.TooShort)
        }

        coVerify(exactly = 0) { analyzeResumeUseCase(any()) }
    }

    @Test
    fun `analyzeAndAdd - should not add user message when flag is false`() = runTest {
        // Arrange
        val conversationId = 123L
        val resumeText = "Достаточно длинный текст резюме для анализа. Здесь больше пятидесяти символов, чтобы пройти валидацию."
        val analysisResult = createMockAnalysisResult()
        val assistantMessage = createMockAssistantMessage(2L, conversationId, analysisResult)

        coEvery { analyzeResumeUseCase(resumeText) } returns Result.success(analysisResult)
        coEvery { conversationRepository.addAssistantMessage(conversationId, analysisResult) } returns Result.success(assistantMessage)

        // Act
        val params = AnalyzeAndAddToConversationUseCase.Params(
            conversationId = conversationId,
            resumeText = resumeText,
            addUserMessage = false
        )
        val result = analyzeAndAddToConversationUseCase(params)

        // Assert
        assertNotNull(result)
        assertNull(result.userMessage)
        assertNotNull(result.assistantMessage)

        coVerify(exactly = 0) { conversationRepository.addUserMessage(any(), any()) }
        coVerify(exactly = 1) { conversationRepository.addAssistantMessage(conversationId, analysisResult) }
    }
}