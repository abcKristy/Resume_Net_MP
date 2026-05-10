package com.example.resume_net.presentation.analysis

sealed interface AnalysisIntent {
    data class UpdateText(val text: String) : AnalysisIntent
    data object Analyze : AnalysisIntent
    data object ClearResult : AnalysisIntent
}