package com.example.resume_net.presentation.conversations

/**
 * События (намерения) пользователя на экране списка диалогов
 */
sealed interface ConversationListEvent {

    /**
     * Загрузить список диалогов
     */
    data object LoadConversations : ConversationListEvent

    /**
     * Обновить поисковый запрос
     */
    data class UpdateSearchQuery(val query: String) : ConversationListEvent

    /**
     * Активировать/деактивировать поиск
     */
    data class SetSearchActive(val isActive: Boolean) : ConversationListEvent

    /**
     * Очистить поиск
     */
    data object ClearSearch : ConversationListEvent

    /**
     * Удалить диалог
     */
    data class DeleteConversation(val conversationId: Long) : ConversationListEvent

    /**
     * Переименовать диалог
     */
    data class RenameConversation(
        val conversationId: Long,
        val newTitle: String
    ) : ConversationListEvent

    /**
     * Сбросить ошибку
     */
    data object ClearError : ConversationListEvent
}