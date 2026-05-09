package com.example.resume_net.presentation.model

data class ResumeInputState(
    val text: String = "",
    val isValid: Boolean = false,
    val validationError: String? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null
)