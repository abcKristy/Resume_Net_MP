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
            is NewAnalysisEvent.UpdateConversationTitle -> updateConversationTitle(event.title)
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

    private fun analyze() {
        val currentText = _state.value.resumeText

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
                    useCache = false  // ← временно отключаем кэш для отладки
                )
                val result = createConversationUseCase(params)

                _state.update { it.copy(isLoading = false) }
                _effect.emit(NewAnalysisEffect.NavigateToChat(result.conversationId))

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.emit(NewAnalysisEffect.ShowError(e.message ?: "Ошибка анализа"))
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

    private fun updateConversationTitle(title: String) {
        _state.update { it.copy(conversationTitle = title) }
    }
}