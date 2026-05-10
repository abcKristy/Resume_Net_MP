package com.example.resume_net.data.repository

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class ModelDownloader(
    private val context: Context
) {
    companion object {
        private const val ASSET_PATH = "ml/resume_model_android_lite.pt"
        private const val MODEL_FILENAME = "resume_model_android_lite.pt"
    }

    suspend fun getModelPath(): String {
        val modelFile = File(context.cacheDir, MODEL_FILENAME)

        if (!modelFile.exists()) {
            copyFromAssets(modelFile)
        }

        return modelFile.absolutePath
    }

    private fun copyFromAssets(modelFile: File) {
        context.assets.open(ASSET_PATH).use { input ->
            FileOutputStream(modelFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}