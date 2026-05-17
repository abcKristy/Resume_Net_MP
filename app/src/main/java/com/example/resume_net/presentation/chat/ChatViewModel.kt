package com.example.resume_net.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.repository.ConversationRepository
import com.example.resume_net.domain.repository.ResumeRepository
import com.example.resume_net.domain.usecase.AddMessageUseCase
import com.example.resume_net.domain.usecase.AnalyzeAndAddToConversationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val conversationRepository: ConversationRepository,
    private val resumeRepository: ResumeRepository,
    private val addMessageUseCase: AddMessageUseCase,
    private val analyzeAndAddToConversationUseCase: AnalyzeAndAddToConversationUseCase
) : ViewModel() {

    companion object {
        private const val MESSAGES_PAGE_SIZE = 20
        private const val MIN_RESUME_LENGTH = 50
    }

    // ID диалога из аргументов навигации
    private val conversationId: Long = savedStateHandle["conversationId"] ?: 0L

    // ============= СОСТОЯНИЕ =============

    private val _state = MutableStateFlow(ChatState(conversationId = conversationId))
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ChatEffect>()
    val effect: SharedFlow<ChatEffect> = _effect.asSharedFlow()

    // Пагинация
    private var currentOffset = 0
    private var isLoadingMore = false

    // ============= ИНИЦИАЛИЗАЦИЯ =============

    init {
        loadConversationMetadata()
        observeMessages()
    }

    /**
     * Загрузка метаданных диалога (название)
     */
    private fun loadConversationMetadata() {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            _state.update { it.copy(conversationTitle = conversation?.title ?: "Диалог") }
        }
    }

    /**
     * Подписка на Flow сообщений (реактивное обновление)
     * Новые сообщения добавляются автоматически
     */
    private fun observeMessages() {
        viewModelScope.launch {
            conversationRepository.observeMessages(conversationId)
                .catch { error ->
                    _effect.emit(ChatEffect.ShowError(error.message ?: "Ошибка загрузки сообщений"))
                }
                .collect { newMessages ->
                    // Обновляем список сообщений, сохраняя порядок
                    _state.update { currentState ->
                        currentState.copy(
                            messages = newMessages,
                            isLoading = false,
                            isLoadingMore = false
                        )
                    }

                    // Прокручиваем к последнему сообщению если оно новое
                    if (newMessages.isNotEmpty()) {
                        _effect.emit(ChatEffect.ScrollToBottom)
                    }
                }
        }
    }

    /**
     * Загрузка следующих сообщений (пагинация)
     */
    private fun loadMoreMessages() {
        if (isLoadingMore || !_state.value.hasMoreMessages) return

        isLoadingMore = true
        _state.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            try {
                val newOffset = currentOffset + MESSAGES_PAGE_SIZE
                val messages = conversationRepository.getMessages(
                    conversationId = conversationId,
                    limit = MESSAGES_PAGE_SIZE,
                    offset = newOffset
                )

                if (messages.isEmpty()) {
                    _state.update { it.copy(hasMoreMessages = false) }
                } else {
                    currentOffset = newOffset
                    // Добавляем загруженные сообщения в начало списка
                    _state.update { currentState ->
                        currentState.copy(
                            messages = messages + currentState.messages,
                            isLoadingMore = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMore = false, error = e.message) }
                _effect.emit(ChatEffect.ShowError(e.message ?: "Ошибка загрузки"))
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.LoadConversation -> loadConversation(event.conversationId)
            is ChatEvent.LoadMoreMessages -> loadMoreMessages()
            is ChatEvent.UpdateInputText -> updateInputText(event.text)
            is ChatEvent.SendMessage -> sendMessage()
            is ChatEvent.SendResumeForAnalysis -> sendResumeForAnalysis()
            is ChatEvent.RenameConversation -> renameConversation(event.newTitle)
            is ChatEvent.DeleteConversation -> deleteConversation()
            is ChatEvent.ClearError -> clearError()
            is ChatEvent.ShowRenameDialog -> showRenameDialog()
            is ChatEvent.DismissRenameDialog -> dismissRenameDialog()
            is ChatEvent.ShowDeleteConfirmation -> showDeleteConfirmation()
            is ChatEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is ChatEvent.ShowExportDialog -> showExportDialog()
            is ChatEvent.DismissExportDialog -> dismissExportDialog()
            is ChatEvent.ExportConversation -> exportConversation()
        }
    }

    /**
     * Загрузка диалога (первоначальная)
     */
    private fun loadConversation(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val messages = conversationRepository.getMessages(
                    conversationId = id,
                    limit = MESSAGES_PAGE_SIZE,
                    offset = 0
                )
                currentOffset = messages.size

                _state.update { currentState ->
                    currentState.copy(
                        messages = messages,
                        isLoading = false,
                        hasMoreMessages = messages.size == MESSAGES_PAGE_SIZE
                    )
                }

                if (messages.isNotEmpty()) {
                    _effect.emit(ChatEffect.ScrollToBottom)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.emit(ChatEffect.ShowError(e.message ?: "Ошибка загрузки"))
            }
        }
    }

    /**
     * Обновление текста ввода
     */
    private fun updateInputText(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    /**
     * Отправка обычного сообщения (не резюме)
     */
    private fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, inputText = "") }

            try {
                val params = AddMessageUseCase.Params(
                    conversationId = conversationId,
                    text = text,
                    isResumeAnalysis = false  // Обычное сообщение, не анализ
                )
                val result = addMessageUseCase(params)

                _state.update { it.copy(isLoading = false) }
                // Сообщение добавится автоматически через observeMessages()

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.emit(ChatEffect.ShowError(e.message ?: "Ошибка отправки"))
            }
        }
    }

    /**
     * Отправка резюме для анализа
     */
    private fun sendResumeForAnalysis() {
        val text = _state.value.inputText.trim()

        if (text.length < MIN_RESUME_LENGTH) {
            viewModelScope.launch {
                _effect.emit(ChatEffect.ShowError(
                    "Минимум $MIN_RESUME_LENGTH символов для анализа. Сейчас: ${text.length}"
                ))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isTyping = true, inputText = "") }

            try {
                val params = AnalyzeAndAddToConversationUseCase.Params(
                    conversationId = conversationId,
                    resumeText = text,
                    addUserMessage = true
                )
                val result = analyzeAndAddToConversationUseCase(params)

                _state.update { it.copy(isLoading = false, isTyping = false) }
                // Сообщения добавятся автоматически через observeMessages()

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, isTyping = false, error = e.message) }

                val errorMessage = when (e) {
                    is AnalysisError.EmptyResume -> "Текст резюме пуст"
                    is AnalysisError.TooShort -> "Текст слишком короткий (минимум 50 символов)"
                    is AnalysisError.ModelNotAvailable -> "Модель не загружена. Проверьте подключение"
                    else -> e.message ?: "Ошибка анализа"
                }
                _effect.emit(ChatEffect.ShowError(errorMessage))
            }
        }
    }

    /**
     * Переименование диалога
     */
    private fun renameConversation(newTitle: String) {
        if (newTitle.isBlank()) return

        viewModelScope.launch {
            try {
                conversationRepository.renameConversation(conversationId, newTitle)
                _state.update { it.copy(conversationTitle = newTitle) }
                _effect.emit(ChatEffect.ShowSuccess("Название изменено"))
            } catch (e: Exception) {
                _effect.emit(ChatEffect.ShowError(e.message ?: "Ошибка переименования"))
            }
        }
    }

    /**
     * Удаление диалога
     */
    private fun deleteConversation() {
        viewModelScope.launch {
            try {
                conversationRepository.deleteConversation(conversationId)
                _effect.emit(ChatEffect.ShowSuccess("Диалог удалён"))
                _effect.emit(ChatEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(ChatEffect.ShowError(e.message ?: "Ошибка удаления"))
            }
        }
    }

    /**
     * Сброс ошибки
     */
    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun showRenameDialog() {
        _state.update { it.copy(showRenameDialog = true) }
    }

    private fun dismissRenameDialog() {
        _state.update { it.copy(showRenameDialog = false) }
    }

    private fun showDeleteConfirmation() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    private fun showExportDialog() {
        _state.update { it.copy(showExportDialog = true) }
    }

    private fun dismissExportDialog() {
        _state.update { it.copy(showExportDialog = false) }
    }

    private fun exportConversation() {
        viewModelScope.launch {
            _state.update { it.copy(showExportDialog = false) }
            // TODO: реализовать экспорт (Этап 7)
            _effect.emit(ChatEffect.ShowSuccess("Экспорт будет доступен в следующей версии"))
        }
    }
}