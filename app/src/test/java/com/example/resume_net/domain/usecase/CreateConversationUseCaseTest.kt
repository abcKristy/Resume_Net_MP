package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.IssueSeverity
import com.example.resume_net.domain.model.ResumeTag
import com.example.resume_net.domain.repository.ConversationRepository
import com.example.resume_net.domain.repository.ResumeRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class CreateConversationUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var resumeRepository: ResumeRepository
    private lateinit var createConversationUseCase: CreateConversationUseCase

    @Before
    fun setUp() {
        conversationRepository = mockk(relaxed = true)
        resumeRepository = mockk(relaxed = true)
        createConversationUseCase = CreateConversationUseCase(
            conversationRepository = conversationRepository,
            resumeRepository = resumeRepository
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

    @Test
    fun `createConversation - success with valid data`() = runTest {
        // Arrange
        val resumeText = "Опытный разработчик Android с 5-летним опытом..."
        val analysisResult = createMockAnalysisResult()
        val expectedConversationId = 123L

        coEvery { resumeRepository.analyze(resumeText) } returns Result.success(analysisResult)
        coEvery { conversationRepository.createConversation(resumeText, analysisResult) } returns expectedConversationId

        // Act
        val params = CreateConversationUseCase.Params(resumeText = resumeText, useCache = false)
        val result = createConversationUseCase(params)

        // Assert
        assertNotNull(result)
        assertEquals(expectedConversationId, result.conversationId)
        assertEquals(analysisResult.score, result.analysisResult.score)
        assertTrue(result.fromCache == false)

        coVerify(exactly = 1) { resumeRepository.analyze(resumeText) }
        coVerify(exactly = 1) { conversationRepository.createConversation(resumeText, analysisResult) }
    }

    @Test
    fun `createConversation - should throw exception when analysis fails`() = runTest {
        // Arrange
        val resumeText = "Текст резюме"
        val exception = Exception("Model not available")

        coEvery { resumeRepository.analyze(resumeText) } returns Result.failure(exception)

        // Act & Assert
        val params = CreateConversationUseCase.Params(resumeText = resumeText)

        try {
            createConversationUseCase(params)
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Analysis failed: Model not available", e.message)
        }

        coVerify(exactly = 0) { conversationRepository.createConversation(any(), any()) }
    }
}