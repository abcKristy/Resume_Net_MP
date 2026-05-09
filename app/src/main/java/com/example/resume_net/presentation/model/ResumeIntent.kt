package com.example.resume_net.presentation.model

sealed interface ResumeIntent {
    data class AnalyzeText(val text: String) : ResumeIntent
    data object RetryDownload : ResumeIntent
    data object ClearResult : ResumeIntent
    data class UpdateText(val text: String) : ResumeIntent
}