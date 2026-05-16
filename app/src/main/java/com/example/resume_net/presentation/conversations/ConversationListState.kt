package com.example.resume_net.presentation.conversations

import com.example.resume_net.domain.model.Conversation

/**
 * Состояние экрана списка диалогов
 */
data class ConversationListState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val error: String? = null
)