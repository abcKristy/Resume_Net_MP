package com.example.resume_net.domain.model

data class AnalysisIssue(
    val tag: ResumeTag,
    val probability: Float,
    val severity: IssueSeverity,
    val recommendation: String
)

enum class IssueSeverity {
    CRITICAL,
    WARNING,
    OK
}