package com.example.resume_net.presentation.newanalysis

sealed interface NewAnalysisEvent {
    data class UpdateResumeText(val text: String) : NewAnalysisEvent
    data class UpdateConversationTitle(val title: String) : NewAnalysisEvent  // ← новое
    data object Analyze : NewAnalysisEvent
    data object Cancel : NewAnalysisEvent
    data object ClearError : NewAnalysisEvent
    data object DismissModelLoadingDialog : NewAnalysisEvent
    data object RetryAnalysis : NewAnalysisEvent
    data object DismissDuplicateDialog : NewAnalysisEvent
    data object OpenExistingConversation : NewAnalysisEvent
}