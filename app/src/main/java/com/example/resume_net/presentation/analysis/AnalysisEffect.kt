package com.example.resume_net.presentation.analysis

sealed interface AnalysisEffect {
    data class ShowError(val message: String) : AnalysisEffect
    data object NavigateToResult : AnalysisEffect
}