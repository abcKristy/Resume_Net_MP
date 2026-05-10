package com.example.resume_net.data.ml

import android.util.Log
import com.example.resume_net.data.mapper.IValueMapper
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

/**
 * Фасад для работы с PyTorch моделью на Android.
 *
 * Отвечает за:
 * 1. Загрузку модели из файла
 * 2. Инференс (токенизация — снаружи)
 * 3. Маппинг результатов через IValueMapper
 */
class PyTorchModelFacade(
    private val modelPath: String
) {
    companion object {
        private const val TAG = "RESUME_ANALYZER"
        private const val MAX_LENGTH = 300L
    }

    private var module: Module? = null

    /**
     * Загружает модель из файла.
     * Вызвать один раз при старте приложения.
     */
    fun load() {
        Log.d(TAG, "Loading model from: $modelPath")
        module = Module.load(modelPath)
        Log.d(TAG, "Model loaded successfully")
    }

    /**
     * Выполняет инференс.
     *
     * @param inputIds LongArray размером 300 (токены)
     * @param attentionMask LongArray размером 300 (маска внимания)
     * @return Pair(оценка 1-5, вероятности 20 тегов)
     */
    fun predict(inputIds: LongArray, attentionMask: LongArray): Pair<Float, FloatArray> {
        val currentModule = module
            ?: throw IllegalStateException("Model not loaded. Call load() first.")

        // Создание входных тензоров (Long = Int64)
        val inputTensor = Tensor.fromBlob(inputIds, longArrayOf(1, MAX_LENGTH))
        val maskTensor = Tensor.fromBlob(attentionMask, longArrayOf(1, MAX_LENGTH))

        // Инференс
        val output = currentModule.forward(
            IValue.from(inputTensor),
            IValue.from(maskTensor)
        )

        // Маппинг результата (Tuple -> Pair)
        val (score, probs) = IValueMapper.toResult(output)

        Log.d(TAG, "Prediction: score=$score, probs_sum=${probs.sum()}")

        return Pair(score, probs)
    }

    /**
     * Освобождает ресурсы модели.
     */
    fun close() {
        module?.destroy()
        module = null
        Log.d(TAG, "Model resources released")
    }
}