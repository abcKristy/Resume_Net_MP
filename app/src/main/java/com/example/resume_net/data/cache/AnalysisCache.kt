package com.example.resume_net.data.cache

import com.example.resume_net.data.db.AnalysisDao
import com.example.resume_net.data.db.AnalysisEntity
import com.example.resume_net.domain.model.AnalysisIssue
import com.example.resume_net.domain.model.AnalysisResult
import com.example.resume_net.domain.model.IssueSeverity
import com.example.resume_net.domain.model.ResumeTag
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AnalysisCache(
    private val dao: AnalysisDao
) {

    companion object {
        private const val CACHE_TTL_DAYS = 30L // Время жизни кэша 30 дней
    }

    /**
     * Сохранение результата анализа в кэш
     * @param textHash хеш текста резюме
     * @param resumeText оригинальный текст
     * @param result результат анализа
     */
    suspend fun saveResult(textHash: String, resumeText: String, result: AnalysisResult) {
        val dto = CachedResultDto.fromDomain(result)
        val json = Json.encodeToString(dto)
        dao.insert(
            AnalysisEntity(
                resumeText = resumeText,
                resumeTextHash = textHash,  // ← НОВОЕ поле (нужно добавить в AnalysisEntity)
                score = result.score,
                issuesJson = json,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    /**
     * Получение результата из кэша по хешу текста
     * @param textHash хеш текста резюме
     * @return результат анализа или null если не найден или устарел
     */
    suspend fun getByHash(textHash: String): AnalysisResult? {
        val entity = dao.findByHash(textHash) ?: return null

        // Проверка на устаревание кэша (опционально)
        val now = Clock.System.now().toEpochMilliseconds()
        val cacheAge = now - entity.createdAt
        val maxAge = CACHE_TTL_DAYS * 24 * 60 * 60 * 1000L

        if (cacheAge > maxAge) {
            // Кэш устарел, удаляем
            dao.deleteById(entity.id)
            return null
        }

        val dto: CachedResultDto = Json.decodeFromString(entity.issuesJson)
        return dto.toDomain()
    }

    suspend fun getHistory(): List<Pair<String, AnalysisResult>> {
        return dao.getAll().map { entity ->
            val dto: CachedResultDto = Json.decodeFromString(entity.issuesJson)
            entity.resumeText to dto.toDomain()
        }
    }

    suspend fun clearHistory() {
        dao.deleteAll()
    }
}

@Serializable
data class CachedResultDto(
    val score: Float,
    val issues: List<CachedIssueDto>
) {
    fun toDomain(): AnalysisResult {
        val allTags = issues.map { it.toDomain() }
        return AnalysisResult(
            score = score,
            issues = allTags.filter { it.severity == IssueSeverity.CRITICAL },
            warnings = allTags.filter { it.severity == IssueSeverity.WARNING },
            allTags = allTags
        )
    }

    companion object {
        fun fromDomain(result: AnalysisResult): CachedResultDto {
            return CachedResultDto(
                score = result.score,
                issues = result.allTags.map { CachedIssueDto.fromDomain(it) }
            )
        }
    }
}

@Serializable
data class CachedIssueDto(
    val tag: String,
    val probability: Float,
    val severity: String,
    val recommendation: String
) {
    fun toDomain(): AnalysisIssue {
        return AnalysisIssue(
            tag = try { ResumeTag.valueOf(tag) } catch (e: IllegalArgumentException) { ResumeTag.NO_NUMBERS },
            probability = probability,
            severity = try { IssueSeverity.valueOf(severity) } catch (e: IllegalArgumentException) { IssueSeverity.OK },
            recommendation = recommendation
        )
    }

    companion object {
        fun fromDomain(issue: AnalysisIssue): CachedIssueDto {
            return CachedIssueDto(
                tag = issue.tag.name,
                probability = issue.probability,
                severity = issue.severity.name,
                recommendation = issue.recommendation
            )
        }
    }
}