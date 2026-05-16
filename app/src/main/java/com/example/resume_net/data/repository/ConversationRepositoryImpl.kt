package com.example.resume_net.data.repository

import com.example.resume_net.data.db.ConversationDao
import com.example.resume_net.data.db.MessageDao
import com.example.resume_net.data.db.MessageRole
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.model.Conversation
import com.example.resume_net.domain.model.mapper.ConversationMapper
import com.example.resume_net.domain.model.mapper.MessageMapper
import com.example.resume_net.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ConversationRepository {

    companion object {
        private const val DEFAULT_LIMIT = 20
    }

    /**
     * Вычисление SHA-256 хеша текста для кэширования
     */
    private fun hashText(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Генерация названия диалога из текста резюме
     */
    private fun generateTitle(resumeText: String): String {
        val lines = resumeText.lines()
            .filter { it.isNotBlank() }
            .firstOrNull()
            ?: resumeText

        return when {
            lines.length <= 30 -> lines
            else -> lines.substring(0, 30) + "..."
        }
    }

    override suspend fun createConversation(
        resumeText: String,
        analysisResult: AnalysisResult
    ): Long = withContext(Dispatchers.IO) {
        // 1. Создаём диалог
        val title = generateTitle(resumeText)
        val hash = hashText(resumeText)
        val conversationEntity = ConversationMapper.newEntity(
            title = title,
            resumeTextHash = hash
        )

        val conversationId = conversationDao.insert(conversationEntity)

        // 2. Добавляем сообщение пользователя
        val userMessage = MessageMapper.newUserMessage(
            conversationId = conversationId,
            text = resumeText
        )
        val userEntity = MessageMapper.toUserEntity(conversationId, userMessage)
        messageDao.insert(userEntity)

        // 3. Добавляем сообщение ассистента
        val assistantMessage = MessageMapper.newAssistantMessage(
            conversationId = conversationId,
            analysisResult = analysisResult
        )
        val assistantEntity = MessageMapper.toAssistantEntity(conversationId, assistantMessage)
        messageDao.insert(assistantEntity)

        return@withContext conversationId
    }

    override suspend fun getConversations(
        limit: Int,
        offset: Int
    ): List<Conversation> = withContext(Dispatchers.IO) {
        val entities = conversationDao.getPaginated(limit, offset)

        entities.map { entity ->
            val lastMessage = messageDao.getLastMessage(entity.id)
            ConversationMapper.toDomain(entity, lastMessage)
        }
    }

    override fun observeConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllOrdered().map { entities ->
            // Для Flow загружаем последние сообщения (может быть неоптимально для больших списков)
            entities.mapNotNull { entity ->
                val lastMessage = runBlockingOrNull { messageDao.getLastMessage(entity.id) }
                ConversationMapper.toDomain(entity, lastMessage)
            }
        }
    }

    override suspend fun getConversationById(id: Long): Conversation? = withContext(Dispatchers.IO) {
        val entity = conversationDao.getById(id) ?: return@withContext null
        val lastMessage = messageDao.getLastMessage(id)
        ConversationMapper.toDomain(entity, lastMessage)
    }

    override suspend fun deleteConversation(id: Long) = withContext(Dispatchers.IO) {
        conversationDao.deleteById(id)
        // Сообщения удалятся каскадно благодаря FOREIGN KEY CASCADE
    }

    override suspend fun renameConversation(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        conversationDao.updateTitle(id, newTitle)
    }

    override suspend fun addUserMessage(
        conversationId: Long,
        text: String
    ): Result<ChatMessage.UserMessage> = withContext(Dispatchers.IO) {
        try {
            // Проверяем существование диалога
            val conversation = conversationDao.getById(conversationId)
            if (conversation == null) {
                return@withContext Result.failure(Exception("Conversation not found"))
            }

            // Создаём сообщение
            val userMessage = MessageMapper.newUserMessage(conversationId, text)
            val entity = MessageMapper.toUserEntity(conversationId, userMessage)
            val messageId = messageDao.insert(entity)

            // Обновляем timestamp диалога
            conversationDao.updateTimestamp(conversationId, System.currentTimeMillis())

            // Возвращаем созданное сообщение с ID
            val createdMessage = userMessage.copy(id = messageId)
            Result.success(createdMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAssistantMessage(
        conversationId: Long,
        analysisResult: AnalysisResult
    ): Result<ChatMessage.AssistantMessage> = withContext(Dispatchers.IO) {
        try {
            // Проверяем существование диалога
            val conversation = conversationDao.getById(conversationId)
            if (conversation == null) {
                return@withContext Result.failure(Exception("Conversation not found"))
            }

            // Создаём сообщение
            val assistantMessage = MessageMapper.newAssistantMessage(conversationId, analysisResult)
            val entity = MessageMapper.toAssistantEntity(conversationId, assistantMessage)
            val messageId = messageDao.insert(entity)

            // Обновляем timestamp диалога
            conversationDao.updateTimestamp(conversationId, System.currentTimeMillis())

            // Возвращаем созданное сообщение с ID
            val createdMessage = assistantMessage.copy(id = messageId)
            Result.success(createdMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(
        conversationId: Long,
        limit: Int,
        offset: Int
    ): List<ChatMessage> = withContext(Dispatchers.IO) {
        val entities = messageDao.getByConversationIdPaginated(conversationId, limit, offset)

        entities.mapNotNull { entity ->
            when (entity.role) {
                MessageRole.USER -> MessageMapper.toUserMessage(entity)
                MessageRole.ASSISTANT -> MessageMapper.toAssistantMessage(entity)
                else -> null
            }
        }
    }

    override fun observeMessages(conversationId: Long): Flow<List<ChatMessage>> {
        return messageDao.getByConversationIdFlow(conversationId).map { entities ->
            entities.mapNotNull { entity ->
                when (entity.role) {
                    MessageRole.USER -> MessageMapper.toUserMessage(entity)
                    MessageRole.ASSISTANT -> MessageMapper.toAssistantMessage(entity)
                    else -> null
                }
            }
        }
    }

    override suspend fun getLastMessage(conversationId: Long): ChatMessage? = withContext(Dispatchers.IO) {
        val entity = messageDao.getLastMessage(conversationId) ?: return@withContext null
        when (entity.role) {
            MessageRole.USER -> MessageMapper.toUserMessage(entity)
            MessageRole.ASSISTANT -> MessageMapper.toAssistantMessage(entity)
            else -> null
        }
    }
}

/**
 * Вспомогательная функция для runBlocking в Flow (только для простых случаев)
 */
private inline fun <T> runBlockingOrNull(crossinline block: suspend () -> T): T? {
    return kotlinx.coroutines.runBlocking {
        try {
            block()
        } catch (e: Exception) {
            null
        }
    }
}