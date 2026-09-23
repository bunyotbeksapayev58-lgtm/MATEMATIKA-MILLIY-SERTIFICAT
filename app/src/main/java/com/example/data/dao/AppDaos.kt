package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdminLogEntity
import com.example.data.model.AnswerEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.SavedQuestionEntity
import com.example.data.model.TestAttemptEntity
import com.example.data.model.TestTemplateEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserErrorEntity
import com.example.data.model.UserQuestionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE (username = :identifier OR email = :identifier) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    fun countUsers(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE isBlocked = 0")
    fun countActiveUsers(): Flow<Int>

    @Query("UPDATE users SET isBlocked = CASE WHEN isBlocked = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleBlockUser(id: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE module = :module AND status = 'APPROVED' ORDER BY id ASC")
    fun getQuestionsByModule(module: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE module = :module AND status = 'APPROVED' ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getQuestionsPaged(module: String, limit: Int, offset: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE module = :module AND difficulty = :difficulty AND status = 'APPROVED' ORDER BY id ASC")
    fun getQuestionsByDifficulty(module: String, difficulty: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE module = :module AND topic = :topic AND status = 'APPROVED' ORDER BY id ASC")
    fun getQuestionsByTopic(module: String, topic: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE module = :module AND status = 'APPROVED' ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestions(module: String, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE module = :module AND difficulty = :difficulty AND status = 'APPROVED' ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestionsByDifficulty(module: String, difficulty: String, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE module = :module AND topic = :topic AND status = 'APPROVED' ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestionsByTopic(module: String, topic: String, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE module = :module AND difficulty = :difficulty AND topic = :topic AND status = 'APPROVED' ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestionsByDifficultyAndTopic(module: String, difficulty: String, topic: String, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE module = 'GEOMETRY' AND (:bookSource = 'ALL' OR bookSource = :bookSource) AND (:difficulty = 'ALL' OR difficulty = :difficulty) AND (:topic = 'Barcha mavzular' OR topic = :topic) AND status = 'APPROVED' ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomGeometryQuestions(bookSource: String, difficulty: String, topic: String, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE module = 'GEOMETRY' AND (:bookSource = 'ALL' OR bookSource = :bookSource) AND status = 'APPROVED' ORDER BY id ASC")
    fun getGeometryQuestionsByBookSource(bookSource: String): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE module = 'GEOMETRY' AND status = 'APPROVED'")
    fun countApprovedGeometryQuestions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE module = 'GEOMETRY' AND bookSource = :bookSource AND status = 'APPROVED'")
    fun countGeometryByBookSource(bookSource: String): Flow<Int>

    @Query("SELECT DISTINCT topic FROM questions WHERE module = 'GEOMETRY' AND status = 'APPROVED'")
    fun getGeometryTopics(): Flow<List<String>>

    @Query("SELECT DISTINCT bookSource FROM questions WHERE module = 'GEOMETRY' AND status = 'APPROVED'")
    fun getGeometryBookSources(): Flow<List<String>>

    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestionsAdmin(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE module = :module ORDER BY id DESC")
    fun getQuestionsByModuleAdmin(module: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE (questionText LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%') ORDER BY id DESC")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE status = 'APPROVED'")
    fun countApprovedQuestions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE module = :module AND status = 'APPROVED'")
    fun countQuestionsByModule(module: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE module = :module AND difficulty = :difficulty AND status = 'APPROVED'")
    fun countQuestionsByDifficulty(module: String, difficulty: String): Flow<Int>

    @Query("SELECT DISTINCT topic FROM questions WHERE module = :module AND status IN ('APPROVED', 'PUBLISHED') AND isArchived = 0")
    fun getTopicsByModule(module: String): Flow<List<String>>

    // Unworked question selection enforcing UNIQUE user-question history
    @Query("""
        SELECT * FROM questions 
        WHERE module = :module 
        AND (:difficulty = 'ALL' OR difficulty = :difficulty)
        AND (:topic = 'Barcha mavzular' OR topic = :topic)
        AND status IN ('APPROVED', 'PUBLISHED')
        AND isArchived = 0
        AND id NOT IN (SELECT questionId FROM user_question_history WHERE userId = :userId)
        ORDER BY RANDOM()
        LIMIT :count
    """)
    suspend fun getAvailableUnworkedQuestions(
        userId: Long,
        module: String,
        difficulty: String,
        topic: String,
        count: Int
    ): List<QuestionEntity>

    @Query("""
        SELECT COUNT(*) FROM questions 
        WHERE module = :module 
        AND (:difficulty = 'ALL' OR difficulty = :difficulty)
        AND (:topic = 'Barcha mavzular' OR topic = :topic)
        AND status IN ('APPROVED', 'PUBLISHED')
        AND isArchived = 0
        AND id NOT IN (SELECT questionId FROM user_question_history WHERE userId = :userId)
    """)
    suspend fun countAvailableUnworkedQuestions(
        userId: Long,
        module: String,
        difficulty: String,
        topic: String
    ): Int

    @Query("""
        SELECT COUNT(*) FROM questions 
        WHERE module = :module 
        AND status IN ('APPROVED', 'PUBLISHED')
        AND isArchived = 0
        AND id NOT IN (SELECT questionId FROM user_question_history WHERE userId = :userId)
    """)
    suspend fun countTotalUnworkedQuestionsByModule(
        userId: Long,
        module: String
    ): Int

    @Query("""
        SELECT * FROM questions 
        WHERE module = 'GEOMETRY' 
        AND (:bookSource = 'ALL' OR bookSource = :bookSource)
        AND (:difficulty = 'ALL' OR difficulty = :difficulty)
        AND (:topic = 'Barcha mavzular' OR topic = :topic)
        AND status IN ('APPROVED', 'PUBLISHED')
        AND isArchived = 0
        AND id NOT IN (SELECT questionId FROM user_question_history WHERE userId = :userId)
        ORDER BY RANDOM()
        LIMIT :count
    """)
    suspend fun getAvailableUnworkedGeometryQuestions(
        userId: Long,
        bookSource: String,
        difficulty: String,
        topic: String,
        count: Int
    ): List<QuestionEntity>

    @Query("""
        SELECT COUNT(*) FROM questions 
        WHERE module = 'GEOMETRY' 
        AND (:bookSource = 'ALL' OR bookSource = :bookSource)
        AND (:difficulty = 'ALL' OR difficulty = :difficulty)
        AND (:topic = 'Barcha mavzular' OR topic = :topic)
        AND status IN ('APPROVED', 'PUBLISHED')
        AND isArchived = 0
        AND id NOT IN (SELECT questionId FROM user_question_history WHERE userId = :userId)
    """)
    suspend fun countAvailableUnworkedGeometryQuestions(
        userId: Long,
        bookSource: String,
        difficulty: String,
        topic: String
    ): Int

    // Duplicate detection and lookup for JSON imports
    @Query("SELECT * FROM questions WHERE customQuestionId = :customId LIMIT 1")
    suspend fun getQuestionByCustomId(customId: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE questionHash = :hash LIMIT 1")
    suspend fun getQuestionByHash(hash: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE questionText = :text LIMIT 1")
    suspend fun getQuestionByText(text: String): QuestionEntity?

    @Query("UPDATE questions SET isArchived = 1, status = 'ARCHIVED' WHERE id = :id")
    suspend fun archiveQuestion(id: Long)

    @Query("UPDATE questions SET isArchived = 0, status = 'PUBLISHED' WHERE id = :id")
    suspend fun unarchiveQuestion(id: Long)

    @Query("UPDATE questions SET status = :status WHERE id = :id")
    suspend fun updateQuestionStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM questions WHERE difficulty = :difficulty AND isArchived = 0")
    fun countQuestionsByDifficultyOverall(difficulty: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE status = :status")
    fun countQuestionsByStatus(status: String): Flow<Int>

    @Query("SELECT customQuestionId FROM questions WHERE customQuestionId IS NOT NULL AND customQuestionId != ''")
    suspend fun getAllExistingCustomIds(): List<String>

    @Query("SELECT questionHash FROM questions WHERE questionHash IS NOT NULL AND questionHash != ''")
    suspend fun getAllExistingHashes(): List<String>

    @Query("SELECT * FROM questions WHERE customQuestionId IN (:customIds)")
    suspend fun getQuestionsByCustomIds(customIds: List<String>): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionHash IN (:hashes)")
    suspend fun getQuestionsByHashes(hashes: List<String>): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    @Query("UPDATE questions SET timesAttempted = timesAttempted + 1, timesCorrect = timesCorrect + :correctIncrement, totalTimeSpentSeconds = totalTimeSpentSeconds + :timeSpent, averageTimeSpentSeconds = (totalTimeSpentSeconds + :timeSpent) / (timesAttempted + 1) WHERE id = :questionId")
    suspend fun recordQuestionAttemptStats(questionId: Long, correctIncrement: Int, timeSpent: Int)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Update
    suspend fun updateQuestions(questions: List<QuestionEntity>)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
}

@Dao
interface TestTemplateDao {
    @Query("SELECT * FROM test_templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<TestTemplateEntity>>

    @Query("SELECT * FROM test_templates WHERE module = :module ORDER BY id ASC")
    fun getTemplatesByModule(module: String): Flow<List<TestTemplateEntity>>

    @Query("SELECT * FROM test_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Long): TestTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TestTemplateEntity): Long

    @Delete
    suspend fun deleteTemplate(template: TestTemplateEntity)
}

@Dao
interface TestAttemptDao {
    @Query("SELECT * FROM test_attempts WHERE userId = :userId ORDER BY startedAt DESC")
    fun getAttemptsByUser(userId: Long): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptById(id: Long): TestAttemptEntity?

    @Query("SELECT * FROM test_attempts WHERE userId = :userId AND state = 'IN_PROGRESS' ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveAttempt(userId: Long): TestAttemptEntity?

    @Query("SELECT * FROM test_attempts WHERE state = 'COMPLETED' ORDER BY completedAt DESC")
    fun getAllCompletedAttemptsAdmin(): Flow<List<TestAttemptEntity>>

    @Query("SELECT COUNT(*) FROM test_attempts WHERE state = 'COMPLETED'")
    fun countCompletedAttempts(): Flow<Int>

    @Query("SELECT COUNT(*) FROM test_attempts WHERE userId = :userId AND state = 'COMPLETED'")
    fun countCompletedAttemptsByUser(userId: Long): Flow<Int>

    @Query("SELECT AVG(percentage) FROM test_attempts WHERE state = 'COMPLETED'")
    fun getGlobalAveragePercentage(): Flow<Float?>

    @Query("SELECT AVG(percentage) FROM test_attempts WHERE userId = :userId AND state = 'COMPLETED'")
    fun getUserAveragePercentage(userId: Long): Flow<Float?>

    @Query("SELECT MAX(score) FROM test_attempts WHERE userId = :userId AND state = 'COMPLETED'")
    fun getUserBestScore(userId: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long

    @Update
    suspend fun updateAttempt(attempt: TestAttemptEntity)

    @Delete
    suspend fun deleteAttempt(attempt: TestAttemptEntity)
}

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE attemptId = :attemptId")
    suspend fun getAnswersByAttempt(attemptId: Long): List<AnswerEntity>

    @Query("SELECT * FROM answers WHERE attemptId = :attemptId AND questionId = :questionId LIMIT 1")
    suspend fun getAnswer(attemptId: Long, questionId: Long): AnswerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AnswerEntity>)

    @Query("DELETE FROM answers WHERE attemptId = :attemptId")
    suspend fun deleteAnswersByAttempt(attemptId: Long)
}

@Dao
interface SavedQuestionDao {
    @Query("SELECT q.* FROM questions q INNER JOIN saved_questions s ON q.id = s.questionId WHERE s.userId = :userId ORDER BY s.savedAt DESC")
    fun getSavedQuestionsByUser(userId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_questions WHERE userId = :userId AND questionId = :questionId)")
    fun isQuestionSaved(userId: Long, questionId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuestion(savedQuestion: SavedQuestionEntity): Long

    @Query("DELETE FROM saved_questions WHERE userId = :userId AND questionId = :questionId")
    suspend fun removeSavedQuestion(userId: Long, questionId: Long)
}

@Dao
interface UserErrorDao {
    @Query("SELECT q.* FROM questions q INNER JOIN user_errors e ON q.id = e.questionId WHERE e.userId = :userId AND e.resolved = 0 ORDER BY e.timestamp DESC")
    fun getUnresolvedErrors(userId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM user_errors WHERE userId = :userId AND questionId = :questionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getErrorRecord(userId: Long, questionId: Long): UserErrorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertError(error: UserErrorEntity): Long

    @Query("UPDATE user_errors SET resolved = 1 WHERE userId = :userId AND questionId = :questionId")
    suspend fun markResolved(userId: Long, questionId: Long)

    @Query("DELETE FROM user_errors WHERE userId = :userId AND questionId = :questionId")
    suspend fun deleteError(userId: Long, questionId: Long)
}

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdminLogEntity): Long
}

@Dao
interface PracticeSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: com.example.data.model.PracticeSessionEntity): Long

    @Update
    suspend fun updateSession(session: com.example.data.model.PracticeSessionEntity)

    @Query("SELECT * FROM practice_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): com.example.data.model.PracticeSessionEntity?

    @Query("SELECT * FROM practice_sessions WHERE userId = :userId AND (status = 'IN_PROGRESS' OR status = 'PAUSED') ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(userId: Long): com.example.data.model.PracticeSessionEntity?

    @Query("SELECT * FROM practice_sessions WHERE userId = :userId AND module = :module AND (status = 'IN_PROGRESS' OR status = 'PAUSED') ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSessionByModule(userId: Long, module: String): com.example.data.model.PracticeSessionEntity?

    @Query("SELECT * FROM practice_sessions WHERE userId = :userId AND status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedSessionsByUser(userId: Long): Flow<List<com.example.data.model.PracticeSessionEntity>>

    @Query("SELECT * FROM practice_sessions WHERE userId = :userId AND module = :module AND status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedSessionsByUserAndModule(userId: Long, module: String): Flow<List<com.example.data.model.PracticeSessionEntity>>

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE userId = :userId AND module = :module AND status = 'COMPLETED'")
    fun countCompletedSessionsByModule(userId: Long, module: String): Flow<Int>

    @Query("SELECT * FROM practice_sessions WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getAllCompletedSessions(): Flow<List<com.example.data.model.PracticeSessionEntity>>

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE userId = :userId AND status = 'COMPLETED'")
    fun countCompletedSessions(userId: Long): Flow<Int>

    @Query("SELECT SUM(totalActiveTimeSeconds) FROM practice_sessions WHERE userId = :userId AND status = 'COMPLETED'")
    fun getTotalActiveTimeByUser(userId: Long): Flow<Long?>

    @Query("DELETE FROM practice_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
}

@Dao
interface AttemptAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: com.example.data.model.AttemptAnswerEntity): Long

    @Query("SELECT * FROM attempt_answers WHERE sessionId = :sessionId ORDER BY questionNumber ASC")
    suspend fun getAnswersBySession(sessionId: Long): List<com.example.data.model.AttemptAnswerEntity>

    @Query("SELECT * FROM attempt_answers WHERE sessionId = :sessionId AND questionId = :questionId LIMIT 1")
    suspend fun getAnswer(sessionId: Long, questionId: Long): com.example.data.model.AttemptAnswerEntity?

    @Query("SELECT * FROM attempt_answers WHERE userId = :userId")
    suspend fun getAnswersByUser(userId: Long): List<com.example.data.model.AttemptAnswerEntity>

    @Query("SELECT * FROM attempt_answers")
    suspend fun getAllAnswers(): List<com.example.data.model.AttemptAnswerEntity>

    @Query("DELETE FROM attempt_answers WHERE sessionId = :sessionId")
    suspend fun deleteAnswersBySession(sessionId: Long)
}

@Dao
interface PracticeConfigDao {
    @Query("SELECT * FROM practice_configs WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<com.example.data.model.PracticeConfigEntity?>

    @Query("SELECT * FROM practice_configs WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): com.example.data.model.PracticeConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: com.example.data.model.PracticeConfigEntity)
}

@Dao
interface UserQuestionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistory(history: UserQuestionHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistories(histories: List<UserQuestionHistoryEntity>): List<Long>

    @Query("SELECT COUNT(*) > 0 FROM user_question_history WHERE userId = :userId AND questionId = :questionId")
    suspend fun hasUserAnswered(userId: Long, questionId: Long): Boolean

    @Query("SELECT questionId FROM user_question_history WHERE userId = :userId")
    suspend fun getUserAnsweredQuestionIds(userId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM user_question_history WHERE userId = :userId")
    fun countUserAnsweredQuestions(userId: Long): Flow<Int>

    @Query("SELECT * FROM user_question_history WHERE userId = :userId ORDER BY answeredAt DESC")
    fun getUserHistory(userId: Long): Flow<List<UserQuestionHistoryEntity>>

    @Query("SELECT COUNT(*) FROM user_question_history WHERE answeredAt >= :sinceTimestamp")
    fun countQuestionsAnsweredSince(sinceTimestamp: Long): Flow<Int>

    @Query("DELETE FROM user_question_history WHERE userId = :userId")
    suspend fun deleteHistoryByUserId(userId: Long)
}

