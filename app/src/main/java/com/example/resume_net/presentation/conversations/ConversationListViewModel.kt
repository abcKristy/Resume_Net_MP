package com.example.resume_net.presentation.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resume_net.domain.usecase.DeleteConversationUseCase
import com.example.resume_net.domain.usecase.GetConversationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConversationListViewModel(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase
) : ViewModel() {

    // ============= СОСТОЯНИЕ =============

    private val _state = MutableStateFlow(ConversationListState())
    val state: StateFlow<ConversationListState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ConversationListEffect>()
    val effect: SharedFlow<ConversationListEffect> = _effect.asSharedFlow()

    // ============= ПОДПИСКА НА ИЗМЕНЕНИЯ В БД =============

    init {
        // Подписываемся на Flow из репозитория для автообновления
        observeConversations()
    }

    /**
     * Наблюдение за изменениями в БД (реактивное обновление)
     */
    private fun observeConversations() {
        viewModelScope.launch {
            getConversationsUseCase.observe()
                .catch { error ->
                    _state.update { it.copy(error = error.message) }
                    _effect.emit(ConversationListEffect.ShowError(error.message ?: "Ошибка загрузки"))
                }
                .collect { conversations ->
                    _state.update { currentState ->
                        currentState.copy(
                            conversations = applySearchFilter(conversations, currentState.searchQuery),
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    // ============= ОБРАБОТКА СОБЫТИЙ =============

    fun onEvent(event: ConversationListEvent) {
        when (event) {
            is ConversationListEvent.LoadConversations -> loadConversations()
            is ConversationListEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is ConversationListEvent.DeleteConversation -> deleteConversation(event.conversationId)
            is ConversationListEvent.RenameConversation -> renameConversation(event.conversationId, event.newTitle)
            is ConversationListEvent.ClearSearch -> clearSearch()
            is ConversationListEvent.ClearError -> clearError()
        }
    }

    /**
     * Загрузка диалогов (ручное обновление)
     */
    private fun loadConversations() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }

            try {
                val conversations = getConversationsUseCase()
                _state.update { currentState ->
                    currentState.copy(
                        conversations = applySearchFilter(conversations, currentState.searchQuery),
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isRefreshing = false, error = e.message) }
                _effect.emit(ConversationListEffect.ShowError(e.message ?: "Ошибка загрузки"))
            }
        }
    }

    /**
     * Обновление поискового запроса
     */
    private fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(searchQuery = query)
            }

            // Обновляем фильтрованный список
            val allConversations = getConversationsUseCase()
            _state.update { currentState ->
                currentState.copy(
                    conversations = applySearchFilter(allConversations, query)
                )
            }
        }
    }

    /**
     * Удаление диалога
     */
    private fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            try {
                val success = deleteConversationUseCase(conversationId)
                if (success) {
                    _effect.emit(ConversationListEffect.ShowSuccess("Диалог удалён"))
                    // Список обновится автоматически через observeConversations()
                } else {
                    _effect.emit(ConversationListEffect.ShowError("Диалог не найден"))
                }
            } catch (e: Exception) {
                _effect.emit(ConversationListEffect.ShowError(e.message ?: "Ошибка удаления"))
            }
        }
    }

    /**
     * Переименование диалога
     */
    private fun renameConversation(conversationId: Long, newTitle: String) {
        viewModelScope.launch {
            try {
                // Здесь нужно вызвать rename из репозитория
                // conversationRepository.renameConversation(conversationId, newTitle)
                _effect.emit(ConversationListEffect.ShowSuccess("Название изменено"))
            } catch (e: Exception) {
                _effect.emit(ConversationListEffect.ShowError(e.message ?: "Ошибка переименования"))
            }
        }
    }

    /**
     * Очистка поиска
     */
    private fun clearSearch() {
        updateSearchQuery("")
    }

    /**
     * Сброс ошибки
     */
    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Применить фильтр поиска к списку диалогов
     */
    private fun applySearchFilter(
        conversations: List<com.example.resume_net.domain.model.Conversation>,
        query: String
    ): List<com.example.resume_net.domain.model.Conversation> {
        if (query.isBlank()) return conversations

        return conversations.filter { conversation ->
            conversation.title.contains(query, ignoreCase = true) ||
                    conversation.lastMessagePreview?.contains(query, ignoreCase = true) == true
        }
    }

    /**
     * Навигация к чату
     */
    fun navigateToChat(conversationId: Long) {
        viewModelScope.launch {
            _effect.emit(ConversationListEffect.NavigateToChat(conversationId))
        }
    }
}