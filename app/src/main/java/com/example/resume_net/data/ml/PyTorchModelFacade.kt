package com.example.resume_net.data.ml

import android.util.Log
import com.example.resume_net.data.mapper.IValueMapper
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

class PyTorchModelFacade(
    private val modelPath: String
) {
    companion object {
        private const val TAG = "PyTorchModelFacade"
        private const val MAX_LENGTH = 300L
    }

    private var module: Module? = null
    private var isClosed = false

    fun load() {
        check(!isClosed) { "Model already closed" }
        Log.d(TAG, "Loading model from: $modelPath")
        module = Module.load(modelPath)
        Log.d(TAG, "Model loaded successfully")
    }

    fun predict(inputIds: LongArray, attentionMask: LongArray): Pair<Float, FloatArray> {
        check(!isClosed) { "Model is closed" }
        val currentModule = module
            ?: throw IllegalStateException("Model not loaded. Call load() first.")

        val inputTensor = Tensor.fromBlob(inputIds, longArrayOf(1, MAX_LENGTH))
        val maskTensor = Tensor.fromBlob(attentionMask, longArrayOf(1, MAX_LENGTH))

        val output = currentModule.forward(
            IValue.from(inputTensor),
            IValue.from(maskTensor)
        )

        val (score, probs) = IValueMapper.toResult(output)

        Log.d(TAG, "Prediction: score=$score, probs_sum=${probs.sum()}")

        return Pair(score, probs)
    }

    fun close() {
        if (isClosed) return
        Log.d(TAG, "Closing model and releasing resources")
        module?.destroy()
        module = null
        isClosed = true
        Log.d(TAG, "Model resources released")
    }
}