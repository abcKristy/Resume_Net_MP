package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UseCase для удаления диалога
 *
 * @param conversationRepository репозиторий для работы с диалогами
 */
class DeleteConversationUseCase(
    private val conversationRepository: ConversationRepository
) {

    /**
     * Выполнение удаления
     *
     * @param conversationId ID диалога для удаления
     * @return true если удаление успешно, false если диалог не найден
     * @throws Exception при ошибке БД
     */
    suspend operator fun invoke(conversationId: Long): Boolean = withContext(Dispatchers.IO) {
        // Проверяем существование диалога
        val conversation = conversationRepository.getConversationById(conversationId)
        if (conversation == null) {
            return@withContext false
        }

        // Удаляем диалог (сообщения удалятся каскадно)
        conversationRepository.deleteConversation(conversationId)
        return@withContext true
    }
}