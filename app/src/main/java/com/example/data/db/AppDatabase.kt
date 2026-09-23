package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AdminLogDao
import com.example.data.dao.AnswerDao
import com.example.data.dao.AttemptAnswerDao
import com.example.data.dao.PracticeConfigDao
import com.example.data.dao.PracticeSessionDao
import com.example.data.dao.QuestionDao
import com.example.data.dao.SavedQuestionDao
import com.example.data.dao.TestAttemptDao
import com.example.data.dao.TestTemplateDao
import com.example.data.dao.UserDao
import com.example.data.dao.UserErrorDao
import com.example.data.dao.UserQuestionHistoryDao
import com.example.data.model.AdminLogEntity
import com.example.data.model.AnswerEntity
import com.example.data.model.AttemptAnswerEntity
import com.example.data.model.PracticeConfigEntity
import com.example.data.model.PracticeSessionEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.SavedQuestionEntity
import com.example.data.model.TestAttemptEntity
import com.example.data.model.TestTemplateEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserErrorEntity
import com.example.data.model.UserQuestionHistoryEntity

@Database(
    entities = [
        UserEntity::class,
        QuestionEntity::class,
        TestTemplateEntity::class,
        TestAttemptEntity::class,
        AnswerEntity::class,
        SavedQuestionEntity::class,
        UserErrorEntity::class,
        AdminLogEntity::class,
        PracticeSessionEntity::class,
        AttemptAnswerEntity::class,
        PracticeConfigEntity::class,
        UserQuestionHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questionDao(): QuestionDao
    abstract fun testTemplateDao(): TestTemplateDao
    abstract fun testAttemptDao(): TestAttemptDao
    abstract fun answerDao(): AnswerDao
    abstract fun savedQuestionDao(): SavedQuestionDao
    abstract fun userErrorDao(): UserErrorDao
    abstract fun adminLogDao(): AdminLogDao
    abstract fun practiceSessionDao(): PracticeSessionDao
    abstract fun attemptAnswerDao(): AttemptAnswerDao
    abstract fun practiceConfigDao(): PracticeConfigDao
    abstract fun userQuestionHistoryDao(): UserQuestionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "matematika_milliy_sertifikat.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
