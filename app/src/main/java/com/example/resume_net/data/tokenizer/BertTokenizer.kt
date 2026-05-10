package com.example.resume_net.data.tokenizer

import android.content.Context

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
    }

    fun load() {
        val inputStream = context.assets.open(VOCAB_FILE)
        vocab = inputStream.bufferedReader().useLines { lines ->
            lines.mapIndexed { index, token ->
                token.trim() to index.toLong()
            }.toMap()
        }
    }

    fun tokenize(text: String): Pair<LongArray, LongArray> {
        val cleaned = text.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val words = cleaned.split(" ")
        val tokens = mutableListOf<String>()
        tokens.add(CLS_TOKEN)

        for (word in words) {
            if (word.isEmpty()) continue
            val token = vocab[word]
            if (token != null) {
                tokens.add(word)
            } else {
                // Разбиваем на символы с ## префиксом
                word.forEach { char ->
                    tokens.add("##$char")
                }
            }
        }

        tokens.add(SEP_TOKEN)

        val tokenIds = tokens.take(maxLength).map { token ->
            vocab[token] ?: vocab[UNK_TOKEN] ?: 0L
        }

        val paddedIds = if (tokenIds.size < maxLength) {
            tokenIds + List(maxLength - tokenIds.size) { vocab[PAD_TOKEN] ?: 0L }
        } else {
            tokenIds
        }

        val attentionMask = LongArray(maxLength) { index ->
            if (index < tokens.size) 1L else 0L
        }

        return Pair(paddedIds.toLongArray(), attentionMask)
    }
}