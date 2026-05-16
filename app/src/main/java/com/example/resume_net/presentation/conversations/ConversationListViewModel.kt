package com.example.resume_net.presentation.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resume_net.domain.model.Conversation
import com.example.resume_net.domain.repository.ConversationRepository
import com.example.resume_net.domain.usecase.DeleteConversationUseCase
import com.example.resume_net.domain.usecase.GetConversationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConversationListViewModel(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val conversationRepository: ConversationRepository  // ← ДОБАВИТЬ для rename
) : ViewModel() {

    // ============= СОСТОЯНИЕ =============

    private val _state = MutableStateFlow(ConversationListState())
    val state: StateFlow<ConversationListState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ConversationListEffect>()
    val effect: SharedFlow<ConversationListEffect> = _effect.asSharedFlow()

    // Кэш всех диалогов (без фильтрации)
    private var allConversations: List<Conversation> = emptyList()

    // ============= ПОДПИСКА НА ИЗМЕНЕНИЯ В БД =============

    init {
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
                    allConversations = conversations
                    updateFilteredList()
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    /**
     * Обновление отфильтрованного списка на основе поискового запроса
     */
    private fun updateFilteredList() {
        val query = _state.value.searchQuery
        val filtered = if (query.isBlank()) {
            allConversations
        } else {
            allConversations.filter { conversation ->
                conversation.title.contains(query, ignoreCase = true) ||
                        conversation.lastMessagePreview?.contains(query, ignoreCase = true) == true
            }
        }
        _state.update { it.copy(filteredConversations = filtered) }
    }

    // ============= ОБРАБОТКА СОБЫТИЙ =============

    fun onEvent(event: ConversationListEvent) {
        when (event) {
            is ConversationListEvent.LoadConversations -> loadConversations()
            is ConversationListEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is ConversationListEvent.SetSearchActive -> setSearchActive(event.isActive)
            is ConversationListEvent.ClearSearch -> clearSearch()
            is ConversationListEvent.DeleteConversation -> deleteConversation(event.conversationId)
            is ConversationListEvent.RenameConversation -> renameConversation(event.conversationId, event.newTitle)
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
                allConversations = conversations
                updateFilteredList()
                _state.update { it.copy(isRefreshing = false) }
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
        _state.update { currentState ->
            currentState.copy(searchQuery = query)
        }
        updateFilteredList()
    }

    /**
     * Активация/деактивация режима поиска
     */
    private fun setSearchActive(isActive: Boolean) {
        if (!isActive) {
            clearSearch()
        }
        _state.update { it.copy(isSearchActive = isActive) }
    }

    /**
     * Очистка поиска
     */
    private fun clearSearch() {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = "",
                isSearchActive = false
            )
        }
        updateFilteredList()
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
                conversationRepository.renameConversation(conversationId, newTitle)
                _effect.emit(ConversationListEffect.ShowSuccess("Название изменено"))
                // Список обновится автоматически
            } catch (e: Exception) {
                _effect.emit(ConversationListEffect.ShowError(e.message ?: "Ошибка переименования"))
            }
        }
    }

    /**
     * Сброс ошибки
     */
    private fun clearError() {
        _state.update { it.copy(error = null) }
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