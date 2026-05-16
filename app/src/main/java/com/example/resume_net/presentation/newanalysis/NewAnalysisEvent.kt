package com.example.resume_net.presentation.newanalysis

/**
 * События (намерения) пользователя на экране нового анализа
 */
sealed interface NewAnalysisEvent {

    /**
     * Обновление текста резюме
     */
    data class UpdateResumeText(val text: String) : NewAnalysisEvent

    /**
     * Запуск анализа
     */
    data object Analyze : NewAnalysisEvent

    /**
     * Отмена (закрытие экрана)
     */
    data object Cancel : NewAnalysisEvent

    /**
     * Сброс ошибки
     */
    data object ClearError : NewAnalysisEvent

    /**
     * Закрыть диалог загрузки модели
     */
    data object DismissModelLoadingDialog : NewAnalysisEvent

    /**
     * Повторить анализ после закрытия диалога модели
     */
    data object RetryAnalysis : NewAnalysisEvent

    /**
     * Закрыть диалог дубликата
     */
    data object DismissDuplicateDialog : NewAnalysisEvent

    /**
     * Открыть существующий диалог (при дубликате)
     */
    data object OpenExistingConversation : NewAnalysisEvent
}