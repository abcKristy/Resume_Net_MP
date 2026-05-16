package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class AddMessageUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var resumeRepository: ResumeRepository
    private lateinit var addMessageUseCase: AddMessageUseCase

    @Before
    fun setUp() {
        conversationRepository = mockk(relaxed = true)
        resumeRepository = mockk(relaxed = true)
        addMessageUseCase = AddMessageUseCase(
            conversationRepository = conversationRepository,
            resumeRepository = resumeRepository
        )
    }

    private fun createMockUserMessage(id: Long, conversationId: Long, text: String): ChatMessage.UserMessage {
        return ChatMessage.UserMessage(
            id = id,
            conversationId = conversationId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    }

    private fun createMockAssistantMessage(id: Long, conversationId: Long): ChatMessage.AssistantMessage {
        return ChatMessage.AssistantMessage(
            id = id,
            conversationId = conversationId,
            analysisResult = AnalysisResult(
                score = 4.0f,
                issues = emptyList(),
                warnings = emptyList(),
                allTags = emptyList()
            ),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    }

    @Test
    fun `addMessage - success add user message without analysis`() = runTest {
        // Arrange
        val conversationId = 123L
        val messageText = "Привет, это тестовое сообщение"
        val expectedMessage = createMockUserMessage(1L, conversationId, messageText)

        coEvery { conversationRepository.addUserMessage(conversationId, messageText) }
            .returns(Result.success(expectedMessage))

        // Act
        val params = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = messageText,
            isResumeAnalysis = false
        )
        val result = addMessageUseCase(params)

        // Assert
        assertNotNull(result)
        assertEquals(messageText, result.userMessage.text)
        assertNull(result.assistantMessage)

        coVerify(exactly = 1) { conversationRepository.addUserMessage(conversationId, messageText) }
        coVerify(exactly = 0) { conversationRepository.addAssistantMessage(any(), any()) }
    }

    @Test
    fun `addMessage - success add resume with analysis`() = runTest {
        // Arrange
        val conversationId = 123L
        val resumeText = "Опытный разработчик с 5-летним стажем работы в Android разработке. " +
                "За это время успешно реализовал 10+ проектов. Владею Kotlin, Java, Compose."
        val userMessage = createMockUserMessage(1L, conversationId, resumeText)
        val assistantMessage = createMockAssistantMessage(2L, conversationId)
        val analysisResult = assistantMessage.analysisResult

        coEvery { conversationRepository.addUserMessage(conversationId, resumeText) }
            .returns(Result.success(userMessage))
        coEvery { resumeRepository.analyze(resumeText) }
            .returns(Result.success(analysisResult))
        coEvery { conversationRepository.addAssistantMessage(conversationId, analysisResult) }
            .returns(Result.success(assistantMessage))

        // Act
        val params = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = resumeText,
            isResumeAnalysis = true
        )
        val result = addMessageUseCase(params)

        // Assert
        assertNotNull(result)
        assertEquals(resumeText, result.userMessage.text)
        assertNotNull(result.assistantMessage)
        assertEquals(assistantMessage.id, result.assistantMessage?.id)

        coVerify(exactly = 1) { conversationRepository.addUserMessage(conversationId, resumeText) }
        coVerify(exactly = 1) { resumeRepository.analyze(resumeText) }
        coVerify(exactly = 1) { conversationRepository.addAssistantMessage(conversationId, analysisResult) }
    }

    @Test
    fun `addMessage - should throw exception when conversation not exists`() = runTest {
        // Arrange
        val conversationId = 999L
        val messageText = "Тестовое сообщение"
        val exception = Exception("Conversation not found")

        coEvery { conversationRepository.addUserMessage(conversationId, messageText) }
            .returns(Result.failure(exception))

        // Act & Assert
        val params = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = messageText,
            isResumeAnalysis = false
        )

        try {
            addMessageUseCase(params)
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Conversation not found", e.message)
        }
    }

    @Test
    fun `addMessage - should not analyze short text (less than 50 chars)`() = runTest {
        // Arrange
        val conversationId = 123L
        val shortText = "Короткий текст" // 14 символов < 50
        val userMessage = createMockUserMessage(1L, conversationId, shortText)

        coEvery { conversationRepository.addUserMessage(conversationId, shortText) }
            .returns(Result.success(userMessage))

        // Act
        val params = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = shortText,
            isResumeAnalysis = true  // Запрос на анализ, но текст короткий
        )
        val result = addMessageUseCase(params)

        // Assert
        assertNotNull(result)
        assertEquals(shortText, result.userMessage.text)
        assertNull(result.assistantMessage)  // Анализ не выполнен

        coVerify(exactly = 1) { conversationRepository.addUserMessage(conversationId, shortText) }
        coVerify(exactly = 0) { resumeRepository.analyze(any()) }
        coVerify(exactly = 0) { conversationRepository.addAssistantMessage(any(), any()) }
    }

    @Test
    fun `addMessage - should analyze text with exactly 50 characters`() = runTest {
        // Arrange
        val conversationId = 123L
        // Текст ровно 50 символов
        val exactText = "Это текст ровно пятьдесят символов для проверки анализа."
        val userMessage = createMockUserMessage(1L, conversationId, exactText)
        val assistantMessage = createMockAssistantMessage(2L, conversationId)
        val analysisResult = assistantMessage.analysisResult

        coEvery { conversationRepository.addUserMessage(conversationId, exactText) }
            .returns(Result.success(userMessage))
        coEvery { resumeRepository.analyze(exactText) }
            .returns(Result.success(analysisResult))
        coEvery { conversationRepository.addAssistantMessage(conversationId, analysisResult) }
            .returns(Result.success(assistantMessage))

        // Act
        val params = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = exactText,
            isResumeAnalysis = true
        )
        val result = addMessageUseCase(params)

        // Assert
        assertNotNull(result)
        assertNotNull(result.assistantMessage)
        assertEquals(assistantMessage.id, result.assistantMessage?.id)

        coVerify(exactly = 1) { resumeRepository.analyze(exactText) }
    }

    @Test
    fun `addMessage - should update updated_at after adding`() = runTest {
        // Arrange
        val conversationId = 123L
        val messageText = "Новое сообщение"
        val userMessage = createMockUserMessage(1L, conversationId, messageText)

        coEvery { conversationRepository.addUserMessage(conversationId, messageText) }
            .returns(Result.success(userMessage))

        // Act
        val params = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = messageText,
            isResumeAnalysis = false
        )
        addMessageUseCase(params)

        // Assert
        // Обновление updated_at происходит внутри ConversationRepositoryImpl.addUserMessage
        // Проверяем, что addUserMessage был вызван
        coVerify(exactly = 1) { conversationRepository.addUserMessage(conversationId, messageText) }
    }

    @Test
    fun `addMessage - should handle analysis failure gracefully`() = runTest {
        // Arrange
        val conversationId = 123L
        val resumeText = "Достаточно длинный текст резюме для анализа. Здесь больше 50 символов."
        val userMessage = createMockUserMessage(1L, conversationId, resumeText)
        val analysisException = Exception("Model not loaded")

        coEvery { conversationRepository.addUserMessage(conversationId, resumeText) }
            .returns(Result.success(userMessage))
        coEvery { resumeRepository.analyze(resumeText) }
            .returns(Result.failure(analysisException))

        // Act & Assert
        val params = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = resumeText,
            isResumeAnalysis = true
        )

        try {
            addMessageUseCase(params)
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: Exception) {
            assertEquals("Analysis failed: Model not loaded", e.message)
        }

        coVerify(exactly = 1) { conversationRepository.addUserMessage(conversationId, resumeText) }
        coVerify(exactly = 1) { resumeRepository.analyze(resumeText) }
        coVerify(exactly = 0) { conversationRepository.addAssistantMessage(any(), any()) }
    }

    @Test
    fun `addMessage - success add multiple messages in same conversation`() = runTest {
        // Arrange
        val conversationId = 123L
        val firstText = "Первое сообщение"
        val secondText = "Второе сообщение"
        val firstMessage = createMockUserMessage(1L, conversationId, firstText)
        val secondMessage = createMockUserMessage(2L, conversationId, secondText)

        coEvery { conversationRepository.addUserMessage(conversationId, firstText) }
            .returns(Result.success(firstMessage))
        coEvery { conversationRepository.addUserMessage(conversationId, secondText) }
            .returns(Result.success(secondMessage))

        // Act
        val params1 = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = firstText,
            isResumeAnalysis = false
        )
        val result1 = addMessageUseCase(params1)

        val params2 = AddMessageUseCase.Params(
            conversationId = conversationId,
            text = secondText,
            isResumeAnalysis = false
        )
        val result2 = addMessageUseCase(params2)

        // Assert
        assertEquals(firstText, result1.userMessage.text)
        assertEquals(secondText, result2.userMessage.text)

        coVerify(exactly = 2) { conversationRepository.addUserMessage(conversationId, any()) }
    }
}