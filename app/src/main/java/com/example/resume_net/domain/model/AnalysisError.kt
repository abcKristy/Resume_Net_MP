package com.example.resume_net.domain.model

sealed class AnalysisError : Exception() {
    data object EmptyResume : AnalysisError()
    data object TooShort : AnalysisError()
    data object ModelNotAvailable : AnalysisError()
    data class TokenizerError(override val message: String) : AnalysisError()
    data class InferenceError(override val message: String) : AnalysisError()
}