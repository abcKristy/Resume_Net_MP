package com.example.resume_net.presentation.newanalysis

/**
 * Состояние экрана нового анализа
 */
data class NewAnalysisState(
    val resumeText: String = "",
    val isLoading: Boolean = false,
    val charCount: Int = 0,
    val isAnalyzeEnabled: Boolean = false,
    val error: String? = null,
    val showModelLoadingDialog: Boolean = false,
    val showDuplicateDialog: Boolean = false,
    val duplicateConversationId: Long? = null
)