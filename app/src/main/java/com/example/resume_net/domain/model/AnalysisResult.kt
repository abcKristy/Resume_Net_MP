package com.example.resume_net.domain.model

data class AnalysisResult(
    val score: Float,
    val issues: List<AnalysisIssue>,
    val warnings: List<AnalysisIssue>,
    val allTags: List<AnalysisIssue>
)