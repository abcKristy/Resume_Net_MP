package com.example.resume_net.presentation.chat

/**
 * Эффекты (одноразовые действия) для экрана чата
 */
sealed interface ChatEffect {

    /**
     * Показать сообщение об ошибке
     */
    data class ShowError(val message: String) : ChatEffect

    /**
     * Показать уведомление об успехе
     */
    data class ShowSuccess(val message: String) : ChatEffect

    /**
     * Навигация назад (при удалении диалога)
     */
    data object NavigateBack : ChatEffect

    /**
     * Прокрутить к последнему сообщению
     */
    data object ScrollToBottom : ChatEffect
}