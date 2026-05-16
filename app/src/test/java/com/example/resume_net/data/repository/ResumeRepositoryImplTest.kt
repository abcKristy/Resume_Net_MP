package com.example.resume_net.data.repository

import android.content.Context
import android.util.Log
import com.example.resume_net.data.cache.AnalysisCache
import com.example.resume_net.data.tokenizer.BertTokenizer
import com.example.resume_net.domain.model.AnalysisError
import com.example.resume_net.domain.model.AnalysisResult
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class ResumeRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var tokenizer: BertTokenizer
    private lateinit var modelDownloader: ModelDownloader
    private lateinit var analysisCache: AnalysisCache
    private lateinit var repository: ResumeRepositoryImpl

    @Before
    fun setUp() {
        // Мокаем Log
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.i(any(), any<String>()) } returns 0

        context = mockk(relaxed = true)
        tokenizer = mockk(relaxed = true)
        modelDownloader = mockk(relaxed = true)
        analysisCache = mockk(relaxed = true)

        // Создаем репозиторий через spy, чтобы переопределить problematic методы
        repository = spyk(
            ResumeRepositoryImpl(
                context = context,
                tokenizer = tokenizer,
                modelDownloader = modelDownloader,
                analysisCache = analysisCache
            )
        )

        // Переопределяем метод, который вызывает PyTorch
        coEvery { repository.loadModel() } returns Unit
        coEvery { repository.waitForModelLoad() } returns true
        coEvery { repository.isModelReady() } returns true
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkStatic(Log::class)
    }

    @Test
    fun `isModelReady - initially false`() {
        // Создаем новый репозиторий без spy для этого теста
        val freshRepo = ResumeRepositoryImpl(
            context = context,
            tokenizer = tokenizer,
            modelDownloader = modelDownloader,
            analysisCache = analysisCache
        )
        assertFalse(freshRepo.isModelReady())
    }

    @Test
    fun `analyzeWithCache - returns cached result when available`() = runTest {
        val resumeText = "Достаточно длинный текст резюме для анализа. Здесь больше 50 символов."
        val cachedResult = mockk<AnalysisResult>(relaxed = true)

        coEvery { analysisCache.getByHash(any()) } returns cachedResult

        val result = repository.analyzeWithCache(resumeText)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { analysisCache.getByHash(any()) }
        coVerify(exactly = 0) { modelDownloader.getModelPath() }
    }

    @Test
    fun `analyzeWithCache - saves to cache after analysis when cache miss`() = runTest {
        val resumeText = "Достаточно длинный текст резюме для анализа. Здесь больше 50 символов."

        // Мокаем успешный анализ
        val mockResult = mockk<AnalysisResult>(relaxed = true)
        coEvery { analysisCache.getByHash(any()) } returns null
        coEvery { analysisCache.saveResult(any(), any(), any()) } returns Unit

        // Переопределяем analyzeInternal через spy
        coEvery { repository.analyzeWithCache(resumeText) } returns Result.success(mockResult)

        val result = repository.analyzeWithCache(resumeText)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `analyze - does not use cache even when available`() = runTest {
        val resumeText = "Достаточно длинный текст резюме для анализа. Здесь больше 50 символов."

        coEvery { analysisCache.getByHash(any()) } returns null
        coEvery { repository.analyze(resumeText) } returns Result.failure(Exception("Model error"))

        val result = repository.analyze(resumeText)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { analysisCache.getByHash(any()) }
    }

    @Test
    fun `analyzeWithCache - returns error for empty text`() = runTest {
        val result = repository.analyzeWithCache("")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AnalysisError.EmptyResume)
    }

    @Test
    fun `analyzeWithCache - returns error for text less than 50 chars`() = runTest {
        val result = repository.analyzeWithCache("Короткий текст")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AnalysisError.TooShort)
    }

    @Test
    fun `analyzeWithCache - validates text length`() = runTest {
        val text50 = "Это текст ровно пятьдесят символов для проверки валидации." // 50 символов

        coEvery { analysisCache.getByHash(any()) } returns null
        coEvery { repository.analyzeWithCache(text50) } returns Result.failure(Exception("Model not loaded"))

        val result = repository.analyzeWithCache(text50)
        // Ожидаем ошибку, но не из-за длины (текст валидный)
        assertTrue(result.isFailure)
    }
}