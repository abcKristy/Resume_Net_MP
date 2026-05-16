package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.Conversation
import com.example.resume_net.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * UseCase для получения списка диалогов с пагинацией
 *
 * @param conversationRepository репозиторий для работы с диалогами
 */
class GetConversationsUseCase(
    private val conversationRepository: ConversationRepository
) {

    /**
     * Параметры для получения списка диалогов
     *
     * @param limit количество диалогов на страницу (по умолчанию 20)
     * @param offset смещение от начала (по умолчанию 0)
     * @param observe если true, возвращает Flow для реактивного обновления
     */
    data class Params(
        val limit: Int = 20,
        val offset: Int = 0,
        val observe: Boolean = false
    )

    /**
     * Получение списка диалогов (одноразово)
     *
     * @param params параметры
     * @return список диалогов
     */
    suspend operator fun invoke(params: Params = Params()): List<Conversation> = withContext(Dispatchers.IO) {
        conversationRepository.getConversations(
            limit = params.limit,
            offset = params.offset
        )
    }

    /**
     * Получение Flow диалогов (реактивное обновление)
     *
     * @return Flow списка диалогов
     */
    fun observe(): Flow<List<Conversation>> {
        return conversationRepository.observeConversations()
    }

    /**
     * Получение следующей страницы
     *
     * @param currentPage текущая страница (начиная с 0)
     * @param limit количество на страницу
     * @return новые параметры для следующей страницы
     */
    fun getNextPageParams(currentPage: Int, limit: Int = 20): Params {
        return Params(
            limit = limit,
            offset = (currentPage + 1) * limit
        )
    }

    /**
     * Проверка, есть ли ещё страницы
     */
    fun hasNextPage(totalCount: Int, currentOffset: Int, limit: Int): Boolean {
        return currentOffset + limit < totalCount
    }
}