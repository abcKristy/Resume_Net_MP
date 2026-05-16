package com.example.resume_net.presentation.conversations

/**
 * Эффекты (одноразовые действия) для экрана списка диалогов
 */
sealed interface ConversationListEffect {

    /**
     * Показать сообщение об ошибке
     */
    data class ShowError(val message: String) : ConversationListEffect

    /**
     * Показать уведомление об успешном действии
     */
    data class ShowSuccess(val message: String) : ConversationListEffect

    /**
     * Навигация к чату
     */
    data class NavigateToChat(val conversationId: Long) : ConversationListEffect
}