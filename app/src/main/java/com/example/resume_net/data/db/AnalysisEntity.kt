package com.example.resume_net.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analysis_history",
    indices = [
        Index(value = ["resume_text_hash"], name = "idx_analysis_hash")
    ]
)
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "resume_text")
    val resumeText: String,

    @ColumnInfo(name = "resume_text_hash", defaultValue = "")
    val resumeTextHash: String? = null,

    @ColumnInfo(name = "score")
    val score: Float,

    @ColumnInfo(name = "issues_json")
    val issuesJson: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)