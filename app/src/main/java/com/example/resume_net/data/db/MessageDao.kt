package com.example.resume_net.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    /**
     * Вставка нового сообщения
     * @return id вставленного сообщения
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    /**
     * Вставка нескольких сообщений (например, при восстановлении истории)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    /**
     * Получение сообщения по ID
     */
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: Long): MessageEntity?

    /**
     * Получение всех сообщений диалога с пагинацией
     * Сортировка по created_at ASC (старые сверху, новые снизу)
     *
     * @param conversationId ID диалога
     * @param limit количество сообщений на страницу
     * @param offset смещение от начала
     * @return список сообщений
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
        ORDER BY created_at ASC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getByConversationIdPaginated(
        conversationId: Long,
        limit: Int,
        offset: Int
    ): List<MessageEntity>

    /**
     * Получение всех сообщений диалога с Flow (для реактивного UI)
     * Автоматически обновляет список при изменениях в БД
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
        ORDER BY created_at ASC
    """)
    fun getByConversationIdFlow(conversationId: Long): Flow<List<MessageEntity>>

    /**
     * Получение последнего сообщения в диалоге
     * @return последнее сообщение или null если диалог пуст
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
        ORDER BY created_at DESC 
        LIMIT 1
    """)
    suspend fun getLastMessage(conversationId: Long): MessageEntity?

    /**
     * Получение первого сообщения пользователя в диалоге (исходное резюме)
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId AND role = 'USER'
        ORDER BY created_at ASC 
        LIMIT 1
    """)
    suspend fun getFirstUserMessage(conversationId: Long): MessageEntity?

    /**
     * Получение всех сообщений ассистента в диалоге
     */
    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId AND role = 'ASSISTANT'
        ORDER BY created_at ASC
    """)
    suspend fun getAssistantMessages(conversationId: Long): List<MessageEntity>

    /**
     * Удаление всех сообщений диалога
     * (Обычно не нужно использовать напрямую, т.к. стоит CASCADE)
     */
    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteByConversationId(conversationId: Long)

    /**
     * Удаление одного сообщения по ID
     */
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteById(messageId: Long)

    /**
     * Обновление флага избранного
     */
    @Query("UPDATE messages SET is_favorite = :isFavorite WHERE id = :messageId")
    suspend fun setFavorite(messageId: Long, isFavorite: Boolean)

    /**
     * Получение избранных сообщений (для быстрого доступа к важным анализам)
     */
    @Query("""
        SELECT * FROM messages 
        WHERE is_favorite = 1 
        ORDER BY created_at DESC
    """)
    suspend fun getFavoriteMessages(): List<MessageEntity>

    /**
     * Подсчет количества сообщений в диалоге
     */
    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId")
    suspend fun getMessageCount(conversationId: Long): Int

    /**
     * Поиск сообщений по тексту (для функции поиска)
     * @param query поисковая строка
     */
    @Query("""
        SELECT * FROM messages 
        WHERE content LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    suspend fun searchMessages(query: String): List<MessageEntity>
}