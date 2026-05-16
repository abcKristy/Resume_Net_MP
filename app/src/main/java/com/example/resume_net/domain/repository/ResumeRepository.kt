package com.example.resume_net.domain.repository

import com.example.resume_net.domain.model.AnalysisResult

interface ResumeRepository {

    /**
     * Анализ резюме без кэширования
     * @param resume текст резюме
     * @return Result с результатом анализа или ошибкой
     */
    suspend fun analyze(resume: String): Result<AnalysisResult>

    /**
     * Анализ резюме с кэшированием
     * При повторном анализе того же текста возвращает результат из кэша
     * @param resumeText текст резюме
     * @return Result с результатом анализа или ошибкой
     */
    suspend fun analyzeWithCache(resumeText: String): Result<AnalysisResult>
}