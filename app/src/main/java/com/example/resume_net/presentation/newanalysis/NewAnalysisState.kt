package com.example.resume_net.presentation.newanalysis

data class NewAnalysisState(
    val resumeText: String = "",
    val conversationTitle: String = "",
    val isLoading: Boolean = false,
    val charCount: Int = 0,
    val isAnalyzeEnabled: Boolean = false,
    val error: String? = null,
    val showModelLoadingDialog: Boolean = false,
    val showDuplicateDialog: Boolean = false,
    val duplicateConversationId: Long? = null
)