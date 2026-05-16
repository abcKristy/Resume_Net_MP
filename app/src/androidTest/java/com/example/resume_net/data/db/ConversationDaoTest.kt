package com.example.resume_net.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ConversationDao

    @Before
    fun setUp() {
        // Создаём in-memory базу данных (не сохраняется на диск)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.conversationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ============= ВСТАВКА =============

    @Test
    fun testInsertAndGetById() = runBlocking {
        val now = System.currentTimeMillis()
        val conversation = ConversationEntity(
            title = "Тестовый диалог",
            createdAt = now,
            updatedAt = now,
            resumeTextHash = "abc123"
        )

        val id = dao.insert(conversation)
        assertTrue("ID должен быть больше 0", id > 0)

        val loaded = dao.getById(id)
        assertNotNull("Диалог должен существовать", loaded)
        assertEquals("Название не совпадает", "Тестовый диалог", loaded?.title)
        assertEquals("Хеш не совпадает", "abc123", loaded?.resumeTextHash)
    }

    @Test
    fun testInsertMultiple() = runBlocking {
        val now = System.currentTimeMillis()

        val id1 = dao.insert(ConversationEntity(title = "Диалог 1", createdAt = now, updatedAt = now))
        val id2 = dao.insert(ConversationEntity(title = "Диалог 2", createdAt = now, updatedAt = now))

        assertNotEquals("ID должны быть разными", id1, id2)

        val conv1 = dao.getById(id1)
        val conv2 = dao.getById(id2)

        assertEquals("Диалог 1", conv1?.title)
        assertEquals("Диалог 2", conv2?.title)
    }

    // ============= ОБНОВЛЕНИЕ =============

    @Test
    fun testUpdateTitle() = runBlocking {
        val now = System.currentTimeMillis()
        val id = dao.insert(
            ConversationEntity(
                title = "Старое название",
                createdAt = now,
                updatedAt = now
            )
        )

        dao.updateTitle(id, "Новое название")

        val updated = dao.getById(id)
        assertEquals("Название должно обновиться", "Новое название", updated?.title)
    }

    @Test
    fun testUpdateTimestamp() = runBlocking {
        val now = System.currentTimeMillis()
        val id = dao.insert(
            ConversationEntity(
                title = "Диалог",
                createdAt = now,
                updatedAt = now
            )
        )

        val newTimestamp = now + 3600000 // +1 час
        dao.updateTimestamp(id, newTimestamp)

        val updated = dao.getById(id)
        assertEquals("Timestamp должен обновиться", newTimestamp, updated?.updatedAt)
    }

    // ============= УДАЛЕНИЕ =============

    @Test
    fun testDeleteById() = runBlocking {
        val now = System.currentTimeMillis()
        val id = dao.insert(
            ConversationEntity(
                title = "Удаляемый диалог",
                createdAt = now,
                updatedAt = now
            )
        )

        // Проверяем что диалог существует
        assertNotNull("Диалог должен существовать до удаления", dao.getById(id))

        // Удаляем
        dao.deleteById(id)

        // Проверяем что удалился
        assertNull("Диалог должен быть удалён", dao.getById(id))
    }

    @Test
    fun testDeleteAll() = runBlocking {
        val now = System.currentTimeMillis()

        dao.insert(ConversationEntity(title = "Диалог 1", createdAt = now, updatedAt = now))
        dao.insert(ConversationEntity(title = "Диалог 2", createdAt = now, updatedAt = now))

        assertEquals("Должно быть 2 диалога", 2, dao.getCount())

        dao.deleteAll()

        assertEquals("После удаления всех должно быть 0", 0, dao.getCount())
    }

    // ============= ПОЛУЧЕНИЕ СПИСКА =============

    @Test
    fun testGetAllOrderedByUpdatedAtDesc() = runBlocking {
        val now = System.currentTimeMillis()

        // Создаём диалоги с разными updated_at
        val conv1 = ConversationEntity(title = "Самый старый", createdAt = now, updatedAt = now + 1000)
        val conv2 = ConversationEntity(title = "Самый новый", createdAt = now, updatedAt = now + 3000)
        val conv3 = ConversationEntity(title = "Средний", createdAt = now, updatedAt = now + 2000)

        dao.insert(conv1)
        dao.insert(conv2)
        dao.insert(conv3)

        val all = dao.getAllOrdered().first()

        assertEquals("Первый должен быть самый новый", "Самый новый", all[0].title)
        assertEquals("Второй должен быть средний", "Средний", all[1].title)
        assertEquals("Третий должен быть самый старый", "Самый старый", all[2].title)
    }

    @Test
    fun testGetPaginated() = runBlocking {
        val now = System.currentTimeMillis()

        // Создаём 25 диалогов
        for (i in 1..25) {
            dao.insert(
                ConversationEntity(
                    title = "Диалог $i",
                    createdAt = now,
                    updatedAt = now + i * 1000L
                )
            )
        }

        // Первая страница (10 записей)
        val page1 = dao.getPaginated(10, 0)
        assertEquals("Первая страница должна содержать 10 записей", 10, page1.size)
        assertEquals("Диалог 25", page1[0].title) // Самый новый (с большим updated_at)

        // Вторая страница (еще 10 записей)
        val page2 = dao.getPaginated(10, 10)
        assertEquals(10, page2.size)

        // Третья страница (оставшиеся 5)
        val page3 = dao.getPaginated(10, 20)
        assertEquals(5, page3.size)
        assertEquals("Диалог 5", page3.last().title)
    }

    // ============= ПОИСК =============

    @Test
    fun testFindByHash() = runBlocking {
        val now = System.currentTimeMillis()
        val hash = "unique_hash_12345"

        dao.insert(
            ConversationEntity(
                title = "Диалог с хешем",
                createdAt = now,
                updatedAt = now,
                resumeTextHash = hash
            )
        )

        val found = dao.findByHash(hash)
        assertNotNull("Диалог должен найден по хешу", found)
        assertEquals(hash, found?.resumeTextHash)

        val notFound = dao.findByHash("nonexistent_hash")
        assertNull("Несуществующий хеш не должен ничего найти", notFound)
    }

    @Test
    fun testFindByHashReturnsNullForNonexistent() = runBlocking {
        val result = dao.findByHash("hash_that_does_not_exist")
        assertNull(result)
    }

    // ============= СЧЕТЧИКИ =============

    @Test
    fun testGetCount() = runBlocking {
        val now = System.currentTimeMillis()

        assertEquals("Изначально должно быть 0", 0, dao.getCount())

        dao.insert(ConversationEntity(title = "Диалог 1", createdAt = now, updatedAt = now))
        assertEquals("После одного добавления должно быть 1", 1, dao.getCount())

        dao.insert(ConversationEntity(title = "Диалог 2", createdAt = now, updatedAt = now))
        assertEquals("После двух добавлений должно быть 2", 2, dao.getCount())

        dao.deleteAll()
        assertEquals("После очистки должно быть 0", 0, dao.getCount())
    }
}