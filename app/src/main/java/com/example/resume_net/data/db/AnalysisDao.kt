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

    // НОВЫЙ метод для поиска по хешу
    @Query("SELECT * FROM analysis_history WHERE resume_text_hash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): AnalysisEntity?

    // НОВЫЙ метод для удаления по ID (для очистки устаревшего кэша)
    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}