package com.example.resume_net.domain.usecase

import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.repository.ResumeRepository

class AnalyzeResumeUseCase(
    private val repository: ResumeRepository
) {
    suspend operator fun invoke(resumeText: String): Result<AnalysisResult> {
        val trimmed = resumeText.trim()

        if (trimmed.isEmpty()) {
            return Result.failure(AnalysisError.EmptyResume)
        }

        if (trimmed.length < 50) {
            return Result.failure(AnalysisError.TooShort)
        }

        return repository.analyze(trimmed)
    }
}