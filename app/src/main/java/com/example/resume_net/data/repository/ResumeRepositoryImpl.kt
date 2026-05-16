package com.example.resume_net.data.repository

import android.content.Context
import android.util.Log
import com.example.resume_net.data.cache.AnalysisCache
import com.example.resume_net.data.ml.PyTorchModelFacade
import com.example.resume_net.data.tokenizer.BertTokenizer
import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.IssueSeverity
import com.example.resume_net.domain.model.ResumeTag
import com.example.resume_net.domain.repository.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.security.MessageDigest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json

class ResumeRepositoryImpl(
    private val context: Context,
    private val tokenizer: BertTokenizer,
    private val modelDownloader: ModelDownloader,
    private val analysisCache: AnalysisCache
) : ResumeRepository {

    companion object {
        private const val TAG = "RESUME_ANALYZER"
        private const val MODEL_LOAD_TIMEOUT_MS = 5000L  // 5 секунд таймаут
    }

    private var modelFacade: PyTorchModelFacade? = null
    private var recommendations: Map<String, String> = emptyMap()
    private var tags: List<String> = emptyList()
    private var thresholds = Thresholds(critical = 0.5f, warning = 0.3f)

    // Флаги состояния модели
    @Volatile
    private var isModelLoaded = false

    @Volatile
    private var isLoadingModel = false

    // ============= ПУБЛИЧНЫЙ МЕТОД ДЛЯ ПРОВЕРКИ =============

    /**
     * Проверка, загружена ли модель
     */
    fun isModelReady(): Boolean = isModelLoaded

    /**
     * Ожидание загрузки модели с таймаутом
     * @return true если модель загружена, false если таймаут
     */
    suspend fun waitForModelLoad(): Boolean = withContext(Dispatchers.IO) {
        if (isModelLoaded) return@withContext true

        val result = withTimeoutOrNull(MODEL_LOAD_TIMEOUT_MS) {
            while (!isModelLoaded && isLoadingModel) {
                delay(100)
            }
            isModelLoaded
        }

        return@withContext result == true
    }

    // ============= АСИНХРОННАЯ ЗАГРУЗКА МОДЕЛИ =============

    /**
     * Загрузка модели (вызывается один раз при старте или первом анализе)
     */
    suspend fun loadModel() {
        if (isModelLoaded || isLoadingModel) return

        isLoadingModel = true
        Log.d(TAG, "Starting async model loading...")

        withContext(Dispatchers.IO) {
            try {
                val modelPath = modelDownloader.getModelPath()
                Log.d(TAG, "Loading model from: $modelPath")
                val facade = PyTorchModelFacade(modelPath)
                facade.load()
                modelFacade = facade
                isModelLoaded = true
                Log.d(TAG, "Model loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load model", e)
                throw e
            } finally {
                isLoadingModel = false
            }
        }
    }

    /**
     * Принудительная перезагрузка модели (при ошибках или освобождении памяти)
     */
    suspend fun reloadModel() {
        releaseModel()
        loadModel()
    }

    /**
     * Освобождение ресурсов модели
     */
    fun releaseModel() {
        if (!isModelLoaded) return
        Log.d(TAG, "Releasing model resources")
        modelFacade?.close()
        modelFacade = null
        isModelLoaded = false
        recommendations = emptyMap()
        tags = emptyList()
        Log.d(TAG, "Model resources released")
    }

    // ============= ВНУТРЕННИЙ МЕТОД С ОЖИДАНИЕМ МОДЕЛИ =============

    private suspend fun requireModelLoaded(): PyTorchModelFacade {
        // Ждём загрузку модели с таймаутом
        val ready = waitForModelLoad()
        if (!ready) {
            throw IllegalStateException("Model not loaded within ${MODEL_LOAD_TIMEOUT_MS}ms")
        }
        return modelFacade ?: throw FileNotFoundException("Model not loaded")
    }

    // ============= ХЕШИРОВАНИЕ =============

    private fun hashText(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ============= АНАЛИЗ С КЭШИРОВАНИЕМ =============

    override suspend fun analyze(resume: String): Result<AnalysisResult> {
        return analyzeInternal(resume, useCache = false)
    }

    override suspend fun analyzeWithCache(resumeText: String): Result<AnalysisResult> {
        return analyzeInternal(resumeText, useCache = true)
    }

    private suspend fun analyzeInternal(
        resume: String,
        useCache: Boolean
    ): Result<AnalysisResult> = withContext(Dispatchers.Default) {
        val trimmed = resume.trim()

        // 1. Валидация
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(AnalysisError.EmptyResume)
        }
        if (trimmed.length < 50) {
            return@withContext Result.failure(AnalysisError.TooShort)
        }

        // 2. Проверка кэша
        if (useCache) {
            val hash = hashText(trimmed)
            val cachedResult = analysisCache.getByHash(hash)
            if (cachedResult != null) {
                Log.d(TAG, "Cache hit for hash: ${hash.take(8)}...")
                return@withContext Result.success(cachedResult)
            }
            Log.d(TAG, "Cache miss for hash: ${hash.take(8)}...")
        }

        // 3. Анализ
        try {
            // Убеждаемся, что модель загружена
            if (!isModelLoaded && !isLoadingModel) {
                loadModel()
            }

            val facade = requireModelLoaded()
            ensureTokenizerLoaded()
            loadMetadata()

            val (inputIds, attentionMask) = tokenizer.tokenize(trimmed)
            val (score, probs) = facade.predict(inputIds, attentionMask)

            val allTags = buildIssues(probs)

            val result = AnalysisResult(
                score = roundToTwoDecimals(score),
                issues = allTags.filter { it.severity == IssueSeverity.CRITICAL },
                warnings = allTags.filter { it.severity == IssueSeverity.WARNING },
                allTags = allTags
            )

            // 4. Сохранение в кэш
            if (useCache) {
                val hash = hashText(trimmed)
                analysisCache.saveResult(hash, trimmed, result)
                Log.d(TAG, "Saved to cache with hash: ${hash.take(8)}...")
            }

            Result.success(result)
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "ModelNotAvailable", e)
            Result.failure(AnalysisError.ModelNotAvailable)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "TokenizerError", e)
            Result.failure(AnalysisError.TokenizerError(e.message ?: "Tokenization failed"))
        } catch (e: Exception) {
            Log.e(TAG, "InferenceError", e)
            Result.failure(AnalysisError.InferenceError(e.message ?: "Inference failed"))
        }
    }

    // ============= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =============

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
            Log.w(TAG, "Failed to load recommendations.json", e)
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
            Log.e(TAG, "Failed to load metadata.json", e)
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

    // ============= ВНУТРЕННИЕ КЛАССЫ =============

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

private fun roundToTwoDecimals(value: Float): Float {
    return (value * 100).toInt() / 100.0f
}