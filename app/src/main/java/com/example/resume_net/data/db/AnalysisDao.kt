package com.example.resume_net.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analysis_history ORDER BY created_at DESC")
    suspend fun getAll(): List<AnalysisEntity>

    @Insert
    suspend fun insert(entity: AnalysisEntity)

    @Query("DELETE FROM analysis_history")
    suspend fun deleteAll()
}