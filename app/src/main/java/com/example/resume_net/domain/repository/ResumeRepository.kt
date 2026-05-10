package com.example.resume_net.domain.repository

import com.example.resume_net.domain.model.AnalysisResult

interface ResumeRepository {
    suspend fun analyze(resume: String): Result<AnalysisResult>
}