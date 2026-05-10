package com.example.resume_net.presentation.analysis

import com.example.resume_net.domain.model.AnalysisResult

data class AnalysisState(
    val resumeText: String = "",
    val isLoading: Boolean = false,
    val result: AnalysisResult? = null,
    val error: String? = null
)