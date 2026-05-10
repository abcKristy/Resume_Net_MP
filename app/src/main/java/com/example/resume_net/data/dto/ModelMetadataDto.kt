package com.example.resume_net.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModelMetadataDto(
    val tags: List<String>,
    val thresholds: ThresholdsDto,
    val max_length: Int
)

@Serializable
data class ThresholdsDto(
    val critical: Float,
    val warning: Float
)