package com.example.resume_net.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_history")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "resume_text")
    val resumeText: String,

    @ColumnInfo(name = "score")
    val score: Float,

    @ColumnInfo(name = "issues_json")
    val issuesJson: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)