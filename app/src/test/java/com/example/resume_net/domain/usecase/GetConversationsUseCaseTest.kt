package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.Conversation
import com.example.resume_net.domain.repository.ConversationRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GetConversationsUseCaseTest {

    private lateinit var conversationRepository: ConversationRepository
    private lateinit var getConversationsUseCase: GetConversationsUseCase

    @Before
    fun setUp() {
        conversationRepository = mockk(relaxed = true)
        getConversationsUseCase = GetConversationsUseCase(conversationRepository)
    }

    private fun createMockConversation(
        id: Long,
        title: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Conversation {
        return Conversation(
            id = id,
            title = title,
            createdAt = updatedAt - 10000,
            updatedAt = updatedAt,
            lastMessagePreview = "Последнее сообщение",
            lastMessageTimestamp = updatedAt,
            lastScore = 4.2f
        )
    }

    private fun createMockConversationList(): List<Conversation> {
        val now = System.currentTimeMillis()
        return listOf(
            createMockConversation(1, "Первый диалог", now),
            createMockConversation(2, "Второй диалог", now - 1000),
            createMockConversation(3, "Третий диалог", now - 2000)
        )
    }

    @Test
    fun `getConversations - success with default params`() = runTest {
        // Arrange
        val expectedConversations = createMockConversationList()
        coEvery { conversationRepository.getConversations(20, 0) } returns expectedConversations

        // Act
        val result = getConversationsUseCase()

        // Assert
        assertNotNull(result)
        assertEquals(3, result.size)
        assertEquals("Первый диалог", result[0].title)
        assertEquals("Второй диалог", result[1].title)
        assertEquals("Третий диалог", result[2].title)

        coVerify(exactly = 1) { conversationRepository.getConversations(20, 0) }
    }

    @Test
    fun `getConversations - success with custom pagination`() = runTest {
        // Arrange
        val limit = 10
        val offset = 5
        val expectedConversations = createMockConversationList()

        coEvery { conversationRepository.getConversations(limit, offset) } returns expectedConversations

        // Act
        val params = GetConversationsUseCase.Params(limit = limit, offset = offset)
        val result = getConversationsUseCase(params)

        // Assert
        assertNotNull(result)
        assertEquals(3, result.size)

        coVerify(exactly = 1) { conversationRepository.getConversations(limit, offset) }
    }

    @Test
    fun `getConversations - returns empty list when no conversations`() = runTest {
        // Arrange
        coEvery { conversationRepository.getConversations(20, 0) } returns emptyList()

        // Act
        val result = getConversationsUseCase()

        // Assert
        assertNotNull(result)
        assertTrue(result.isEmpty())

        coVerify(exactly = 1) { conversationRepository.getConversations(20, 0) }
    }

    @Test
    fun `getConversations - pagination with multiple pages`() = runTest {
        // Arrange
        val pageSize = 10

        // Первая страница (10 записей)
        val firstPage = (1..10).map { i ->
            createMockConversation(i.toLong(), "Диалог $i", System.currentTimeMillis() - i * 1000L)
        }

        // Вторая страница (5 записей)
        val secondPage = (11..15).map { i ->
            createMockConversation(i.toLong(), "Диалог $i", System.currentTimeMillis() - i * 1000L)
        }

        coEvery { conversationRepository.getConversations(pageSize, 0) } returns firstPage
        coEvery { conversationRepository.getConversations(pageSize, pageSize) } returns secondPage

        // Act
        val firstPageResult = getConversationsUseCase(GetConversationsUseCase.Params(limit = pageSize, offset = 0))
        val secondPageResult = getConversationsUseCase(GetConversationsUseCase.Params(limit = pageSize, offset = pageSize))

        // Assert
        assertEquals(10, firstPageResult.size)
        assertEquals(5, secondPageResult.size)
        assertEquals("Диалог 1", firstPageResult[0].title)
        assertEquals("Диалог 11", secondPageResult[0].title)

        coVerify(exactly = 1) { conversationRepository.getConversations(pageSize, 0) }
        coVerify(exactly = 1) { conversationRepository.getConversations(pageSize, pageSize) }
    }

    @Test
    fun `getConversations - observe returns flow of conversations`() = runTest {
        // Arrange
        val expectedConversations = createMockConversationList()
        val flow = flowOf(expectedConversations)

        coEvery { conversationRepository.observeConversations() } returns flow

        // Act
        val result = getConversationsUseCase.observe()

        // Assert
        assertNotNull(result)
        coVerify(exactly = 1) { conversationRepository.observeConversations() }
    }

    @Test
    fun `getNextPageParams - calculates correct offset`() = runTest {
        // Arrange
        val currentPage = 2
        val limit = 15

        // Act
        val nextParams = getConversationsUseCase.getNextPageParams(currentPage, limit)

        // Assert
        assertEquals(limit, nextParams.limit)
        assertEquals((currentPage + 1) * limit, nextParams.offset)
    }

    @Test
    fun `getNextPageParams - default limit 20`() = runTest {
        // Act
        val nextParams = getConversationsUseCase.getNextPageParams(0)

        // Assert
        assertEquals(20, nextParams.limit)
        assertEquals(20, nextParams.offset)
    }

    @Test
    fun `hasNextPage - returns true when more pages exist`() = runTest {
        // Arrange
        val totalCount = 50
        val currentOffset = 20
        val limit = 20

        // Act
        val hasNext = getConversationsUseCase.hasNextPage(totalCount, currentOffset, limit)

        // Assert
        assertTrue(hasNext) // 20 + 20 = 40 < 50
    }

    @Test
    fun `hasNextPage - returns false when no more pages`() = runTest {
        // Arrange
        val totalCount = 40
        val currentOffset = 20
        val limit = 20

        // Act
        val hasNext = getConversationsUseCase.hasNextPage(totalCount, currentOffset, limit)

        // Assert
        assertFalse(hasNext) // 20 + 20 = 40 == 40
    }

    @Test
    fun `hasNextPage - returns false on exact last page`() = runTest {
        // Arrange
        val totalCount = 45
        val currentOffset = 40
        val limit = 20

        // Act
        val hasNext = getConversationsUseCase.hasNextPage(totalCount, currentOffset, limit)

        // Assert
        assertFalse(hasNext) // 40 + 20 = 60 > 45, но логика: offset + limit < totalCount
    }

    @Test
    fun `getConversations - should handle repository exception`() = runTest {
        // Arrange
        val exception = RuntimeException("Database connection failed")
        coEvery { conversationRepository.getConversations(any(), any()) } throws exception

        // Act & Assert
        try {
            getConversationsUseCase()
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Database connection failed", e.message)
        }
    }

    @Test
    fun `getConversations - preserves sorting order from repository`() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val sortedByUpdatedAt = listOf(
            createMockConversation(1, "Самый новый", now),
            createMockConversation(2, "Средний", now - 5000),
            createMockConversation(3, "Самый старый", now - 10000)
        )

        coEvery { conversationRepository.getConversations(20, 0) } returns sortedByUpdatedAt

        // Act
        val result = getConversationsUseCase()

        // Assert
        assertEquals("Самый новый", result[0].title)
        assertEquals("Средний", result[1].title)
        assertEquals("Самый старый", result[2].title)
    }
}