package com.example.resume_net.data.repository

import com.example.resume_net.data.db.ConversationDao
import com.example.resume_net.data.db.ConversationEntity
import com.example.resume_net.data.db.MessageDao
import com.example.resume_net.data.db.MessageEntity
import com.example.resume_net.data.db.MessageRole
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
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
class ConversationRepositoryImplTest {

    private lateinit var conversationDao: ConversationDao
    private lateinit var messageDao: MessageDao
    private lateinit var repository: ConversationRepositoryImpl

    @Before
    fun setUp() {
        conversationDao = mockk(relaxed = true)
        messageDao = mockk(relaxed = true)
        repository = ConversationRepositoryImpl(conversationDao, messageDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    private fun createMockAnalysisResult(): AnalysisResult {
        return mockk(relaxed = true)
    }

    // ============= ТЕСТЫ CREATE =============

    @Test
    fun `createConversation - saves conversation and messages`() = runTest {
        // Arrange
        val resumeText = "Тестовое резюме"
        val analysisResult = createMockAnalysisResult()
        val expectedId = 123L

        coEvery { conversationDao.insert(any()) } returns expectedId
        coEvery { messageDao.insert(any()) } returns 1L andThen 2L

        // Act
        val result = repository.createConversation(resumeText, analysisResult)

        // Assert
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { conversationDao.insert(any()) }
        coVerify(exactly = 2) { messageDao.insert(any()) }
    }

    // ============= ТЕСТЫ GET =============

    @Test
    fun `getConversations - returns list of conversations`() = runTest {
        // Arrange
        val entities = listOf(
            ConversationEntity(id = 1, title = "Диалог 1", createdAt = 1000, updatedAt = 1000),
            ConversationEntity(id = 2, title = "Диалог 2", createdAt = 2000, updatedAt = 2000)
        )
        coEvery { conversationDao.getPaginated(20, 0) } returns entities
        coEvery { messageDao.getLastMessage(any()) } returns null

        // Act
        val result = repository.getConversations(20, 0)

        // Assert
        assertEquals(2, result.size)
        assertEquals("Диалог 1", result[0].title)
        assertEquals("Диалог 2", result[1].title)
    }

    @Test
    fun `getConversationById - returns null when not found`() = runTest {
        coEvery { conversationDao.getById(999L) } returns null

        val result = repository.getConversationById(999L)

        assertNull(result)
    }

    @Test
    fun `getConversationById - returns conversation when found`() = runTest {
        val entity = ConversationEntity(id = 1, title = "Диалог", createdAt = 1000, updatedAt = 1000)
        coEvery { conversationDao.getById(1L) } returns entity
        coEvery { messageDao.getLastMessage(1L) } returns null

        val result = repository.getConversationById(1L)

        assertNotNull(result)
        assertEquals("Диалог", result?.title)
    }

    // ============= ТЕСТЫ УДАЛЕНИЯ =============

    @Test
    fun `deleteConversation - calls dao delete`() = runTest {
        coEvery { conversationDao.deleteById(123L) } returns Unit

        repository.deleteConversation(123L)

        coVerify(exactly = 1) { conversationDao.deleteById(123L) }
    }

    // ============= ТЕСТЫ ПЕРЕИМЕНОВАНИЯ =============

    @Test
    fun `renameConversation - calls dao update`() = runTest {
        coEvery { conversationDao.updateTitle(123L, "Новое название") } returns Unit

        repository.renameConversation(123L, "Новое название")

        coVerify(exactly = 1) { conversationDao.updateTitle(123L, "Новое название") }
    }

    // ============= ТЕСТЫ ДОБАВЛЕНИЯ СООБЩЕНИЙ =============

    @Test
    fun `addUserMessage - success when conversation exists`() = runTest {
        // Arrange
        val conversationId = 123L
        val text = "Тестовое сообщение"
        val conversation = ConversationEntity(id = conversationId, title = "Диалог", createdAt = 1000, updatedAt = 1000)

        coEvery { conversationDao.getById(conversationId) } returns conversation
        coEvery { messageDao.insert(any()) } returns 1L
        coEvery { conversationDao.updateTimestamp(conversationId, any()) } returns Unit

        // Act
        val result = repository.addUserMessage(conversationId, text)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(text, result.getOrNull()?.text)
        coVerify(exactly = 1) { messageDao.insert(any()) }
        coVerify(exactly = 1) { conversationDao.updateTimestamp(conversationId, any()) }
    }

    @Test
    fun `addUserMessage - fails when conversation not exists`() = runTest {
        coEvery { conversationDao.getById(999L) } returns null

        val result = repository.addUserMessage(999L, "Тест")

        assertTrue(result.isFailure)
        assertEquals("Conversation not found", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { messageDao.insert(any()) }
    }

    @Test
    fun `addAssistantMessage - success when conversation exists`() = runTest {
        // Arrange
        val conversationId = 123L
        val analysisResult = createMockAnalysisResult()
        val conversation = ConversationEntity(id = conversationId, title = "Диалог", createdAt = 1000, updatedAt = 1000)

        coEvery { conversationDao.getById(conversationId) } returns conversation
        coEvery { messageDao.insert(any()) } returns 1L
        coEvery { conversationDao.updateTimestamp(conversationId, any()) } returns Unit

        // Act
        val result = repository.addAssistantMessage(conversationId, analysisResult)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { messageDao.insert(any()) }
        coVerify(exactly = 1) { conversationDao.updateTimestamp(conversationId, any()) }
    }

    // ============= ТЕСТЫ ПОЛУЧЕНИЯ СООБЩЕНИЙ =============

    @Test
    fun `getMessages - returns list of messages`() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val entities = listOf(
            MessageEntity(id = 1, conversationId = 123L, role = MessageRole.USER, content = "Привет", createdAt = now),
            MessageEntity(id = 2, conversationId = 123L, role = MessageRole.ASSISTANT, content = "Привет!", createdAt = now + 1000)
        )
        coEvery { messageDao.getByConversationIdPaginated(123L, 20, 0) } returns entities

        // Act
        val result = repository.getMessages(123L, 20, 0)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result[0] is ChatMessage.UserMessage)
        assertTrue(result[1] is ChatMessage.AssistantMessage)
    }

    @Test
    fun `getLastMessage - returns null when no messages`() = runTest {
        coEvery { messageDao.getLastMessage(123L) } returns null

        val result = repository.getLastMessage(123L)

        assertNull(result)
    }

    @Test
    fun `getLastMessage - returns last message when exists`() = runTest {
        val now = System.currentTimeMillis()
        val entity = MessageEntity(id = 1, conversationId = 123L, role = MessageRole.USER, content = "Последнее", createdAt = now)
        coEvery { messageDao.getLastMessage(123L) } returns entity

        val result = repository.getLastMessage(123L)

        assertNotNull(result)
        assertEquals("Последнее", (result as? ChatMessage.UserMessage)?.text)
    }
}