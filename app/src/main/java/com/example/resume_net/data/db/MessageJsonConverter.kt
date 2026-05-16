package com.example.resume_net.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Конвертер для сохранения тегов в JSON формате
 */
class MessageJsonConverter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromTagsJson(tags: List<TagProbability>?): String? {
        return tags?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toTagsJson(jsonString: String?): List<TagProbability>? {
        return jsonString?.let { json.decodeFromString(it) }
    }
}

/**
 * DTO для сохранения тегов и вероятностей
 */
@Serializable
data class TagProbability(
    val tagName: String,
    val probability: Float,
    val severity: String,  // CRITICAL, WARNING, OK
    val recommendation: String
)