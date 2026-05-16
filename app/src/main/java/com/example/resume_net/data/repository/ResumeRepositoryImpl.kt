package com.example.resume_net.data.repository

import android.content.Context
import android.util.Log
import com.example.resume_net.data.ml.PyTorchModelFacade
import com.example.resume_net.data.tokenizer.BertTokenizer
import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.IssueSeverity
import com.example.resume_net.domain.model.ResumeTag
import com.example.resume_net.domain.repository.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import kotlinx.coroutines.delay

class ResumeRepositoryImpl(
    private val context: Context,
    private val tokenizer: BertTokenizer,
    private val modelDownloader: ModelDownloader
) : ResumeRepository {

    private var modelFacade: PyTorchModelFacade? = null
    private var recommendations: Map<String, String> = emptyMap()
    private var tags: List<String> = emptyList()
    private var thresholds = Thresholds(critical = 0.5f, warning = 0.3f)

    private var isModelLoaded = false
    private var isLoadingModel = false

    override suspend fun analyze(resume: String): Result<AnalysisResult> {
        return withContext(Dispatchers.Default) {
            try {
                if (!isModelLoaded && !isLoadingModel) {
                    loadModel()
                }

                var attempts = 0
                while (!isModelLoaded && attempts < 50) {
                    delay(100)
                    attempts++
                }

                if (!isModelLoaded) {
                    return@withContext Result.failure(AnalysisError.ModelNotAvailable)
                }

                val facade = requireModelLoaded()
                ensureTokenizerLoaded()
                loadMetadata()

                val (inputIds, attentionMask) = tokenizer.tokenize(resume)
                val (score, probs) = facade.predict(inputIds, attentionMask)

                val allTags = buildIssues(probs)

                Result.success(
                    AnalysisResult(
                        score = score,
                        issues = allTags.filter { it.severity == IssueSeverity.CRITICAL },
                        warnings = allTags.filter { it.severity == IssueSeverity.WARNING },
                        allTags = allTags
                    )
                )
            } catch (e: FileNotFoundException) {
                Log.e("RESUME_ANALYZER", "ModelNotAvailable", e)
                Result.failure(AnalysisError.ModelNotAvailable)
            } catch (e: IllegalStateException) {
                Log.e("RESUME_ANALYZER", "TokenizerError", e)
                Result.failure(AnalysisError.TokenizerError(e.message ?: "Tokenization failed"))
            } catch (e: Exception) {
                Log.e("RESUME_ANALYZER", "InferenceError", e)
                Result.failure(AnalysisError.InferenceError(e.message ?: "Inference failed"))
            }
        }
    }

    suspend fun loadModel() {
        if (isModelLoaded || isLoadingModel) return

        isLoadingModel = true
        withContext(Dispatchers.IO) {
            try {
                val modelPath = modelDownloader.getModelPath()
                Log.d("RESUME_ANALYZER", "Loading model from: $modelPath")
                val facade = PyTorchModelFacade(modelPath)
                facade.load()
                modelFacade = facade
                isModelLoaded = true
                Log.d("RESUME_ANALYZER", "Model loaded successfully")
            } catch (e: Exception) {
                Log.e("RESUME_ANALYZER", "Failed to load model", e)
                throw e
            } finally {
                isLoadingModel = false
            }
        }
    }

    /**
     * Освобождает ресурсы модели.
     * Вызывается при нехватке памяти или когда приложение уходит в фон.
     */
    fun releaseModel() {
        if (!isModelLoaded) return

        Log.d("RESUME_ANALYZER", "Releasing model resources")
        modelFacade?.close()
        modelFacade = null
        isModelLoaded = false

        recommendations = emptyMap()
        tags = emptyList()

        Log.d("RESUME_ANALYZER", "Model resources released successfully")
    }

    /**
     * Проверяет, загружена ли модель
     */
    fun isModelReady(): Boolean = isModelLoaded

    private fun requireModelLoaded(): PyTorchModelFacade {
        return modelFacade ?: throw FileNotFoundException("Model not loaded. Call loadModel() first.")
    }

    private fun ensureTokenizerLoaded() {
        tokenizer.load()
    }

    private fun loadMetadata() {
        if (recommendations.isNotEmpty() && tags.isNotEmpty()) return

        val json = Json { ignoreUnknownKeys = true }

        try {
            val recJson = context.assets.open("ml/recommendations.json")
                .bufferedReader().readText()
            val recDtos: Map<String, RecommendationDto> = json.decodeFromString(recJson)
            recommendations = recDtos.mapValues { it.value.recommendation }
        } catch (e: Exception) {
            Log.w("RESUME_ANALYZER", "Failed to load recommendations.json", e)
        }

        try {
            val metaJson = context.assets.open("ml/model_metadata.json")
                .bufferedReader().readText()
            val metadata: MetadataDto = json.decodeFromString(metaJson)
            tags = metadata.labels
            thresholds = Thresholds(
                critical = metadata.threshold_high,
                warning = metadata.threshold_low
            )
        } catch (e: Exception) {
            Log.e("RESUME_ANALYZER", "Failed to load metadata.json", e)
            throw e
        }
    }

    private fun buildIssues(probs: FloatArray): List<AnalysisIssue> {
        return probs.mapIndexed { index, prob ->
            val tagName = tags.getOrElse(index) { "unknown_$index" }
            val resumeTag = try {
                ResumeTag.valueOf(tagName.uppercase())
            } catch (e: IllegalArgumentException) {
                ResumeTag.NO_NUMBERS
            }
            val severity = when {
                prob >= thresholds.critical -> IssueSeverity.CRITICAL
                prob >= thresholds.warning -> IssueSeverity.WARNING
                else -> IssueSeverity.OK
            }
            AnalysisIssue(
                tag = resumeTag,
                probability = prob,
                severity = severity,
                recommendation = recommendations[tagName] ?: ""
            )
        }
    }

    data class Thresholds(val critical: Float, val warning: Float)

    @kotlinx.serialization.Serializable
    data class MetadataDto(
        val labels: List<String>,
        val threshold_high: Float,
        val threshold_low: Float,
        val max_length: Int = 300
    )

    @kotlinx.serialization.Serializable
    data class RecommendationDto(
        val name: String,
        val recommendation: String,
        val category: String = "",
        val severity: String = ""
    )
}