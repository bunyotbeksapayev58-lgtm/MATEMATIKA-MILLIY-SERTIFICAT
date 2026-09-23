package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val phone: String = "",
    val firstName: String,
    val lastName: String,
    val passwordHash: String,
    val role: String = "USER", // "USER" or "ADMIN"
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["module", "topic"]),
        Index(value = ["difficulty"]),
        Index(value = ["status"]),
        Index(value = ["bookSource"]),
        Index(value = ["questionHash"]),
        Index(value = ["isArchived"])
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customQuestionId: String = "", // e.g. "math-hard-000001" from JSON import
    val subject: String = "Matematika",
    val module: String = "MATHEMATICS", // "MATHEMATICS" or "GEOMETRY"
    val bookSource: String = "Puza Geometriya 1", // "Puza Geometriya 1", "Puza Geometriya 2", "Puza Geometriya 1 + Puza Geometriya 2", "Umumiy Matematika"
    val topic: String,
    val subtopic: String = "",
    val difficulty: String, // "EASY", "MEDIUM", "HARD", "VERY_HARD"
    val questionText: String,
    val diagram: String = "", // Geometric diagram descriptor
    val formulaUsed: String = "", // Formula or geometric rule
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String = "",
    val solutionSteps: String = "",
    val estimatedTimeSeconds: Int = 120,
    val tags: String = "",
    val sourceType: String = "PRACTICE", // "AI_GENERATED", "ADMIN_ADDED", "PRACTICE", "TEST", "ADMIN_JSON_IMPORT"
    val status: String = "PUBLISHED", // "PUBLISHED", "APPROVED", "DRAFT", "PENDING_REVIEW", "ARCHIVED"
    val isArchived: Boolean = false,
    val questionHash: String = "", // Canonical SHA-256 / MD5 hash for duplicate detection
    val timesAttempted: Int = 0,
    val timesCorrect: Int = 0,
    val totalTimeSpentSeconds: Long = 0,
    val averageTimeSpentSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_templates")
data class TestTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val module: String = "MATHEMATICS",
    val topic: String = "Barcha mavzular",
    val questionCount: Int = 45,
    val durationMinutes: Int = 150,
    val difficulty: String = "ALL", // "ALL", "EASY", "MEDIUM", "HARD"
    val isNationalCertMode: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "test_attempts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["state"])
    ]
)
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val testTitle: String,
    val module: String = "MATHEMATICS",
    val totalQuestions: Int = 45,
    val state: String = "IN_PROGRESS", // "IN_PROGRESS", "COMPLETED"
    val score: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unansweredCount: Int = 0,
    val percentage: Float = 0f,
    val timeSpentSeconds: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val topicBreakdownJson: String = "{}",
    val weakTopicsRecommendation: String = ""
)

@Entity(
    tableName = "answers",
    indices = [
        Index(value = ["attemptId", "questionId"], unique = true)
    ]
)
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val attemptId: Long,
    val questionId: Long,
    val selectedOption: String?, // "A", "B", "C", "D" or null
    val isCorrect: Boolean = false,
    val timeSpentSeconds: Int = 0
)

@Entity(
    tableName = "practice_sessions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["startedAt"])
    ]
)
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val module: String = "MATHEMATICS",
    val topic: String = "Barcha mavzular",
    val difficulty: String = "ALL", // "ALL", "EASY", "MEDIUM", "HARD", "VERY_HARD"
    val questionCount: Int = 20,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val status: String = "IN_PROGRESS", // "NOT_STARTED", "IN_PROGRESS", "PAUSED", "COMPLETED", "ABANDONED"
    val totalActiveTimeSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val pausedAt: Long? = null,
    val resumedAt: Long? = null,
    val pauseDurationSeconds: Int = 0,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val percentage: Float = 0f,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unansweredCount: Int = 0,
    val averageTimePerQuestionSeconds: Float = 0f,
    val fastestQuestionSeconds: Int = 0,
    val slowestQuestionSeconds: Int = 0,
    val certificateGrade: String = "",
    val aiRecommendation: String = "",
    val bookSource: String = "ALL" // "ALL", "Puza Geometriya 1", "Puza Geometriya 2", "Puza Geometriya 1 + Puza Geometriya 2"
)

@Entity(
    tableName = "attempt_answers",
    indices = [
        Index(value = ["sessionId", "questionId"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["questionId"])
    ]
)
data class AttemptAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val userId: Long,
    val questionId: Long,
    val questionNumber: Int,
    val selectedAnswer: String?, // "A", "B", "C", "D" or null
    val isCorrect: Boolean = false,
    val questionStartedAt: Long = System.currentTimeMillis(),
    val questionAnsweredAt: Long? = null,
    val timeSpentSeconds: Int = 0
)

@Entity(
    tableName = "saved_questions",
    indices = [
        Index(value = ["userId", "questionId"], unique = true)
    ]
)
data class SavedQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val questionId: Long,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_errors",
    indices = [
        Index(value = ["userId", "questionId"])
    ]
)
data class UserErrorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val questionId: Long,
    val attemptId: Long = 0,
    val selectedOption: String,
    val correctOption: String,
    val resolved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "admin_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["action"])
    ]
)
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val adminUserId: Long = 0,
    val adminUsername: String = "admin",
    val action: String, // "USER_BLOCK", "USER_DELETE", "QUESTION_CREATE", "QUESTION_EDIT", "QUESTION_DELETE", "TEST_CREATE", "CONFIG_CHANGE"
    val targetType: String = "", // "USER", "QUESTION", "TEST", "SETTINGS"
    val targetId: String = "",
    val details: String = "",
    val ipAddress: String = "127.0.0.1",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "practice_configs")
data class PracticeConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val availableDifficultiesJson: String = "[\"EASY\",\"MEDIUM\",\"HARD\",\"VERY_HARD\"]",
    val availableCountsJson: String = "[5,10,15,20,30,45,50,100]",
    val mockTestDurationMinutes: Int = 150,
    val allowCustomCount: Boolean = true,
    val minCustomCount: Int = 1,
    val maxCustomCount: Int = 100
)

@Entity(
    tableName = "user_question_history",
    indices = [
        Index(value = ["userId", "questionId"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["questionId"]),
        Index(value = ["sessionId"]),
        Index(value = ["answeredAt"])
    ]
)
data class UserQuestionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val questionId: Long,
    val sessionId: Long = 0,
    val answeredAt: Long = System.currentTimeMillis(),
    val isCorrect: Boolean = false,
    val timeSpentSeconds: Int = 0
)

