package com.example.resume_net.data.repository

import android.content.Context
import com.example.resume_net.data.mapper.toProbs
import com.example.resume_net.data.mapper.toScore
import com.example.resume_net.data.mapper.toTensor
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
import org.pytorch.IValue
import org.pytorch.Module
import java.io.File
import java.io.FileNotFoundException

class ResumeRepositoryImpl(
    private val context: Context,
    private val tokenizer: BertTokenizer,
    private val modelDownloader: ModelDownloader
) : ResumeRepository {

    private var module: Module? = null
    private var recommendations: Map<String, String> = emptyMap()
    private var thresholds = Thresholds(critical = 0.5f, warning = 0.3f)

    override suspend fun analyze(resumeText: String): Result<AnalysisResult> {
        return withContext(Dispatchers.Default) {
            try {
                ensureModelLoaded()
                ensureTokenizerLoaded()
                loadRecommendations()
                loadThresholds()

                val (inputIds, attentionMask) = tokenizer.tokenize(resumeText)
                val inputTensor = inputIds.toTensor()
                val maskTensor = attentionMask.toTensor()

                val result = module!!.forward(
                    IValue.from(inputTensor),
                    IValue.from(maskTensor)
                )

                val score = result.toScore()
                val probs = result.toProbs()
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
                Result.failure(AnalysisError.ModelNotAvailable)
            } catch (e: IllegalStateException) {
                Result.failure(AnalysisError.TokenizerError(e.message ?: "Tokenization failed"))
            } catch (e: Exception) {
                Result.failure(AnalysisError.InferenceError(e.message ?: "Inference failed"))
            }
        }
    }

    suspend fun loadModel() {
        withContext(Dispatchers.IO) {
            val modelPath = modelDownloader.getModelPath()
            module = Module.load(modelPath)
        }
    }

    private fun ensureModelLoaded() {
        if (module == null) {
            throw FileNotFoundException("Model not loaded. Call loadModel() first.")
        }
    }

    private fun ensureTokenizerLoaded() {
        tokenizer.load()
    }

    private fun loadRecommendations() {
        if (recommendations.isNotEmpty()) return
        val json = context.assets.open("ml/recommendations.json")
            .bufferedReader()
            .readText()
        recommendations = Json.decodeFromString(json)
    }

    private fun loadThresholds() {
        val json = context.assets.open("ml/model_metadata.json")
            .bufferedReader()
            .readText()
        val metadata = Json.decodeFromString<MetadataDto>(json)
        thresholds = Thresholds(
            critical = metadata.thresholds.critical,
            warning = metadata.thresholds.warning
        )
    }

    private fun buildIssues(probs: FloatArray): List<AnalysisIssue> {
        val tags = loadTags()
        return probs.mapIndexed { index, prob ->
            val tagName = tags.getOrElse(index) { "unknown_$index" }
            val resumeTag = try {
                ResumeTag.valueOf(tagName.uppercase())
            } catch (e: IllegalArgumentException) {
                ResumeTag.CONTACT_INFO
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

    private fun loadTags(): List<String> {
        val json = context.assets.open("ml/model_metadata.json")
            .bufferedReader()
            .readText()
        return Json.decodeFromString<MetadataDto>(json).tags
    }

    data class Thresholds(
        val critical: Float,
        val warning: Float
    )

    @kotlinx.serialization.Serializable
    data class MetadataDto(
        val tags: List<String>,
        val thresholds: ThresholdsDto,
        val max_length: Int
    )

    @kotlinx.serialization.Serializable
    data class ThresholdsDto(
        val critical: Float,
        val warning: Float
    )
}