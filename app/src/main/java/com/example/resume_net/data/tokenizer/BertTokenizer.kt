package com.example.resume_net.data.tokenizer

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BertTokenizer(
    private val context: Context
) {
    private var vocab: Map<String, Long> = emptyMap()
    private var maxLength: Int = 300

    companion object {
        private const val UNK_TOKEN = "[UNK]"
        private const val CLS_TOKEN = "[CLS]"
        private const val SEP_TOKEN = "[SEP]"
        private const val PAD_TOKEN = "[PAD]"
        private const val VOCAB_FILE = "ml/vocab.txt"
        private const val TOKENIZER_CONFIG = "ml/tokenizer.json"
    }

    fun load() {
        loadVocab()
        loadConfig()
    }

    private fun loadVocab() {
        val inputStream = context.assets.open(VOCAB_FILE)
        vocab = inputStream.bufferedReader().useLines { lines ->
            lines.mapIndexed { index, token ->
                token.trim() to index.toLong()
            }.toMap()
        }
    }

    private fun loadConfig() {
        val jsonString = context.assets.open(TOKENIZER_CONFIG).bufferedReader().readText()
        val config = Json.parseToJsonElement(jsonString).jsonObject
        maxLength = config["model_max_length"]?.jsonPrimitive?.content?.toIntOrNull() ?: 300
    }

    fun tokenize(text: String): Pair<LongArray, LongArray> {
        val tokens = listOf(CLS_TOKEN) + tokenizeText(text) + listOf(SEP_TOKEN)

        val tokenIds = tokens.take(maxLength).map { token ->
            vocab[token] ?: vocab[UNK_TOKEN] ?: 0L
        }

        val paddedIds = if (tokenIds.size < maxLength) {
            tokenIds + List(maxLength - tokenIds.size) { vocab[PAD_TOKEN] ?: 0L }
        } else {
            tokenIds
        }

        val attentionMask = paddedIds.mapIndexed { index, _ ->
            if (index < tokenIds.size) 1L else 0L
        }

        return Pair(paddedIds.toLongArray(), attentionMask.toLongArray())
    }

    private fun tokenizeText(text: String): List<String> {
        val cleaned = text.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s.\\-,!?;:'\"()\\[\\]{}]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val words = cleaned.split(" ")

        return words.flatMap { word ->
            if (word.isEmpty()) {
                emptyList()
            } else if (word.length <= 4) {
                listOf(word)
            } else {
                tokenizeLongWord(word)
            }
        }
    }

    private fun tokenizeLongWord(word: String): List<String> {
        val subwords = mutableListOf<String>()
        var remaining = word

        while (remaining.isNotEmpty()) {
            var found = false
            val end = minOf(remaining.length, 15)

            for (i in end downTo 0) {
                val candidate = if (i == remaining.length) {
                    remaining
                } else {
                    "##${remaining.substring(i)}"
                }

                if (candidate in vocab) {
                    subwords.add(candidate)
                    remaining = remaining.substring(0, i)
                    found = true
                    break
                }
            }

            if (!found) {
                if (remaining.length == 1) {
                    subwords.add(remaining)
                } else {
                    subwords.add("##${remaining.substring(remaining.length - 1)}")
                    remaining = remaining.substring(0, remaining.length - 1)
                }
            }
        }

        return subwords.reversed()
    }
}