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
    suspend fun saveResult(resumeText: String, result: AnalysisResult) {
        val dto = CachedResultDto.fromDomain(result)
        val json = Json.encodeToString(dto)
        dao.insert(
            AnalysisEntity(
                resumeText = resumeText,
                score = result.score,
                issuesJson = json,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
        )
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