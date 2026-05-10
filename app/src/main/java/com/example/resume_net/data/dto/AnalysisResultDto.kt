package com.example.resume_net.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResultDto(
    val score: Float,
    val probs: List<Float>
)