package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.Conversation
import com.example.resume_net.domain.repository.ConversationRepository
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class DeleteConversationUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var deleteConversationUseCase: DeleteConversationUseCase

    @Before
    fun setUp() {
        conversationRepository = mockk(relaxed = true)
        deleteConversationUseCase = DeleteConversationUseCase(conversationRepository)
    }

    private fun createMockConversation(id: Long): Conversation {
        return Conversation(
            id = id,
            title = "Тестовый диалог",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    @Test
    fun `deleteConversation - success when conversation exists`() = runTest {
        // Arrange
        val conversationId = 123L
        val conversation = createMockConversation(conversationId)

        coEvery { conversationRepository.getConversationById(conversationId) } returns conversation
        coEvery { conversationRepository.deleteConversation(conversationId) } returns Unit

        // Act
        val result = deleteConversationUseCase(conversationId)

        // Assert
        assertTrue(result)
        coVerify(exactly = 1) { conversationRepository.getConversationById(conversationId) }
        coVerify(exactly = 1) { conversationRepository.deleteConversation(conversationId) }
    }

    @Test
    fun `deleteConversation - returns false when conversation not exists`() = runTest {
        // Arrange
        val conversationId = 999L

        coEvery { conversationRepository.getConversationById(conversationId) } returns null

        // Act
        val result = deleteConversationUseCase(conversationId)

        // Assert
        assertFalse(result)
        coVerify(exactly = 1) { conversationRepository.getConversationById(conversationId) }
        coVerify(exactly = 0) { conversationRepository.deleteConversation(conversationId) }
    }

    @Test
    fun `deleteConversation - should handle repository exception`() = runTest {
        // Arrange
        val conversationId = 123L
        val conversation = createMockConversation(conversationId)

        coEvery { conversationRepository.getConversationById(conversationId) } returns conversation
        coEvery { conversationRepository.deleteConversation(conversationId) } throws RuntimeException("Database error")

        // Act & Assert
        try {
            deleteConversationUseCase(conversationId)
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }
}