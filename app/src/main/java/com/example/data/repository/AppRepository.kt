package com.example.data.repository

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
import com.example.data.importer.ImportPreview
import com.example.data.importer.ImportResult
import com.example.data.importer.JsonQuestionImporter
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
import com.example.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class AuthState(
    val currentUser: UserEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class UserLevelInfo(
    val levelName: String,
    val titleUz: String,
    val colorHex: Long,
    val progressPercent: Int
)

data class UserTimeStats(
    val totalQuestionsSolved: Int = 0,
    val totalActiveTimeSeconds: Long = 0,
    val averageQuestionTimeSeconds: Float = 0f,
    val todayActiveTimeSeconds: Long = 0,
    val thisWeekActiveTimeSeconds: Long = 0,
    val thisMonthActiveTimeSeconds: Long = 0,
    val topicAverageTimes: Map<String, Float> = emptyMap(),
    val difficultyAverageTimes: Map<String, Float> = emptyMap()
)

data class AdminTimeAnalytics(
    val globalAverageQuestionTimeSeconds: Float = 0f,
    val globalAverageTestTimeSeconds: Float = 0f,
    val topicAverageTimes: Map<String, Float> = emptyMap(),
    val difficultyAverageTimes: Map<String, Float> = emptyMap(),
    val slowestQuestions: List<QuestionEntity> = emptyList(),
    val fastestQuestions: List<QuestionEntity> = emptyList(),
    val difficultyReviewAlerts: List<DifficultyReviewAlert> = emptyList()
)

data class DifficultyReviewAlert(
    val questionId: Long,
    val questionText: String,
    val recordedDifficulty: String,
    val averageTimeSpentSeconds: Int,
    val accuracyPercentage: Float,
    val recommendation: String
)

class AppRepository(
    val userDao: UserDao,
    val questionDao: QuestionDao,
    val testTemplateDao: TestTemplateDao,
    val testAttemptDao: TestAttemptDao,
    val answerDao: AnswerDao,
    val savedQuestionDao: SavedQuestionDao,
    val userErrorDao: UserErrorDao,
    val adminLogDao: AdminLogDao,
    val practiceSessionDao: PracticeSessionDao,
    val attemptAnswerDao: AttemptAnswerDao,
    val practiceConfigDao: PracticeConfigDao,
    val userQuestionHistoryDao: UserQuestionHistoryDao
) {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // -------------------------------------------------------------
    // AUTHENTICATION & PERSONAL DATA SECURITY
    // -------------------------------------------------------------
    suspend fun login(identifier: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanIdentifier = identifier.trim()
        val user = userDao.getUserByIdentifier(cleanIdentifier)
            ?: return@withContext Result.failure(Exception("Foydalanuvchi topilmadi. Username yoki emailni tekshiring."))

        if (user.isBlocked) {
            return@withContext Result.failure(Exception("Akkaunt bloklangan. Iltimos administrator bilan bog'laning."))
        }

        if (!PasswordHasher.verifyPassword(password, user.passwordHash)) {
            return@withContext Result.failure(Exception("Parol noto'g'ri kiritildi."))
        }

        _authState.value = AuthState(currentUser = user)
        adminLogDao.insertLog(
            AdminLogEntity(
                adminUserId = user.id,
                adminUsername = user.username,
                action = "LOGIN",
                targetType = "USER",
                targetId = user.id.toString(),
                details = "User ${user.username} logged in successfully"
            )
        )
        Result.success(user)
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanUsername.length < 3) {
            return@withContext Result.failure(Exception("Username kamida 3 ta belgidan iborat bo'lishi kerak."))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext Result.failure(Exception("Noto'g'ri email manzili kiritildi."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(Exception("Parol kamida 6 ta belgidan iborat bo'lishi kerak."))
        }

        if (userDao.getUserByUsername(cleanUsername) != null) {
            return@withContext Result.failure(Exception("Bu username band. Boshqa username tanlang."))
        }
        if (userDao.getUserByEmail(cleanEmail) != null) {
            return@withContext Result.failure(Exception("Bu email orqali allaqachon ro'yxatdan o'tilgan."))
        }

        val newUser = UserEntity(
            username = cleanUsername,
            email = cleanEmail,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            passwordHash = PasswordHasher.hashPassword(password),
            role = "USER"
        )
        val id = userDao.insertUser(newUser)
        val inserted = newUser.copy(id = id)
        _authState.value = AuthState(currentUser = inserted)

        adminLogDao.insertLog(
            AdminLogEntity(
                adminUserId = id,
                adminUsername = inserted.username,
                action = "REGISTER",
                targetType = "USER",
                targetId = id.toString(),
                details = "User registered with data privacy consent"
            )
        )

        Result.success(inserted)
    }

    fun logout() {
        _authState.value = AuthState(currentUser = null)
    }

    // -------------------------------------------------------------
    // QUESTIONS & PRACTICE ENGINE (BACKEND-FIRST VALIDATION & TIMING)
    // -------------------------------------------------------------
    fun getQuestionsByModule(module: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsByModule(module)

    fun getQuestionsByDifficulty(module: String, difficulty: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsByDifficulty(module, difficulty)

    fun getQuestionsByTopic(module: String, topic: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsByTopic(module, topic)

    fun getTopicsByModule(module: String): Flow<List<String>> =
        questionDao.getTopicsByModule(module)

    // Backend-validated practice session creation
    suspend fun createPracticeSession(
        userId: Long,
        module: String = "MATHEMATICS",
        topic: String,
        difficulty: String,
        questionCount: Int,
        bookSource: String = "ALL"
    ): Pair<PracticeSessionEntity, List<QuestionEntity>> = withContext(Dispatchers.IO) {
        // Backend Validation (Section 38 Requirement)
        if (questionCount !in 1..100) {
            throw IllegalArgumentException("Misollar soni 1 dan 100 gacha bo'lishi kerak. Kiritilgan: $questionCount")
        }

        val validDifficulties = listOf("ALL", "EASY", "MEDIUM", "HARD", "VERY_HARD")
        if (difficulty !in validDifficulties) {
            throw IllegalArgumentException("Noto'g'ri qiyinlik darajasi tanlandi: $difficulty")
        }

        // Section 7 & 8: Check available unworked questions (UNIQUE user-question history)
        val totalUnworked = questionDao.countTotalUnworkedQuestionsByModule(userId, module)
        if (totalUnworked == 0) {
            throw IllegalStateException("Siz ushbu bo‘limdagi barcha mavjud savollarni ishlab bo‘lgansiz. Yangi savollar admin tomonidan qo‘shilgach davom etishingiz mumkin.")
        }

        val availableCount = if (module == "GEOMETRY") {
            questionDao.countAvailableUnworkedGeometryQuestions(userId, bookSource, difficulty, topic)
        } else {
            questionDao.countAvailableUnworkedQuestions(userId, module, difficulty, topic)
        }

        if (availableCount < questionCount) {
            throw IllegalStateException("Siz tanlagan darajada yangi savollar yetarli emas (mavjud: $availableCount ta). Boshqa daraja yoki boshqa mavzuni tanlang.")
        }

        // Fetch unworked questions using backend filter
        val questions = if (module == "GEOMETRY") {
            questionDao.getAvailableUnworkedGeometryQuestions(userId, bookSource, difficulty, topic, questionCount)
        } else {
            questionDao.getAvailableUnworkedQuestions(userId, module, difficulty, topic, questionCount)
        }

        if (questions.isEmpty()) {
            throw IllegalStateException("Siz tanlagan darajada yangi savollar yetarli emas. Boshqa daraja yoki boshqa mavzuni tanlang.")
        }

        val session = PracticeSessionEntity(
            userId = userId,
            module = module,
            topic = topic,
            difficulty = difficulty,
            bookSource = bookSource,
            questionCount = questions.size,
            startedAt = System.currentTimeMillis(),
            status = "IN_PROGRESS",
            currentQuestionIndex = 0
        )
        val sessionId = practiceSessionDao.insertSession(session)
        val createdSession = session.copy(id = sessionId)

        // Pre-record first question start timestamp (Server authoritative)
        if (questions.isNotEmpty()) {
            attemptAnswerDao.insertAnswer(
                AttemptAnswerEntity(
                    sessionId = sessionId,
                    userId = userId,
                    questionId = questions.first().id,
                    questionNumber = 1,
                    selectedAnswer = null,
                    isCorrect = false,
                    questionStartedAt = System.currentTimeMillis()
                )
            )
        }

        Pair(createdSession, questions)
    }

    fun countGeometryQuestions(): Flow<Int> = questionDao.countApprovedGeometryQuestions()
    fun countGeometryByBookSource(bookSource: String): Flow<Int> = questionDao.countGeometryByBookSource(bookSource)
    fun getGeometryTopics(): Flow<List<String>> = questionDao.getGeometryTopics()
    fun getGeometryBookSources(): Flow<List<String>> = questionDao.getGeometryBookSources()
    suspend fun getActiveGeometrySession(userId: Long): PracticeSessionEntity? = withContext(Dispatchers.IO) {
        practiceSessionDao.getActiveSessionByModule(userId, "GEOMETRY")
    }
    fun getCompletedGeometrySessions(userId: Long): Flow<List<PracticeSessionEntity>> =
        practiceSessionDao.getCompletedSessionsByUserAndModule(userId, "GEOMETRY")

    suspend fun recordQuestionStart(
        sessionId: Long,
        userId: Long,
        questionId: Long,
        questionNumber: Int
    ) = withContext(Dispatchers.IO) {
        val session = practiceSessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session topilmadi")

        if (session.userId != userId) {
            throw SecurityException("403 Forbidden: Ushbu mashg'ulot sessiyasi boshqa foydalanuvchiga tegishli")
        }

        val existing = attemptAnswerDao.getAnswer(sessionId, questionId)
        if (existing == null) {
            attemptAnswerDao.insertAnswer(
                AttemptAnswerEntity(
                    sessionId = sessionId,
                    userId = userId,
                    questionId = questionId,
                    questionNumber = questionNumber,
                    selectedAnswer = null,
                    isCorrect = false,
                    questionStartedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun recordQuestionAnswer(
        sessionId: Long,
        userId: Long,
        questionId: Long,
        questionNumber: Int,
        selectedOption: String
    ): Boolean = withContext(Dispatchers.IO) {
        val session = practiceSessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session topilmadi")

        // Anti-cheat & Security check (Requirement 27)
        if (session.userId != userId) {
            throw SecurityException("403 Forbidden: Sessiya huquqi buzildi")
        }

        val question = questionDao.getQuestionById(questionId)
            ?: throw IllegalArgumentException("Savol topilmadi")

        val isCorrect = question.correctAnswer.equals(selectedOption, ignoreCase = true)
        val now = System.currentTimeMillis()

        val existingAnswer = attemptAnswerDao.getAnswer(sessionId, questionId)
        val startTime = existingAnswer?.questionStartedAt ?: (now - 15000)
        val timeSpent = maxOf(1, ((now - startTime) / 1000).toInt())

        val record = AttemptAnswerEntity(
            id = existingAnswer?.id ?: 0,
            sessionId = sessionId,
            userId = userId,
            questionId = questionId,
            questionNumber = questionNumber,
            selectedAnswer = selectedOption,
            isCorrect = isCorrect,
            questionStartedAt = startTime,
            questionAnsweredAt = now,
            timeSpentSeconds = timeSpent
        )
        attemptAnswerDao.insertAnswer(record)

        // Update Question Time Analytics
        questionDao.recordQuestionAttemptStats(
            questionId = questionId,
            correctIncrement = if (isCorrect) 1 else 0,
            timeSpent = timeSpent
        )

        // Record in user_errors if wrong
        if (!isCorrect) {
            recordUserError(
                userId = userId,
                questionId = questionId,
                wrongAnswer = selectedOption,
                correctOption = question.correctAnswer
            )
        }

        // Section 7 & 8: Permanent UNIQUE user-question history to never repeat
        userQuestionHistoryDao.insertHistory(
            UserQuestionHistoryEntity(
                userId = userId,
                questionId = questionId,
                sessionId = sessionId,
                answeredAt = now,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpent
            )
        )

        isCorrect
    }

    suspend fun pausePracticeSession(sessionId: Long, userId: Long) = withContext(Dispatchers.IO) {
        val session = practiceSessionDao.getSessionById(sessionId) ?: return@withContext
        if (session.userId != userId) throw SecurityException("403 Forbidden")

        val updated = session.copy(
            status = "PAUSED",
            pausedAt = System.currentTimeMillis()
        )
        practiceSessionDao.updateSession(updated)
    }

    suspend fun resumePracticeSession(sessionId: Long, userId: Long): PracticeSessionEntity = withContext(Dispatchers.IO) {
        val session = practiceSessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session topilmadi")
        if (session.userId != userId) throw SecurityException("403 Forbidden")

        val now = System.currentTimeMillis()
        val additionalPause = if (session.pausedAt != null) {
            ((now - session.pausedAt) / 1000).toInt()
        } else 0

        val updated = session.copy(
            status = "IN_PROGRESS",
            resumedAt = now,
            pausedAt = null,
            pauseDurationSeconds = session.pauseDurationSeconds + additionalPause
        )
        practiceSessionDao.updateSession(updated)
        updated
    }

    suspend fun finishPracticeSession(sessionId: Long, userId: Long): PracticeSessionEntity = withContext(Dispatchers.IO) {
        val session = practiceSessionDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Session topilmadi")
        if (session.userId != userId) throw SecurityException("403 Forbidden")

        val answers = attemptAnswerDao.getAnswersBySession(sessionId)
        val totalQuestions = session.questionCount
        val completedCount = answers.count { it.selectedAnswer != null }
        val correctCount = answers.count { it.isCorrect }
        val wrongCount = completedCount - correctCount
        val unansweredCount = maxOf(0, totalQuestions - completedCount)

        val percentage = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions) * 100f else 0f
        val score = (correctCount * 2.2f).toInt() // Standard weight calculation

        val totalActiveTimeSeconds = answers.filter { it.selectedAnswer != null }.sumOf { it.timeSpentSeconds }
        val now = System.currentTimeMillis()
        val totalDurationSeconds = maxOf(totalActiveTimeSeconds, ((now - session.startedAt) / 1000).toInt() - session.pauseDurationSeconds)

        val avgTime = if (completedCount > 0) totalActiveTimeSeconds.toFloat() / completedCount else 0f
        val answeredRecords = answers.filter { it.selectedAnswer != null && it.timeSpentSeconds > 0 }
        val fastestSeconds = answeredRecords.minOfOrNull { it.timeSpentSeconds } ?: 0
        val slowestSeconds = answeredRecords.maxOfOrNull { it.timeSpentSeconds } ?: 0

        val grade = when {
            percentage >= 90f -> "A+"
            percentage >= 80f -> "A"
            percentage >= 70f -> "B+"
            percentage >= 60f -> "B"
            percentage >= 50f -> "C+"
            percentage >= 40f -> "C"
            else -> "Qoniqarsiz"
        }

        // AI / Recommendation Layer (Section 36)
        val recommendation = when {
            percentage >= 85f -> "Ajoyib natija! Milliy sertifikat A+ darajasiga juda yaqinsiz. Murakkab parametrli va stereometriya masalalarini ishlashni davom ettiring."
            percentage >= 70f -> "Yaxshi ko'rsatkich! O'rtacha hisoblash tezligingiz ${String.format("%.0f", avgTime)} soniya. Murakkab tengsizliklar va trigonometriyani mustahkamlash tavsiya etiladi."
            percentage >= 50f -> "Qoniqarli. Xatolar tahlilidagi masalalarni qayta ishlab, formulalarni takrorlang."
            else -> "Boshlang'ich daraja. Oson va o'rta toifadagi misollarni ko'proq yechib, bazaviy qoidalarni mustahkamlang."
        }

        val completedSession = session.copy(
            status = "COMPLETED",
            completedAt = now,
            score = score,
            percentage = percentage,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unansweredCount = unansweredCount,
            totalActiveTimeSeconds = totalActiveTimeSeconds,
            totalDurationSeconds = totalDurationSeconds,
            averageTimePerQuestionSeconds = avgTime,
            fastestQuestionSeconds = fastestSeconds,
            slowestQuestionSeconds = slowestSeconds,
            certificateGrade = grade,
            aiRecommendation = recommendation
        )

        practiceSessionDao.updateSession(completedSession)
        completedSession
    }

    suspend fun getActivePracticeSession(userId: Long): Pair<PracticeSessionEntity, List<QuestionEntity>>? = withContext(Dispatchers.IO) {
        val session = practiceSessionDao.getActiveSession(userId) ?: return@withContext null
        val answers = attemptAnswerDao.getAnswersBySession(session.id)
        val questionIds = answers.map { it.questionId }

        val questions = if (questionIds.isNotEmpty()) {
            questionIds.mapNotNull { questionDao.getQuestionById(it) }
        } else {
            questionDao.getRandomQuestions(session.module, session.questionCount)
        }

        Pair(session, questions)
    }

    // -------------------------------------------------------------
    // USER TIME & PROGRESS STATISTICS (Section 18, 19, 20)
    // -------------------------------------------------------------
    suspend fun getUserTimeStatistics(userId: Long): UserTimeStats = withContext(Dispatchers.IO) {
        val completedSessions = practiceSessionDao.getCompletedSessionsByUser(userId).first()
        val allAnswers = attemptAnswerDao.getAnswersByUser(userId)

        val totalQuestions = allAnswers.count { it.selectedAnswer != null }
        val totalActiveSeconds = completedSessions.sumOf { it.totalActiveTimeSeconds.toLong() }
        val avgQuestionSeconds = if (totalQuestions > 0) totalActiveSeconds.toFloat() / totalQuestions else 0f

        val now = System.currentTimeMillis()
        val oneDayAgo = now - 24 * 3600 * 1000L
        val oneWeekAgo = now - 7 * 24 * 3600 * 1000L
        val oneMonthAgo = now - 30 * 24 * 3600 * 1000L

        val todaySeconds = completedSessions.filter { it.startedAt >= oneDayAgo }.sumOf { it.totalActiveTimeSeconds.toLong() }
        val weekSeconds = completedSessions.filter { it.startedAt >= oneWeekAgo }.sumOf { it.totalActiveTimeSeconds.toLong() }
        val monthSeconds = completedSessions.filter { it.startedAt >= oneMonthAgo }.sumOf { it.totalActiveTimeSeconds.toLong() }

        // Topic-based average time
        val topicTimes = mutableMapOf<String, Pair<Long, Int>>() // topic -> (totalSeconds, count)
        val difficultyTimes = mutableMapOf<String, Pair<Long, Int>>()

        for (ans in allAnswers) {
            if (ans.selectedAnswer != null && ans.timeSpentSeconds > 0) {
                val q = questionDao.getQuestionById(ans.questionId)
                if (q != null) {
                    val curTopic = topicTimes.getOrDefault(q.topic, Pair(0L, 0))
                    topicTimes[q.topic] = Pair(curTopic.first + ans.timeSpentSeconds, curTopic.second + 1)

                    val curDiff = difficultyTimes.getOrDefault(q.difficulty, Pair(0L, 0))
                    difficultyTimes[q.difficulty] = Pair(curDiff.first + ans.timeSpentSeconds, curDiff.second + 1)
                }
            }
        }

        val topicAvgMap = topicTimes.mapValues { (_, pair) ->
            if (pair.second > 0) pair.first.toFloat() / pair.second else 0f
        }
        val diffAvgMap = difficultyTimes.mapValues { (_, pair) ->
            if (pair.second > 0) pair.first.toFloat() / pair.second else 0f
        }

        UserTimeStats(
            totalQuestionsSolved = totalQuestions,
            totalActiveTimeSeconds = totalActiveSeconds,
            averageQuestionTimeSeconds = avgQuestionSeconds,
            todayActiveTimeSeconds = todaySeconds,
            thisWeekActiveTimeSeconds = weekSeconds,
            thisMonthActiveTimeSeconds = monthSeconds,
            topicAverageTimes = topicAvgMap,
            difficultyAverageTimes = diffAvgMap
        )
    }

    // -------------------------------------------------------------
    // ADMIN TIME ANALYTICS & TIME-BASED DIFFICULTY AUDIT (Section 21, 22, 23)
    // -------------------------------------------------------------
    suspend fun getAdminTimeAnalytics(requesterUserId: Long): AdminTimeAnalytics = withContext(Dispatchers.IO) {
        requireAdminRole(requesterUserId)

        val allSessions = practiceSessionDao.getAllCompletedSessions().first()
        val allAnswers = attemptAnswerDao.getAllAnswers()

        val totalActiveTime = allSessions.sumOf { it.totalActiveTimeSeconds.toLong() }
        val answeredCount = allAnswers.count { it.selectedAnswer != null }
        val globalAvgQuestionTime = if (answeredCount > 0) totalActiveTime.toFloat() / answeredCount else 0f
        val globalAvgTestTime = if (allSessions.isNotEmpty()) totalActiveTime.toFloat() / allSessions.size else 0f

        val topicTimes = mutableMapOf<String, Pair<Long, Int>>()
        val diffTimes = mutableMapOf<String, Pair<Long, Int>>()
        val questionStats = mutableMapOf<Long, MutableList<Int>>()

        for (ans in allAnswers) {
            if (ans.selectedAnswer != null && ans.timeSpentSeconds > 0) {
                val q = questionDao.getQuestionById(ans.questionId)
                if (q != null) {
                    val t = topicTimes.getOrDefault(q.topic, Pair(0L, 0))
                    topicTimes[q.topic] = Pair(t.first + ans.timeSpentSeconds, t.second + 1)

                    val d = diffTimes.getOrDefault(q.difficulty, Pair(0L, 0))
                    diffTimes[q.difficulty] = Pair(d.first + ans.timeSpentSeconds, d.second + 1)

                    questionStats.getOrPut(q.id) { mutableListOf() }.add(ans.timeSpentSeconds)
                }
            }
        }

        val topicAvgMap = topicTimes.mapValues { (_, pair) ->
            if (pair.second > 0) pair.first.toFloat() / pair.second else 0f
        }
        val diffAvgMap = diffTimes.mapValues { (_, pair) ->
            if (pair.second > 0) pair.first.toFloat() / pair.second else 0f
        }

        // Time-based difficulty analysis alerts (Section 23)
        val alerts = mutableListOf<DifficultyReviewAlert>()
        val sampleQuestions = questionDao.getQuestionsPaged("MATHEMATICS", 50, 0)

        for (q in sampleQuestions) {
            if (q.timesAttempted >= 3) {
                val accuracy = (q.timesCorrect.toFloat() / q.timesAttempted) * 100f
                if (q.difficulty == "HARD" && q.averageTimeSpentSeconds in 1..20 && accuracy > 85f) {
                    alerts.add(
                        DifficultyReviewAlert(
                            questionId = q.id,
                            questionText = q.questionText,
                            recordedDifficulty = q.difficulty,
                            averageTimeSpentSeconds = q.averageTimeSpentSeconds,
                            accuracyPercentage = accuracy,
                            recommendation = "Foydalanuvchilar tez va oson yechmoqda. Darajani O'RTA ga o'zgartirish tavsiya etiladi."
                        )
                    )
                } else if (q.difficulty == "EASY" && q.averageTimeSpentSeconds > 90 && accuracy < 40f) {
                    alerts.add(
                        DifficultyReviewAlert(
                            questionId = q.id,
                            questionText = q.questionText,
                            recordedDifficulty = q.difficulty,
                            averageTimeSpentSeconds = q.averageTimeSpentSeconds,
                            accuracyPercentage = accuracy,
                            recommendation = "Foydalanuvchilar ko'p vaqt va xato qilmoqda. Darajani QIYIN ga ko'tarish tavsiya etiladi."
                        )
                    )
                }
            }
        }

        AdminTimeAnalytics(
            globalAverageQuestionTimeSeconds = globalAvgQuestionTime,
            globalAverageTestTimeSeconds = globalAvgTestTime,
            topicAverageTimes = topicAvgMap,
            difficultyAverageTimes = diffAvgMap,
            slowestQuestions = sampleQuestions.sortedByDescending { it.averageTimeSpentSeconds }.take(5),
            fastestQuestions = sampleQuestions.filter { it.averageTimeSpentSeconds > 0 }.sortedBy { it.averageTimeSpentSeconds }.take(5),
            difficultyReviewAlerts = alerts
        )
    }

    // -------------------------------------------------------------
    // SAVED QUESTIONS & ERRORS
    // -------------------------------------------------------------
    fun getSavedQuestions(userId: Long): Flow<List<QuestionEntity>> =
        savedQuestionDao.getSavedQuestionsByUser(userId)

    fun isQuestionSaved(userId: Long, questionId: Long): Flow<Boolean> =
        savedQuestionDao.isQuestionSaved(userId, questionId)

    suspend fun toggleSaveQuestion(userId: Long, questionId: Long) = withContext(Dispatchers.IO) {
        val isSaved = savedQuestionDao.isQuestionSaved(userId, questionId).first()
        if (isSaved) {
            savedQuestionDao.removeSavedQuestion(userId, questionId)
        } else {
            savedQuestionDao.saveQuestion(
                SavedQuestionEntity(userId = userId, questionId = questionId)
            )
        }
    }

    fun getUnresolvedErrors(userId: Long): Flow<List<QuestionEntity>> =
        userErrorDao.getUnresolvedErrors(userId)

    suspend fun markErrorResolved(userId: Long, questionId: Long) = withContext(Dispatchers.IO) {
        userErrorDao.markResolved(userId, questionId)
    }

    suspend fun recordUserError(
        userId: Long,
        questionId: Long,
        wrongAnswer: String,
        correctOption: String = ""
    ) = withContext(Dispatchers.IO) {
        val q = questionDao.getQuestionById(questionId)
        userErrorDao.insertError(
            UserErrorEntity(
                userId = userId,
                questionId = questionId,
                attemptId = 0L,
                selectedOption = wrongAnswer,
                correctOption = if (correctOption.isNotEmpty()) correctOption else (q?.correctAnswer ?: "")
            )
        )
    }

    // -------------------------------------------------------------
    // TEST ATTEMPTS & MOCK EXAMS
    // -------------------------------------------------------------
    fun getAllTestTemplates(): Flow<List<TestTemplateEntity>> =
        testTemplateDao.getAllTemplates()

    fun getUserAttempts(userId: Long): Flow<List<TestAttemptEntity>> =
        testAttemptDao.getAttemptsByUser(userId)

    suspend fun getActiveAttempt(userId: Long): TestAttemptEntity? = withContext(Dispatchers.IO) {
        testAttemptDao.getActiveAttempt(userId)
    }

    suspend fun getAttemptById(attemptId: Long): TestAttemptEntity? = withContext(Dispatchers.IO) {
        testAttemptDao.getAttemptById(attemptId)
    }

    suspend fun startNewTestAttempt(
        userId: Long,
        template: TestTemplateEntity
    ): Pair<TestAttemptEntity, List<QuestionEntity>> = withContext(Dispatchers.IO) {
        val topicFilter = if (template.topic.contains("Kompleks") || template.topic == "Barcha mavzular") "Barcha mavzular" else template.topic
        val questions = questionDao.getAvailableUnworkedQuestions(
            userId = userId,
            module = template.module,
            difficulty = template.difficulty,
            topic = topicFilter,
            count = template.questionCount
        ).ifEmpty {
            when {
                template.difficulty != "ALL" -> {
                    questionDao.getRandomQuestionsByDifficulty(template.module, template.difficulty, template.questionCount)
                }
                topicFilter != "Barcha mavzular" -> {
                    questionDao.getRandomQuestionsByTopic(template.module, topicFilter, template.questionCount)
                }
                else -> {
                    questionDao.getRandomQuestions(template.module, template.questionCount)
                }
            }
        }

        val attempt = TestAttemptEntity(
            userId = userId,
            testTitle = template.title,
            module = template.module,
            totalQuestions = questions.size,
            state = "IN_PROGRESS",
            startedAt = System.currentTimeMillis()
        )
        val attemptId = testAttemptDao.insertAttempt(attempt)
        val savedAttempt = attempt.copy(id = attemptId)

        Pair(savedAttempt, questions)
    }

    suspend fun saveAnswer(
        attemptId: Long,
        questionId: Long,
        selectedOption: String
    ) = withContext(Dispatchers.IO) {
        val question = questionDao.getQuestionById(questionId) ?: return@withContext
        val isCorrect = question.correctAnswer.equals(selectedOption, ignoreCase = true)
        answerDao.insertAnswer(
            AnswerEntity(
                attemptId = attemptId,
                questionId = questionId,
                selectedOption = selectedOption,
                isCorrect = isCorrect
            )
        )

        // Permanent history record
        val attempt = testAttemptDao.getAttemptById(attemptId)
        if (attempt != null) {
            userQuestionHistoryDao.insertHistory(
                UserQuestionHistoryEntity(
                    userId = attempt.userId,
                    questionId = questionId,
                    sessionId = attemptId,
                    answeredAt = System.currentTimeMillis(),
                    isCorrect = isCorrect,
                    timeSpentSeconds = 0
                )
            )
        }
    }

    suspend fun getAnswersForAttempt(attemptId: Long): List<AnswerEntity> = withContext(Dispatchers.IO) {
        answerDao.getAnswersByAttempt(attemptId)
    }

    suspend fun finishTestAttempt(
        attemptId: Long,
        timeSpentSeconds: Int
    ): TestAttemptEntity = withContext(Dispatchers.IO) {
        val attempt = testAttemptDao.getAttemptById(attemptId)
            ?: throw IllegalStateException("Test attempt not found")
        val answers = answerDao.getAnswersByAttempt(attemptId)

        var correctCount = 0
        var wrongCount = 0
        val topicStats = mutableMapOf<String, Pair<Int, Int>>() // topic -> (correct, total)

        for (ans in answers) {
            val q = questionDao.getQuestionById(ans.questionId)
            if (q != null) {
                val current = topicStats.getOrDefault(q.topic, Pair(0, 0))
                val isCorrect = q.correctAnswer.equals(ans.selectedOption, ignoreCase = true)
                if (isCorrect) {
                    correctCount++
                    topicStats[q.topic] = Pair(current.first + 1, current.second + 1)
                } else {
                    wrongCount++
                    topicStats[q.topic] = Pair(current.first, current.second + 1)
                    recordUserError(
                        userId = attempt.userId,
                        questionId = q.id,
                        wrongAnswer = ans.selectedOption ?: "-",
                        correctOption = q.correctAnswer
                    )
                }
            }
        }

        val unanswered = maxOf(0, attempt.totalQuestions - (correctCount + wrongCount))
        val percentage = if (attempt.totalQuestions > 0) (correctCount.toFloat() / attempt.totalQuestions) * 100f else 0f
        val score = (percentage * 1.5f).toInt()

        val jsonBreakdown = JSONObject()
        val weakTopics = mutableListOf<String>()
        for ((topic, pair) in topicStats) {
            val topicPercent = if (pair.second > 0) (pair.first.toFloat() / pair.second) * 100f else 0f
            jsonBreakdown.put(topic, "${pair.first}/${pair.second} (${String.format("%.0f", topicPercent)}%)")
            if (topicPercent < 60f) weakTopics.add(topic)
        }

        val rec = if (weakTopics.isNotEmpty()) {
            "Quyidagi mavzularni qayta takrorlang: ${weakTopics.take(3).joinToString(", ")}."
        } else {
            "Ajoyib ko'rsatkich! Barcha mavzular yaxshi o'zlashtirilgan."
        }

        val completed = attempt.copy(
            state = "COMPLETED",
            score = score,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unansweredCount = unanswered,
            percentage = percentage,
            timeSpentSeconds = timeSpentSeconds,
            completedAt = System.currentTimeMillis(),
            topicBreakdownJson = jsonBreakdown.toString(),
            weakTopicsRecommendation = rec
        )
        testAttemptDao.updateAttempt(completed)
        completed
    }

    fun getUserCompletedAttemptsCount(userId: Long): Flow<Int> =
        testAttemptDao.countCompletedAttemptsByUser(userId)

    fun getUserAveragePercentage(userId: Long): Flow<Float?> =
        testAttemptDao.getUserAveragePercentage(userId)

    fun getUserBestScore(userId: Long): Flow<Int?> =
        testAttemptDao.getUserBestScore(userId)

    fun calculateUserLevel(avgPercent: Float, completedTests: Int, bestScore: Int): UserLevelInfo {
        return getUserLevel(completedTests, avgPercent)
    }

    // -------------------------------------------------------------
    // USER LEVEL & CERTIFICATE BADGES
    // -------------------------------------------------------------
    fun getUserLevel(completedCount: Int, avgPercentage: Float?): UserLevelInfo {
        val pct = avgPercentage ?: 0f
        return when {
            completedCount >= 10 && pct >= 85f -> UserLevelInfo("A+", "Milliy Sertifikat — A+ (Oliy)", 0xFF10B981, 100)
            completedCount >= 7 && pct >= 70f -> UserLevelInfo("A", "Milliy Sertifikat — A (Ilg'or)", 0xFF2563EB, 85)
            completedCount >= 5 && pct >= 60f -> UserLevelInfo("B+", "Milliy Sertifikat — B+ (Yaxshi)", 0xFFF59E0B, 70)
            completedCount >= 3 && pct >= 50f -> UserLevelInfo("B", "Milliy Sertifikat — B (O'rta)", 0xFFF97316, 55)
            completedCount >= 1 -> UserLevelInfo("C+", "Boshlang'ich Tayyorgarlik", 0xFF64748B, 35)
            else -> UserLevelInfo("Yangi", "Tayyorgarlikni boshlang", 0xFF94A3B8, 10)
        }
    }

    // -------------------------------------------------------------
    // DATA PRIVACY, LAW ON PERSONAL DATA & AUDIT (Section 28, 29, 30, 31, 32, 33)
    // -------------------------------------------------------------
    suspend fun exportUserDataJson(userId: Long): String = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: throw IllegalArgumentException("User not found")
        val attempts = testAttemptDao.getAttemptsByUser(userId).first()
        val sessions = practiceSessionDao.getCompletedSessionsByUser(userId).first()
        val saved = savedQuestionDao.getSavedQuestionsByUser(userId).first()
        val errors = userErrorDao.getUnresolvedErrors(userId).first()

        val root = JSONObject()
        val profileJson = JSONObject().apply {
            put("id", user.id)
            put("username", user.username)
            put("email", user.email)
            put("firstName", user.firstName)
            put("lastName", user.lastName)
            put("createdAt", user.createdAt)
            put("role", user.role)
        }
        root.put("profile", profileJson)
        root.put("totalCompletedTests", attempts.size)
        root.put("totalPracticeSessions", sessions.size)
        root.put("savedQuestionsCount", saved.size)
        root.put("unresolvedErrorsCount", errors.size)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("legalNotice", "O'zbekiston Respublikasi 'Shaxsga doir ma'lumotlar to'g'risida'gi Qonuniga muvofiq taqdim etildi.")

        root.toString(2)
    }

    suspend fun deleteUserAccountAndData(userId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("Foydalanuvchi topilmadi"))

        // Audit log before deletion
        adminLogDao.insertLog(
            AdminLogEntity(
                adminUserId = user.id,
                adminUsername = user.username,
                action = "USER_DELETE_OWN_DATA",
                targetType = "USER",
                targetId = userId.toString(),
                details = "User requested full account and data erasure (O'RQ-547)"
            )
        )

        userDao.deleteUserById(userId)
        if (_authState.value.currentUser?.id == userId) {
            _authState.value = AuthState(currentUser = null)
        }
        Result.success(true)
    }

    // Backend-First Security: Require Admin Role
    suspend fun requireAdminRole(userId: Long) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        if (user == null || user.role != "ADMIN") {
            throw SecurityException("403 Forbidden: Faqat administratorlar kirishi mumkin.")
        }
    }

    suspend fun logAdminAction(
        adminUserId: Long,
        adminUsername: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String
    ) = withContext(Dispatchers.IO) {
        adminLogDao.insertLog(
            AdminLogEntity(
                adminUserId = adminUserId,
                adminUsername = adminUsername,
                action = action,
                targetType = targetType,
                targetId = targetId,
                details = details
            )
        )
    }

    // -------------------------------------------------------------
    // ADMIN DASHBOARD QUERIES & ACTIONS
    // -------------------------------------------------------------
    fun countUsers(): Flow<Int> = userDao.countUsers()
    fun countActiveUsers(): Flow<Int> = userDao.countActiveUsers()
    fun countApprovedQuestions(): Flow<Int> = questionDao.countApprovedQuestions()
    fun countQuestionsByModule(module: String): Flow<Int> = questionDao.countQuestionsByModule(module)
    fun countCompletedAttemptsGlobal(): Flow<Int> = testAttemptDao.countCompletedAttempts()
    fun getGlobalAveragePercentage(): Flow<Float?> = testAttemptDao.getGlobalAveragePercentage()
    fun getAllUsersAdmin(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getAllQuestionsAdmin(): Flow<List<QuestionEntity>> = questionDao.getAllQuestionsAdmin()
    fun getQuestionsByModuleAdmin(module: String): Flow<List<QuestionEntity>> = questionDao.getQuestionsByModuleAdmin(module)
    fun getRecentAdminLogs(limit: Int = 100): Flow<List<AdminLogEntity>> = adminLogDao.getRecentLogs(limit)

    suspend fun adminDeleteUser(userId: Long) = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        userDao.deleteUserById(userId)
        logAdminAction(admin.id, admin.username, "DELETE_USER", "USER", userId.toString(), "Admin deleted user")
    }

    suspend fun adminToggleBlockUser(userId: Long) = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        userDao.toggleBlockUser(userId)
        logAdminAction(admin.id, admin.username, "TOGGLE_BLOCK_USER", "USER", userId.toString(), "Admin toggled user block status")
    }

    suspend fun adminSaveQuestion(question: QuestionEntity) = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        if (question.id == 0L) {
            val newId = questionDao.insertQuestion(question)
            logAdminAction(admin.id, admin.username, "CREATE_QUESTION", "QUESTION", newId.toString(), "Admin created question")
        } else {
            questionDao.updateQuestion(question)
            logAdminAction(admin.id, admin.username, "UPDATE_QUESTION", "QUESTION", question.id.toString(), "Admin updated question")
        }
    }

    suspend fun adminDeleteQuestion(questionId: Long) = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        questionDao.deleteQuestionById(questionId)
        logAdminAction(admin.id, admin.username, "DELETE_QUESTION", "QUESTION", questionId.toString(), "Admin deleted question")
    }

    suspend fun adminBulkGenerateQuestions(module: String, topic: String, difficulty: String, count: Int): Int = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        // Generates questions and persists them
        val generatedList = mutableListOf<QuestionEntity>()
        for (i in 1..count) {
            val q = QuestionEntity(
                subject = "Matematika",
                module = module,
                topic = topic,
                subtopic = "",
                difficulty = difficulty,
                questionText = "$topic: $difficulty darajadagi test savoli #$i",
                optionA = "To'g'ri javob A",
                optionB = "Noto'g'ri variant B",
                optionC = "Noto'g'ri variant C",
                optionD = "Noto'g'ri variant D",
                correctAnswer = "A",
                explanation = "Standart yechim usuli.",
                status = "PUBLISHED"
            )
            generatedList.add(q)
        }
        questionDao.insertQuestions(generatedList)
        logAdminAction(admin.id, admin.username, "BULK_GENERATE_QUESTIONS", "QUESTION", "$count items", "Admin bulk generated $count questions for $topic")
        count
    }

    // -------------------------------------------------------------
    // QUESTIONS BY DIFFICULTY & STATUS METRICS
    // -------------------------------------------------------------
    fun countQuestionsByDifficultyOverall(diff: String): Flow<Int> =
        questionDao.countQuestionsByDifficultyOverall(diff)

    fun countQuestionsByStatus(status: String): Flow<Int> =
        questionDao.countQuestionsByStatus(status)

    fun countQuestionsAnsweredToday(): Flow<Int> {
        val startOfDay = System.currentTimeMillis() - 24 * 3600 * 1000L
        return userQuestionHistoryDao.countQuestionsAnsweredSince(startOfDay)
    }

    // -------------------------------------------------------------
    // USER QUESTION HISTORY & UNWORKED AVAILABILITY
    // -------------------------------------------------------------
    fun getUserAnsweredQuestionsCount(userId: Long): Flow<Int> =
        userQuestionHistoryDao.countUserAnsweredQuestions(userId)

    fun getUserAnsweredQuestionsHistory(userId: Long): Flow<List<UserQuestionHistoryEntity>> =
        userQuestionHistoryDao.getUserHistory(userId)

    suspend fun getAvailableUnworkedCount(
        userId: Long,
        module: String,
        difficulty: String,
        topic: String,
        bookSource: String = "ALL"
    ): Int = withContext(Dispatchers.IO) {
        if (module == "GEOMETRY") {
            questionDao.countAvailableUnworkedGeometryQuestions(userId, bookSource, difficulty, topic)
        } else {
            questionDao.countAvailableUnworkedQuestions(userId, module, difficulty, topic)
        }
    }

    // -------------------------------------------------------------
    // ADMIN ACTIONS: ARCHIVE (SOFT DELETE) & STATUS MANAGEMENT
    // -------------------------------------------------------------
    suspend fun adminArchiveQuestion(questionId: Long) = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        questionDao.archiveQuestion(questionId)
        logAdminAction(admin.id, admin.username, "ADMIN_ARCHIVED_QUESTION", "QUESTION", questionId.toString(), "Admin archived question")
    }

    suspend fun adminUnarchiveQuestion(questionId: Long) = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        questionDao.unarchiveQuestion(questionId)
        logAdminAction(admin.id, admin.username, "ADMIN_UNARCHIVED_QUESTION", "QUESTION", questionId.toString(), "Admin unarchived question")
    }

    suspend fun adminUpdateQuestionStatus(questionId: Long, status: String) = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        questionDao.updateQuestionStatus(questionId, status)
        logAdminAction(admin.id, admin.username, "ADMIN_UPDATED_STATUS", "QUESTION", questionId.toString(), "Admin updated status to $status")
    }

    // -------------------------------------------------------------
    // ADMIN JSON IMPORT & VALIDATION
    // -------------------------------------------------------------
    suspend fun previewJsonImport(
        jsonString: String,
        selectedModule: String,
        selectedDifficulty: String,
        conflictPolicy: com.example.data.service.ImportConflictPolicy = com.example.data.service.ImportConflictPolicy.SKIP_DUPLICATES
    ): ImportPreview = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        JsonQuestionImporter.analyzeAndValidate(
            jsonString = jsonString,
            selectedModule = selectedModule,
            selectedDifficulty = selectedDifficulty,
            questionDao = questionDao,
            conflictPolicy = conflictPolicy
        )
    }

    suspend fun executeJsonImport(
        preview: ImportPreview,
        allowPartialImport: Boolean = false,
        conflictPolicy: com.example.data.service.ImportConflictPolicy = com.example.data.service.ImportConflictPolicy.SKIP_DUPLICATES
    ): ImportResult = withContext(Dispatchers.IO) {
        val admin = _authState.value.currentUser ?: throw SecurityException("Not logged in")
        requireAdminRole(admin.id)
        val result = JsonQuestionImporter.executeImport(
            preview = preview,
            allowPartialImport = allowPartialImport,
            questionDao = questionDao,
            conflictPolicy = conflictPolicy,
            adminLogDao = adminLogDao,
            adminUserId = admin.id,
            adminUsername = admin.username
        )
        result
    }
}
