package com.example.resume_net.data.repository

import android.content.Context
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

class ResumeRepositoryImpl(
    private val context: Context,
    private val tokenizer: BertTokenizer,
    private val modelDownloader: ModelDownloader
) : ResumeRepository {

    private var modelFacade: PyTorchModelFacade? = null
    private var recommendations: Map<String, String> = emptyMap()
    private var tags: List<String> = emptyList()
    private var thresholds = Thresholds(critical = 0.5f, warning = 0.3f)

    override suspend fun analyze(resume: String): Result<AnalysisResult> {
        return withContext(Dispatchers.Default) {
            try {
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
                android.util.Log.e("RESUME_ANALYZER", "ModelNotAvailable", e)
                Result.failure(AnalysisError.ModelNotAvailable)
            } catch (e: IllegalStateException) {
                android.util.Log.e("RESUME_ANALYZER", "TokenizerError", e)
                Result.failure(AnalysisError.TokenizerError(e.message ?: "Tokenization failed"))
            } catch (e: Exception) {
                android.util.Log.e("RESUME_ANALYZER", "InferenceError", e)
                Result.failure(AnalysisError.InferenceError(e.message ?: "Inference failed"))
            }
        }
    }

    suspend fun loadModel() {
        withContext(Dispatchers.IO) {
            try {
                val modelPath = modelDownloader.getModelPath()
                android.util.Log.d("RESUME_ANALYZER", "Model path: $modelPath")
                val facade = PyTorchModelFacade(modelPath)
                facade.load()
                modelFacade = facade
                android.util.Log.d("RESUME_ANALYZER", "Model loaded successfully")
            } catch (e: Exception) {
                android.util.Log.e("RESUME_ANALYZER", "Failed to load model", e)
                throw e
            }
        }
    }

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
            android.util.Log.w("RESUME_ANALYZER", "Failed to load recommendations.json", e)
        }

        val metaJson = context.assets.open("ml/model_metadata.json")
            .bufferedReader().readText()
        val metadata: MetadataDto = json.decodeFromString(metaJson)
        tags = metadata.labels
        thresholds = Thresholds(
            critical = metadata.threshold_high,
            warning = metadata.threshold_low
        )
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