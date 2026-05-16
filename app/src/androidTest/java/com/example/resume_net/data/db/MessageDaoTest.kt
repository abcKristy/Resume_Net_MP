package com.example.resume_net.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var messageDao: MessageDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        conversationDao = database.conversationDao()
        messageDao = database.messageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ============= ВСПОМОГАТЕЛЬНЫЙ МЕТОД =============

    private suspend fun createTestConversation(): Long {
        val now = System.currentTimeMillis()
        return conversationDao.insert(
            ConversationEntity(
                title = "Тестовый диалог",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    // ============= ВСТАВКА =============

    @Test
    fun testInsertAndGetById() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        val message = MessageEntity(
            conversationId = conversationId,
            role = MessageRole.USER,
            content = "Мое резюме...",
            createdAt = now
        )

        val messageId = messageDao.insert(message)
        assertTrue("ID сообщения должен быть больше 0", messageId > 0)

        val loaded = messageDao.getById(messageId)
        assertNotNull("Сообщение должно существовать", loaded)
        assertEquals("Текст сообщения не совпадает", "Мое резюме...", loaded?.content)
        assertEquals(MessageRole.USER, loaded?.role)
    }

    @Test
    fun testInsertAssistantMessageWithScoreAndTags() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        val tagsJson = """[{"tagName":"NO_NUMBERS","probability":0.8,"severity":"CRITICAL"}]"""

        val message = MessageEntity(
            conversationId = conversationId,
            role = MessageRole.ASSISTANT,
            content = "Анализ резюме",
            score = 4.2f,
            tagsJson = tagsJson,
            createdAt = now
        )

        val messageId = messageDao.insert(message)
        val loaded = messageDao.getById(messageId)

        assertNotNull(loaded)
        assertEquals(4.2f, loaded?.score)
        assertEquals(tagsJson, loaded?.tagsJson)
        assertEquals(MessageRole.ASSISTANT, loaded?.role)
    }

    @Test
    fun testInsertAll() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        val messages = listOf(
            MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "Сообщение 1", createdAt = now),
            MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Ответ 1", createdAt = now + 1000),
            MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "Сообщение 2", createdAt = now + 2000)
        )

        messageDao.insertAll(messages)

        val count = messageDao.getMessageCount(conversationId)
        assertEquals("Должно быть 3 сообщения", 3, count)
    }

    // ============= ПОЛУЧЕНИЕ СПИСКА =============

    @Test
    fun testGetByConversationIdPaginated() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        // Добавляем 25 сообщений
        for (i in 1..25) {
            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId,
                    role = if (i % 2 == 0) MessageRole.USER else MessageRole.ASSISTANT,
                    content = "Сообщение $i",
                    createdAt = now + i * 1000L
                )
            )
        }

        // Первая страница (10 сообщений, самые старые)
        val page1 = messageDao.getByConversationIdPaginated(conversationId, 10, 0)
        assertEquals("Первая страница: 10 сообщений", 10, page1.size)
        assertEquals("Сообщение 1", page1.first().content)
        assertEquals("Сообщение 10", page1.last().content)

        // Вторая страница
        val page2 = messageDao.getByConversationIdPaginated(conversationId, 10, 10)
        assertEquals(10, page2.size)
        assertEquals("Сообщение 11", page2.first().content)
        assertEquals("Сообщение 20", page2.last().content)

        // Третья страница (оставшиеся 5)
        val page3 = messageDao.getByConversationIdPaginated(conversationId, 10, 20)
        assertEquals(5, page3.size)
        assertEquals("Сообщение 21", page3.first().content)
        assertEquals("Сообщение 25", page3.last().content)
    }

    @Test
    fun testGetByConversationIdFlow() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        // Подписываемся на Flow
        var messages = emptyList<MessageEntity>()
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined).launch {
            messageDao.getByConversationIdFlow(conversationId).collect { list ->
                messages = list
            }
        }

        // Добавляем сообщение
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = MessageRole.USER,
                content = "Привет",
                createdAt = now
            )
        )

        // Даём время на обновление Flow
        kotlinx.coroutines.delay(100)

        assertEquals("Flow должен получить 1 сообщение", 1, messages.size)
        assertEquals("Привет", messages.first().content)

        job.cancel()
    }

    // ============= ПОСЛЕДНЕЕ СООБЩЕНИЕ =============

    @Test
    fun testGetLastMessage() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = MessageRole.USER,
                content = "Первое сообщение",
                createdAt = now
            )
        )

        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                content = "Последнее сообщение",
                createdAt = now + 5000
            )
        )

        val lastMessage = messageDao.getLastMessage(conversationId)
        assertNotNull(lastMessage)
        assertEquals("Последнее сообщение", lastMessage?.content)
        assertEquals(MessageRole.ASSISTANT, lastMessage?.role)
    }

    @Test
    fun testGetLastMessageReturnsNullForEmptyConversation() = runBlocking {
        val conversationId = createTestConversation()
        val lastMessage = messageDao.getLastMessage(conversationId)
        assertNull("Для пустого диалога последнее сообщение = null", lastMessage)
    }

    // ============= ПЕРВОЕ СООБЩЕНИЕ ПОЛЬЗОВАТЕЛЯ =============

    @Test
    fun testGetFirstUserMessage() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                content = "Привет! Чем могу помочь?",
                createdAt = now
            )
        )

        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = MessageRole.USER,
                content = "Проверь мое резюме",
                createdAt = now + 1000
            )
        )

        val firstUserMessage = messageDao.getFirstUserMessage(conversationId)
        assertNotNull(firstUserMessage)
        assertEquals("Проверь мое резюме", firstUserMessage?.content)
    }

    // ============= СООБЩЕНИЯ АССИСТЕНТА =============

    @Test
    fun testGetAssistantMessages() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        // Добавляем сообщения пользователя
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "Вопрос 1", createdAt = now))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "Вопрос 2", createdAt = now + 2000))

        // Добавляем ответы ассистента
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Ответ 1", createdAt = now + 1000))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Ответ 2", createdAt = now + 3000))

        val assistantMessages = messageDao.getAssistantMessages(conversationId)
        assertEquals("Должно быть 2 сообщения ассистента", 2, assistantMessages.size)
        assertEquals("Ответ 1", assistantMessages[0].content)
        assertEquals("Ответ 2", assistantMessages[1].content)
    }

    // ============= УДАЛЕНИЕ =============

    @Test
    fun testDeleteById() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        val messageId = messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = MessageRole.USER,
                content = "Сообщение для удаления",
                createdAt = now
            )
        )

        assertNotNull("Сообщение должно существовать", messageDao.getById(messageId))

        messageDao.deleteById(messageId)

        assertNull("Сообщение должно быть удалено", messageDao.getById(messageId))
    }

    @Test
    fun testDeleteByConversationId() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "Сообщение 1", createdAt = now))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Ответ 1", createdAt = now + 1000))

        assertEquals("Должно быть 2 сообщения", 2, messageDao.getMessageCount(conversationId))

        messageDao.deleteByConversationId(conversationId)

        assertEquals("После удаления должно быть 0", 0, messageDao.getMessageCount(conversationId))
    }

    // ============= КАСКАДНОЕ УДАЛЕНИЕ =============

    @Test
    fun testCascadeDelete_WhenConversationDeleted_MessagesAlsoDeleted() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        // Добавляем сообщения
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "Резюме", createdAt = now))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Анализ", score = 4.0f, createdAt = now + 1000))

        // Проверяем что сообщения есть
        assertEquals("Должно быть 2 сообщения", 2, messageDao.getMessageCount(conversationId))

        // Удаляем диалог
        conversationDao.deleteById(conversationId)

        // Проверяем что сообщения удалились каскадно
        val messages = messageDao.getByConversationIdPaginated(conversationId, 10, 0)
        assertTrue("Сообщения должны удалиться каскадно", messages.isEmpty())
        assertEquals("Счетчик сообщений должен быть 0", 0, messageDao.getMessageCount(conversationId))
    }

    // ============= ИЗБРАННОЕ =============

    @Test
    fun testSetFavorite() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        val messageId = messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                content = "Важный анализ",
                score = 4.8f,
                createdAt = now,
                isFavorite = false
            )
        )

        // Проверяем что изначально не в избранном
        var message = messageDao.getById(messageId)
        assertFalse(message?.isFavorite ?: true)

        // Добавляем в избранное
        messageDao.setFavorite(messageId, true)

        message = messageDao.getById(messageId)
        assertTrue(message?.isFavorite ?: false)
    }

    @Test
    fun testGetFavoriteMessages() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Анализ 1", score = 3.5f, createdAt = now, isFavorite = false))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Анализ 2", score = 4.2f, createdAt = now + 1000, isFavorite = true))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Анализ 3", score = 4.9f, createdAt = now + 2000, isFavorite = true))

        val favorites = messageDao.getFavoriteMessages()
        assertEquals("Должно быть 2 избранных сообщения", 2, favorites.size)
        assertEquals("Анализ 3", favorites[0].content) // Сортировка по created_at DESC
        assertEquals("Анализ 2", favorites[1].content)
    }

    // ============= СЧЕТЧИКИ =============

    @Test
    fun testGetMessageCount() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        assertEquals("Изначально 0 сообщений", 0, messageDao.getMessageCount(conversationId))

        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "1", createdAt = now))
        assertEquals(1, messageDao.getMessageCount(conversationId))

        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "2", createdAt = now + 1000))
        assertEquals(2, messageDao.getMessageCount(conversationId))
    }

    // ============= ПОИСК =============

    @Test
    fun testSearchMessages() = runBlocking {
        val conversationId = createTestConversation()
        val now = System.currentTimeMillis()

        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "Я разработчик Android", createdAt = now))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.ASSISTANT, content = "Хороший стек технологий", createdAt = now + 1000))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = MessageRole.USER, content = "У меня 5 лет опыта в iOS", createdAt = now + 2000))

        val androidResults = messageDao.searchMessages("Android")
        assertEquals("Должно найти 1 сообщение с Android", 1, androidResults.size)
        assertEquals("Я разработчик Android", androidResults.first().content)

        val developerResults = messageDao.searchMessages("разработчик")
        assertEquals(1, developerResults.size)

        val emptyResults = messageDao.searchMessages("Python")
        assertEquals(0, emptyResults.size)
    }
}