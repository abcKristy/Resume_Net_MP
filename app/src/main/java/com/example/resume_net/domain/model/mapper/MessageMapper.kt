package com.example.resume_net.domain.model.mapper

import com.example.resume_net.data.db.MessageEntity
import com.example.resume_net.data.db.MessageRole
import com.example.resume_net.data.db.TagProbability
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.ChatMessage
import com.example.resume_net.domain.model.IssueSeverity
import com.example.resume_net.domain.model.ResumeTag
import kotlinx.serialization.json.Json

/**
 * Маппер для конвертации между Entity (Room) и Domain моделью ChatMessage
 */
object MessageMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Конвертация Entity -> Domain (UserMessage)
     */
    fun toUserMessage(entity: MessageEntity): ChatMessage.UserMessage {
        require(entity.role == MessageRole.USER) {
            "Cannot convert ${entity.role} message to UserMessage"
        }

        return ChatMessage.UserMessage(
            id = entity.id,
            conversationId = entity.conversationId,
            text = entity.content,
            timestamp = entity.createdAt,
            isFavorite = entity.isFavorite
        )
    }

    /**
     * Конвертация Entity -> Domain (AssistantMessage)
     */
    fun toAssistantMessage(entity: MessageEntity): ChatMessage.AssistantMessage {
        require(entity.role == MessageRole.ASSISTANT) {
            "Cannot convert ${entity.role} message to AssistantMessage"
        }

        val analysisResult = parseAnalysisResult(
            score = entity.score ?: 0f,
            tagsJson = entity.tagsJson
        )

        return ChatMessage.AssistantMessage(
            id = entity.id,
            conversationId = entity.conversationId,
            analysisResult = analysisResult,
            timestamp = entity.createdAt,
            isFavorite = entity.isFavorite
        )
    }

    /**
     * Конвертация Domain -> Entity (UserMessage)
     */
    fun toUserEntity(
        conversationId: Long,
        message: ChatMessage.UserMessage
    ): MessageEntity {
        return MessageEntity(
            id = message.id,
            conversationId = conversationId,
            role = MessageRole.USER,
            content = message.text,
            score = null,
            tagsJson = null,
            createdAt = message.timestamp,
            isFavorite = message.isFavorite
        )
    }

    /**
     * Конвертация Domain -> Entity (AssistantMessage)
     */
    fun toAssistantEntity(
        conversationId: Long,
        message: ChatMessage.AssistantMessage
    ): MessageEntity {
        val tagsJson = serializeAnalysisResult(message.analysisResult)

        return MessageEntity(
            id = message.id,
            conversationId = conversationId,
            role = MessageRole.ASSISTANT,
            content = generateAssistantContent(message.analysisResult),
            score = message.analysisResult.score,
            tagsJson = tagsJson,
            createdAt = message.timestamp,
            isFavorite = message.isFavorite
        )
    }

    /**
     * Создание нового UserMessage (без ID, для вставки в БД)
     */
    fun newUserMessage(
        conversationId: Long,
        text: String
    ): ChatMessage.UserMessage {
        return ChatMessage.UserMessage(
            id = 0,  // БД сгенерирует ID
            conversationId = conversationId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    }

    /**
     * Создание нового AssistantMessage (без ID, для вставки в БД)
     */
    fun newAssistantMessage(
        conversationId: Long,
        analysisResult: AnalysisResult
    ): ChatMessage.AssistantMessage {
        return ChatMessage.AssistantMessage(
            id = 0,  // БД сгенерирует ID
            conversationId = conversationId,
            analysisResult = analysisResult,
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    }

    /**
     * Парсинг JSON строки тегов в список AnalysisIssue
     */
    private fun parseAnalysisResult(
        score: Float,
        tagsJson: String?
    ): AnalysisResult {
        if (tagsJson.isNullOrEmpty()) {
            return AnalysisResult(
                score = score,
                issues = emptyList(),
                warnings = emptyList(),
                allTags = emptyList()
            )
        }

        return try {
            val tagProbabilities: List<TagProbability> = json.decodeFromString(tagsJson)
            val allTags = tagProbabilities.map { tagProb ->
                AnalysisIssue(
                    tag = try {
                        ResumeTag.valueOf(tagProb.tagName.uppercase())
                    } catch (e: IllegalArgumentException) {
                        ResumeTag.NO_NUMBERS
                    },
                    probability = tagProb.probability,
                    severity = try {
                        IssueSeverity.valueOf(tagProb.severity.uppercase())
                    } catch (e: IllegalArgumentException) {
                        IssueSeverity.OK
                    },
                    recommendation = tagProb.recommendation
                )
            }

            AnalysisResult(
                score = score,
                issues = allTags.filter { it.severity == IssueSeverity.CRITICAL },
                warnings = allTags.filter { it.severity == IssueSeverity.WARNING },
                allTags = allTags
            )
        } catch (e: Exception) {
            // В случае ошибки парсинга возвращаем пустой результат
            AnalysisResult(
                score = score,
                issues = emptyList(),
                warnings = emptyList(),
                allTags = emptyList()
            )
        }
    }

    /**
     * Сериализация AnalysisResult в JSON строку
     */
    private fun serializeAnalysisResult(result: AnalysisResult): String {
        val tagProbabilities = result.allTags.map { issue ->
            TagProbability(
                tagName = issue.tag.name,
                probability = issue.probability,
                severity = issue.severity.name,
                recommendation = issue.recommendation
            )
        }
        return json.encodeToString(tagProbabilities)
    }

    /**
     * Генерация текстового представления анализа для поля content
     * (используется для отображения в списке диалогов)
     */
    private fun generateAssistantContent(result: AnalysisResult): String {
        return buildString {
            append("⭐ Оценка: ${result.score}/5.0\n\n")
            append("📊 Проблемы:\n")
            result.issues.take(3).forEach { issue ->
                append("• ${issue.tag.displayName} (${(issue.probability * 100).toInt()}%)\n")
            }
            if (result.issues.size > 3) {
                append("• и ещё ${result.issues.size - 3} проблем...\n")
            }
        }
    }
}