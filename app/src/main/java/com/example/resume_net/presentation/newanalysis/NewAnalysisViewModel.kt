package com.example.resume_net.presentation.newanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.usecase.CreateConversationUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewAnalysisViewModel(
    private val createConversationUseCase: CreateConversationUseCase
) : ViewModel() {

    companion object {
        private const val MIN_TEXT_LENGTH = 50
    }

    // ============= СОСТОЯНИЕ =============

    private val _state = MutableStateFlow(NewAnalysisState())
    val state: StateFlow<NewAnalysisState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<NewAnalysisEffect>()
    val effect: SharedFlow<NewAnalysisEffect> = _effect.asSharedFlow()

    // ============= ОБРАБОТКА СОБЫТИЙ =============

    fun onEvent(event: NewAnalysisEvent) {
        when (event) {
            is NewAnalysisEvent.UpdateResumeText -> updateResumeText(event.text)
            is NewAnalysisEvent.Analyze -> analyze()
            is NewAnalysisEvent.Cancel -> cancel()
            is NewAnalysisEvent.ClearError -> clearError()
            is NewAnalysisEvent.DismissModelLoadingDialog -> dismissModelLoadingDialog()
            is NewAnalysisEvent.RetryAnalysis -> retryAnalysis()
            is NewAnalysisEvent.DismissDuplicateDialog -> dismissDuplicateDialog()
            is NewAnalysisEvent.OpenExistingConversation -> openExistingConversation()
        }
    }

    /**
     * Обновление текста резюме
     */
    private fun updateResumeText(text: String) {
        val charCount = text.length
        val isAnalyzeEnabled = charCount >= MIN_TEXT_LENGTH

        // Показываем тултип если текст слишком короткий и пользователь пытается нажать
        if (charCount > 0 && charCount < MIN_TEXT_LENGTH) {
            _effect.tryEmit(NewAnalysisEffect.ShowTooltip(
                "Минимум $MIN_TEXT_LENGTH символов. Сейчас: $charCount"
            ))
        }

        _state.update { currentState ->
            currentState.copy(
                resumeText = text,
                charCount = charCount,
                isAnalyzeEnabled = isAnalyzeEnabled,
                error = null
            )
        }
    }

    /**
     * Запуск анализа
     */
    private fun analyze() {
        val currentText = _state.value.resumeText

        // Проверка длины текста
        if (currentText.length < MIN_TEXT_LENGTH) {
            _effect.tryEmit(NewAnalysisEffect.ShowError(
                "Минимум $MIN_TEXT_LENGTH символов для анализа. Сейчас: ${currentText.length}"
            ))
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val params = CreateConversationUseCase.Params(
                    resumeText = currentText,
                    useCache = true
                )
                val result = createConversationUseCase(params)

                // Проверка на дубликат (если conversationId == -1, значит результат из кэша)
                if (result.fromCache && result.conversationId == -1L) {
                    // Нужно найти существующий диалог с таким же текстом
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showDuplicateDialog = true,
                            duplicateConversationId = result.conversationId.takeIf { it != -1L }
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(NewAnalysisEffect.NavigateToChat(result.conversationId))
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }

                // Обработка ошибки загрузки модели
                when (e) {
                    is AnalysisError.ModelNotAvailable -> {
                        _state.update { it.copy(showModelLoadingDialog = true) }
                    }
                    else -> {
                        _effect.emit(NewAnalysisEffect.ShowError(e.message ?: "Ошибка анализа"))
                    }
                }
            }
        }
    }

    /**
     * Закрыть диалог загрузки модели
     */
    private fun dismissModelLoadingDialog() {
        _state.update { it.copy(showModelLoadingDialog = false) }
    }

    /**
     * Повторить анализ (после закрытия диалога модели)
     */
    private fun retryAnalysis() {
        _state.update { it.copy(showModelLoadingDialog = false) }
        analyze()
    }

    /**
     * Закрыть диалог дубликата
     */
    private fun dismissDuplicateDialog() {
        _state.update {
            it.copy(
                showDuplicateDialog = false,
                duplicateConversationId = null
            )
        }
    }

    /**
     * Открыть существующий диалог (при дубликате)
     */
    private fun openExistingConversation() {
        val conversationId = _state.value.duplicateConversationId
        if (conversationId != null && conversationId != -1L) {
            viewModelScope.launch {
                _effect.emit(NewAnalysisEffect.NavigateToChat(conversationId))
            }
        }
        dismissDuplicateDialog()
    }

    /**
     * Отмена (закрытие экрана)
     */
    private fun cancel() {
        viewModelScope.launch {
            _effect.emit(NewAnalysisEffect.NavigateBack)
        }
    }

    /**
     * Сброс ошибки
     */
    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}