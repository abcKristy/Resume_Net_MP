package com.example.resume_net.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resume_net.presentation.model.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResumeViewModel(
    private val analyzeResumeUseCase: Any, // Заменим в Шаге 3
    private val getModelStatusUseCase: Any,
    private val validateTextUseCase: Any
) : ViewModel() {

    private val _inputState = MutableStateFlow(ResumeInputState())
    val inputState: StateFlow<ResumeInputState> = _inputState.asStateFlow()

    private val _resultState = MutableStateFlow(AnalysisResultState())
    val resultState: StateFlow<AnalysisResultState> = _resultState.asStateFlow()

    private val _modelDownloadState = MutableStateFlow(ModelDownloadState())
    val modelDownloadState: StateFlow<ModelDownloadState> = _modelDownloadState.asStateFlow()

    private val _effects = Channel<ResumeEffect>(Channel.BUFFERED)
    val effects: Flow<ResumeEffect> = _effects.receiveAsFlow()

    fun dispatch(intent: ResumeIntent) {
        when (intent) {
            is ResumeIntent.UpdateText -> onUpdateText(intent.text)
            is ResumeIntent.AnalyzeText -> onAnalyzeText(intent.text)
            is ResumeIntent.RetryDownload -> onRetryDownload()
            is ResumeIntent.ClearResult -> onClearResult()
        }
    }

    private fun onUpdateText(text: String) {
        _inputState.update { state ->
            state.copy(
                text = text,
                isValid = text.isNotBlank(),
                validationError = null,
                error = null
            )
        }
    }

    private fun onAnalyzeText(text: String) {
        // Будет реализовано в Шаге 3
    }

    private fun onRetryDownload() {
        // Будет реализовано в Шаге 3
    }

    private fun onClearResult() {
        _resultState.value = AnalysisResultState()
        viewModelScope.launch {
            _effects.send(ResumeEffect.NavigateToInput)
        }
    }

    init {
        // Подписка на статус модели — в Шаге 3
    }
}