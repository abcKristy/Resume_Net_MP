package com.example.resume_net.presentation.chat

/**
 * События (намерения) пользователя на экране чата
 */
sealed interface ChatEvent {

    /**
     * Загрузить начальные сообщения
     */
    data class LoadConversation(val conversationId: Long) : ChatEvent

    /**
     * Загрузить следующую страницу сообщений (скролл вверх)
     */
    data object LoadMoreMessages : ChatEvent

    /**
     * Обновить текст ввода
     */
    data class UpdateInputText(val text: String) : ChatEvent

    /**
     * Отправить сообщение (обычный текст или вопрос)
     */
    data object SendMessage : ChatEvent

    /**
     * Отправить новое резюме для анализа
     */
    data object SendResumeForAnalysis : ChatEvent

    /**
     * Переименовать диалог
     */
    data class RenameConversation(val newTitle: String) : ChatEvent

    /**
     * Удалить диалог
     */
    data object DeleteConversation : ChatEvent

    /**
     * Сбросить ошибку
     */
    data object ClearError : ChatEvent

    /**
     * Показать диалог переименования
     */
    data object ShowRenameDialog : ChatEvent

    /**
     * Показать диалог подтверждения удаления
     */
    data object ShowDeleteConfirmation : ChatEvent

    /**
     * Показать диалог экспорта
     */
    data object ShowExportDialog : ChatEvent

    /**
     * Закрыть диалог переименования
     */
    data object DismissRenameDialog : ChatEvent

    /**
     * Закрыть диалог удаления
     */
    data object DismissDeleteDialog : ChatEvent

    /**
     * Закрыть диалог экспорта
     */
    data object DismissExportDialog : ChatEvent

    /**
     * Выполнить экспорт
     */
    data object ExportConversation : ChatEvent
}