package com.example.resume_net.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.usecase.AnalyzeResumeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalysisViewModel(
    private val analyzeResumeUseCase: AnalyzeResumeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    private val _effects = Channel<AnalysisEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: AnalysisIntent) {
        when (intent) {
            is AnalysisIntent.UpdateText -> updateText(intent.text)
            is AnalysisIntent.Analyze -> analyze()
            is AnalysisIntent.ClearResult -> clearResult()
        }
    }

    private fun updateText(text: String) {
        _state.update { it.copy(resumeText = text, error = null) }
    }

    private fun analyze() {
        val text = _state.value.resumeText

        if (text.isBlank()) {
            _state.update { it.copy(error = "Введите текст резюме") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, result = null) }

            analyzeResumeUseCase(text)
                .onSuccess { result ->
                    _state.update { it.copy(isLoading = false, result = result) }
                    _effects.send(AnalysisEffect.NavigateToResult)
                }
                .onFailure { error ->
                    val message = when (error) {
                        is AnalysisError.EmptyResume -> "Текст резюме пуст"
                        is AnalysisError.TooShort -> "Текст слишком короткий (минимум 50 символов)"
                        is AnalysisError.ModelNotAvailable -> "Модель не загружена. Проверьте подключение к интернету"
                        is AnalysisError.TokenizerError -> "Ошибка обработки текста"
                        is AnalysisError.InferenceError -> "Ошибка анализа. Попробуйте позже"
                        else -> "Неизвестная ошибка"
                    }
                    _state.update { it.copy(isLoading = false, error = message) }
                    _effects.send(AnalysisEffect.ShowError(message))
                }
        }
    }

    private fun clearResult() {
        _state.update { it.copy(result = null, error = null) }
    }
}