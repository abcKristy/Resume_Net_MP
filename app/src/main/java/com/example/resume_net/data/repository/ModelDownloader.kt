package com.example.resume_net.data.repository

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File

class ModelDownloader(
    private val context: Context,
    private val httpClient: HttpClient
) {
    companion object {
        private const val MODEL_URL = "https://your-server.com/resume_model.pt"
        private const val MODEL_FILENAME = "resume_model.pt"
    }

    suspend fun getModelPath(): String {
        val modelFile = File(context.cacheDir, MODEL_FILENAME)

        if (!modelFile.exists()) {
            downloadModel(modelFile)
        }

        return modelFile.absolutePath
    }

    private suspend fun downloadModel(modelFile: File) {
        val response = httpClient.get(MODEL_URL)
        val channel = response.bodyAsChannel()
        val inputStream = channel.toInputStream()

        modelFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        inputStream.close()
    }
}