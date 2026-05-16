package com.example.resume_net.data.tokenizer

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json

class BertTokenizer(
    private val context: Context
) {
    private var vocab: MutableMap<String, Int> = mutableMapOf()
    private var maxLength: Int = 300

    companion object {
        private const val UNK_TOKEN = "[UNK]"
        private const val CLS_TOKEN = "[CLS]"
        private const val SEP_TOKEN = "[SEP]"
        private const val PAD_TOKEN = "[PAD]"
        private const val MASK_TOKEN = "[MASK]"
        private const val VOCAB_FILE = "ml/vocab.txt"  // Или vocab.json
    }

    /**
     * Загрузка словаря BERT
     * Поддерживает как .txt (построчный), так и .json форматы
     */
    fun load() {
        try {
            // Пробуем загрузить JSON
            val jsonString = context.assets.open(VOCAB_FILE.replace(".txt", ".json"))
                .bufferedReader().use { it.readText() }

            val jsonVocab = Json.decodeFromString<Map<String, Int>>(jsonString)
            vocab.putAll(jsonVocab)
            Log.d("BERT", "Loaded JSON vocab with ${vocab.size} tokens")
        } catch (e: Exception) {
            // Fallback на .txt формат (как в оригинальном BERT)
            try {
                val inputStream = context.assets.open(VOCAB_FILE)
                vocab = inputStream.bufferedReader().useLines { lines ->
                    lines.mapIndexed { index, token ->
                        token.trim() to index
                    }.toMap().toMutableMap()
                }
                Log.d("BERT", "Loaded TXT vocab with ${vocab.size} tokens")
            } catch (e2: Exception) {
                Log.e("BERT", "Failed to load vocab", e2)
                throw e2
            }
        }
    }

    /**
     * WordPiece токенизация одного слова
     * @return список субтокенов (например, "playing" -> ["play", "##ing"])
     */
    private fun wordPieceTokenize(word: String): List<String> {
        if (word.isEmpty()) return emptyList()

        // Если слово целиком в словаре
        if (vocab.containsKey(word)) {
            return listOf(word)
        }

        // WordPiece алгоритм: пытаемся разбить на максимальные куски
        val tokens = mutableListOf<String>()
        var remaining = word
        var start = 0

        while (start < remaining.length) {
            var end = remaining.length
            var foundToken: String? = null

            // Ищем самый длинный префикс, который есть в словаре
            while (end > start) {
                val candidate = if (start == 0) {
                    remaining.substring(start, end)
                } else {
                    "##${remaining.substring(start, end)}"
                }

                if (vocab.containsKey(candidate)) {
                    foundToken = candidate
                    break
                }
                end--
            }

            if (foundToken != null) {
                tokens.add(foundToken)
                start = end
            } else {
                // Символ не найден в словаре
                tokens.add(UNK_TOKEN)
                start++
            }
        }

        return tokens
    }

    /**
     * Полная токенизация текста
     */
    fun tokenize(text: String): Pair<LongArray, LongArray> {
        // 1. Preprocessing - BasicTokenizer (как в оригинальном BERT)
        val cleaned = cleanText(text)
        val words = cleaned.split(" ")

        // 2. WordPiece токенизация каждого слова
        val subTokens = mutableListOf<String>()
        subTokens.add(CLS_TOKEN)

        for (word in words) {
            if (word.isEmpty()) continue

            val wordTokens = if (vocab.containsKey(word)) {
                listOf(word)
            } else {
                wordPieceTokenize(word)
            }
            subTokens.addAll(wordTokens)
        }
        subTokens.add(SEP_TOKEN)

        // 3. Truncation (обрезание до maxLength - 2 для [CLS] и [SEP])
        val maxTokens = maxLength - 2
        val truncatedTokens = if (subTokens.size > maxLength) {
            subTokens.subList(0, maxLength)
        } else {
            subTokens
        }

        // 4. Конвертация в ID
        val inputIds = truncatedTokens.map { token ->
            vocab[token] ?: vocab[UNK_TOKEN] ?: error("Missing UNK token in vocab")
        }.toMutableList()

        // 5. Padding
        while (inputIds.size < maxLength) {
            inputIds.add(vocab[PAD_TOKEN] ?: 0)
        }

        // 6. Attention mask (1 для реальных токенов, 0 для padding)
        val attentionMask = LongArray(maxLength) { index ->
            if (index < truncatedTokens.size) 1L else 0L
        }

        return Pair(inputIds.map { it.toLong() }.toLongArray(), attentionMask)
    }

    /**
     * Очистка текста как в оригинальном BERT
     */
    private fun cleanText(text: String): String {
        var cleaned = text.lowercase()

        // Замена кириллических букв на латиницу (если нужно)
        cleaned = transliterateIfNeeded(cleaned)

        // Нормализация пробелов
        cleaned = cleaned.replace(Regex("\\s+"), " ")

        // Удаление лишних символов (можно настроить под русский язык)
        cleaned = cleaned.replace(Regex("[^\\p{L}\\p{N}\\s\\-]"), " ")

        // Обрезка лишних пробелов
        cleaned = cleaned.trim()

        return cleaned
    }

    /**
     * Опциональная транслитерация для русско-английских моделей
     */
    private fun transliterateIfNeeded(text: String): String {
        // Если модель только для английского, можно транслитерировать русский
        // Но BERT мультиязычный обычно понимает кириллицу
        return text
    }

    /**
     * Вспомогательный метод для отладки
     */
    fun debugTokenize(text: String): String {
        val (ids, mask) = tokenize(text)
        val tokens = ids.map { id ->
            vocab.entries.find { it.value == id.toInt() }?.key ?: "???"
        }
        return """
            Text: $text
            Tokens: ${tokens.joinToString(" ")}
            IDs: ${ids.joinToString(" ")}
            Mask: ${mask.joinToString(" ")}
        """.trimIndent()
    }
}