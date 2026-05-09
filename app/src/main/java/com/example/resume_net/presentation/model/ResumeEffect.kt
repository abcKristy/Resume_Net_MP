package com.example.resume_net.presentation.model

sealed interface ResumeEffect {
    data class ShowError(val message: String) : ResumeEffect
    data object NavigateToResult : ResumeEffect
    data object NavigateToInput : ResumeEffect
}