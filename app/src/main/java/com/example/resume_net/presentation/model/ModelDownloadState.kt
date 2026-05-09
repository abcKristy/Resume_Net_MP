package com.example.resume_net.presentation.model

data class ModelDownloadState(
    val status: DownloadStatus = DownloadStatus.NOT_STARTED,
    val progress: Float = 0f, // 0.0..1.0
    val error: String? = null
)

enum class DownloadStatus {
    NOT_STARTED,
    DOWNLOADING,
    READY,
    ERROR
}