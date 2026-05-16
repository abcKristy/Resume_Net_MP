package com.example.resume_net.presentation.chat

import com.example.resume_net.domain.model.ChatMessage

/**
 * Состояние экрана чата
 */
data class ChatState(
    val conversationId: Long = 0L,
    val conversationTitle: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isTyping: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val inputText: String = "",
    val error: String? = null,

    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showExportDialog: Boolean = false

)