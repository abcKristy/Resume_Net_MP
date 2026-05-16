package com.example.resume_net.presentation.newanalysis

/**
 * Эффекты (одноразовые действия) для экрана нового анализа
 */
sealed interface NewAnalysisEffect {

    /**
     * Показать сообщение об ошибке (Snackbar)
     */
    data class ShowError(val message: String) : NewAnalysisEffect

    /**
     * Закрыть экран
     */
    data object NavigateBack : NewAnalysisEffect

    /**
     * Анализ завершён, перейти к чату
     */
    data class NavigateToChat(val conversationId: Long) : NewAnalysisEffect

    /**
     * Показать тултип (подсказку) при наведении на кнопку
     */
    data class ShowTooltip(val message: String) : NewAnalysisEffect
}