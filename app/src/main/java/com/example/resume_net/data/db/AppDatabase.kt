package com.example.resume_net.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AnalysisEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun analysisDao(): AnalysisDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Миграция с версии 2 на 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем колонку resume_text_hash если её нет
                database.execSQL("ALTER TABLE analysis_history ADD COLUMN resume_text_hash TEXT DEFAULT ''")
                // Создаём индекс
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_analysis_hash ON analysis_history (resume_text_hash)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "resume.db"
                )
                    .addMigrations(MIGRATION_2_3)  // ← Добавить миграцию
                    .fallbackToDestructiveMigration()  // ← Оставить как запасной вариант
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}