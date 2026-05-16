package com.example.resume_net.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    /**
     * Вставка нового диалога
     * При конфликте (если id существует) - заменяет
     * @return id вставленной записи
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity): Long

    /**
     * Получение диалога по ID
     * @param id идентификатор диалога
     * @return диалог или null если не найден
     */
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: Long): ConversationEntity?

    /**
     * Получение всех диалогов, отсортированных по обновлению (сначала новые)
     * Используем Flow для автоматического обновления UI при изменениях в БД
     * @return Flow списка диалогов
     */
    @Query("SELECT * FROM conversations ORDER BY updated_at DESC")
    fun getAllOrdered(): Flow<List<ConversationEntity>>

    /**
     * Получение диалогов с пагинацией (для больших списков)
     * @param limit количество записей на страницу
     * @param offset смещение от начала
     */
    @Query("SELECT * FROM conversations ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaginated(limit: Int, offset: Int): List<ConversationEntity>

    /**
     * Удаление диалога по ID
     * @param id идентификатор диалога
     */
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Обновление названия диалога
     * @param id идентификатор диалога
     * @param newTitle новое название
     */
    @Query("UPDATE conversations SET title = :newTitle WHERE id = :id")
    suspend fun updateTitle(id: Long, newTitle: String)

    /**
     * Обновление timestamp последнего сообщения
     * @param id идентификатор диалога
     * @param newTimestamp новый timestamp
     */
    @Query("UPDATE conversations SET updated_at = :newTimestamp WHERE id = :id")
    suspend fun updateTimestamp(id: Long, newTimestamp: Long)

    /**
     * Поиск диалога по хешу текста резюме
     * @param hash SHA-256 хеш текста
     * @return диалог или null
     */
    @Query("SELECT * FROM conversations WHERE resume_text_hash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): ConversationEntity?

    /**
     * Получение количества всех диалогов
     */
    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getCount(): Int

    /**
     * Удаление всех диалогов (для очистки истории)
     */
    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}