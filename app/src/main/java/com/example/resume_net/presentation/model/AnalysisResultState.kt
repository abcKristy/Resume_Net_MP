package com.example.resume_net.presentation.model

data class AnalysisResultState(
    val score: Float = 0f,
    val criticalIssues: List<IssueUi> = emptyList(),
    val warnings: List<IssueUi> = emptyList(),
    val allTags: List<TagRecommendationUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class IssueUi(
    val tag: String,
    val tagLabel: String,
    val probability: Float,
    val description: String
)

data class TagRecommendationUi(
    val tag: String,
    val tagLabel: String,
    val probability: Float,
    val recommendation: String
)