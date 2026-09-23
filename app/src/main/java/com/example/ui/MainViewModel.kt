package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DatabaseInitializer
import com.example.data.importer.ImportPreview
import com.example.data.importer.ImportResult
import com.example.data.model.AdminLogEntity
import com.example.data.model.PracticeSessionEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.TestAttemptEntity
import com.example.data.model.TestTemplateEntity
import com.example.data.model.UserEntity
import com.example.data.repository.AdminTimeAnalytics
import com.example.data.repository.AppRepository
import com.example.data.repository.AuthState
import com.example.data.repository.UserLevelInfo
import com.example.data.repository.UserTimeStats
import com.example.data.service.GeminiQuestionService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    AUTH,
    HOME,
    PRACTICE,
    TESTS,
    ACTIVE_TEST,
    TEST_RESULT,
    ERRORS,
    SAVED,
    STATISTICS,
    GEOMETRY,
    ADMIN_PANEL,
    PRIVACY_POLICY,
    DATA_MANAGEMENT
}

enum class PracticeModeState {
    CONFIGURING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED
}

data class PracticeTestResult(
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val score: Int,
    val percentage: Float,
    val certificateGrade: String,
    val timeSpentSeconds: Int,
    val averageTimePerQuestionSeconds: Float,
    val fastestQuestionSeconds: Int,
    val slowestQuestionSeconds: Int,
    val aiRecommendation: String,
    val difficulty: String,
    val topic: String,
    val questions: List<QuestionEntity>,
    val userAnswers: Map<Long, String>,
    val questionTimes: Map<Long, Int> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AppRepository(
        userDao = db.userDao(),
        questionDao = db.questionDao(),
        testTemplateDao = db.testTemplateDao(),
        testAttemptDao = db.testAttemptDao(),
        answerDao = db.answerDao(),
        savedQuestionDao = db.savedQuestionDao(),
        userErrorDao = db.userErrorDao(),
        adminLogDao = db.adminLogDao(),
        practiceSessionDao = db.practiceSessionDao(),
        attemptAnswerDao = db.attemptAnswerDao(),
        practiceConfigDao = db.practiceConfigDao(),
        userQuestionHistoryDao = db.userQuestionHistoryDao()
    )

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Auth state
    val authState: StateFlow<AuthState> = repository.authState

    // Database initialization status
    private val _isDbInitialized = MutableStateFlow(false)
    val isDbInitialized: StateFlow<Boolean> = _isDbInitialized.asStateFlow()

    // Active Test State (Mock Imtihon)
    private val _activeAttempt = MutableStateFlow<TestAttemptEntity?>(null)
    val activeAttempt: StateFlow<TestAttemptEntity?> = _activeAttempt.asStateFlow()

    private val _testQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val testQuestions: StateFlow<List<QuestionEntity>> = _testQuestions.asStateFlow()

    private val _currentTestQuestionIndex = MutableStateFlow(0)
    val currentTestQuestionIndex: StateFlow<Int> = _currentTestQuestionIndex.asStateFlow()

    private val _testUserAnswers = MutableStateFlow<Map<Long, String>>(emptyMap())
    val testUserAnswers: StateFlow<Map<Long, String>> = _testUserAnswers.asStateFlow()

    private val _testRemainingSeconds = MutableStateFlow(150 * 60)
    val testRemainingSeconds: StateFlow<Int> = _testRemainingSeconds.asStateFlow()

    private var timerJob: Job? = null

    // Last completed test result
    private val _lastCompletedAttempt = MutableStateFlow<TestAttemptEntity?>(null)
    val lastCompletedAttempt: StateFlow<TestAttemptEntity?> = _lastCompletedAttempt.asStateFlow()

    // -------------------------------------------------------------
    // PRACTICE SESSION STATE (Real-time timer & Backend engine)
    // -------------------------------------------------------------
    private val _practiceState = MutableStateFlow(PracticeModeState.CONFIGURING)
    val practiceState: StateFlow<PracticeModeState> = _practiceState.asStateFlow()

    private val _practiceQuestionCount = MutableStateFlow(15)
    val practiceQuestionCount: StateFlow<Int> = _practiceQuestionCount.asStateFlow()

    private val _practiceDurationMinutes = MutableStateFlow(0) // 0 = cheksiz
    val practiceDurationMinutes: StateFlow<Int> = _practiceDurationMinutes.asStateFlow()

    private val _practiceDifficulty = MutableStateFlow("ALL") // "ALL", "EASY", "MEDIUM", "HARD", "VERY_HARD"
    val practiceDifficulty: StateFlow<String> = _practiceDifficulty.asStateFlow()

    private val _practiceTopic = MutableStateFlow("Barcha mavzular")
    val practiceTopic: StateFlow<String> = _practiceTopic.asStateFlow()

    private val _practiceUserAnswers = MutableStateFlow<Map<Long, String>>(emptyMap())
    val practiceUserAnswers: StateFlow<Map<Long, String>> = _practiceUserAnswers.asStateFlow()

    private val _practiceQuestionTimes = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val practiceQuestionTimes: StateFlow<Map<Long, Int>> = _practiceQuestionTimes.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _resumableSession = MutableStateFlow<PracticeSessionEntity?>(null)
    val resumableSession: StateFlow<PracticeSessionEntity?> = _resumableSession.asStateFlow()

    // Real-time timers
    private val _currentQuestionElapsedSeconds = MutableStateFlow(0)
    val currentQuestionElapsedSeconds: StateFlow<Int> = _currentQuestionElapsedSeconds.asStateFlow()

    private val _totalActiveSolvingSeconds = MutableStateFlow(0)
    val totalActiveSolvingSeconds: StateFlow<Int> = _totalActiveSolvingSeconds.asStateFlow()

    private val _practiceRemainingSeconds = MutableStateFlow(0)
    val practiceRemainingSeconds: StateFlow<Int> = _practiceRemainingSeconds.asStateFlow()

    private var practiceTimerJob: Job? = null
    private var currentQuestionStartTime: Long = System.currentTimeMillis()

    private val _practiceResult = MutableStateFlow<PracticeTestResult?>(null)
    val practiceResult: StateFlow<PracticeTestResult?> = _practiceResult.asStateFlow()

    private val _practiceQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val practiceQuestions: StateFlow<List<QuestionEntity>> = _practiceQuestions.asStateFlow()

    private val _practiceIndex = MutableStateFlow(0)
    val practiceIndex: StateFlow<Int> = _practiceIndex.asStateFlow()

    // -------------------------------------------------------------
    // GEOMETRY PRACTICE SESSION STATE (Puza 1 & Puza 2)
    // -------------------------------------------------------------
    private val _geometryState = MutableStateFlow(PracticeModeState.CONFIGURING)
    val geometryState: StateFlow<PracticeModeState> = _geometryState.asStateFlow()

    private val _geometryDifficulty = MutableStateFlow("ALL") // "ALL", "EASY", "MEDIUM", "HARD", "VERY_HARD"
    val geometryDifficulty: StateFlow<String> = _geometryDifficulty.asStateFlow()

    private val _geometryQuestionCount = MutableStateFlow(15)
    val geometryQuestionCount: StateFlow<Int> = _geometryQuestionCount.asStateFlow()

    private val _geometryTopic = MutableStateFlow("Barcha mavzular")
    val geometryTopic: StateFlow<String> = _geometryTopic.asStateFlow()

    private val _geometryBookSource = MutableStateFlow("ALL") // "ALL", "Puza Geometriya 1", "Puza Geometriya 2"
    val geometryBookSource: StateFlow<String> = _geometryBookSource.asStateFlow()

    private val _geometryDurationMinutes = MutableStateFlow(0)
    val geometryDurationMinutes: StateFlow<Int> = _geometryDurationMinutes.asStateFlow()

    private val _geometryRemainingSeconds = MutableStateFlow(0)
    val geometryRemainingSeconds: StateFlow<Int> = _geometryRemainingSeconds.asStateFlow()

    private val _currentGeometrySessionId = MutableStateFlow<Long?>(null)
    val currentGeometrySessionId: StateFlow<Long?> = _currentGeometrySessionId.asStateFlow()

    private val _geometryQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val geometryQuestions: StateFlow<List<QuestionEntity>> = _geometryQuestions.asStateFlow()

    private val _geometryIndex = MutableStateFlow(0)
    val geometryIndex: StateFlow<Int> = _geometryIndex.asStateFlow()

    private val _geometryUserAnswers = MutableStateFlow<Map<Long, String>>(emptyMap())
    val geometryUserAnswers: StateFlow<Map<Long, String>> = _geometryUserAnswers.asStateFlow()

    private val _geometryQuestionTimes = MutableStateFlow<Map<Long, Int>>(emptyMap()) // questionId -> seconds
    val geometryQuestionTimes: StateFlow<Map<Long, Int>> = _geometryQuestionTimes.asStateFlow()

    private val _geometryQuestionElapsedSeconds = MutableStateFlow(0)
    val geometryQuestionElapsedSeconds: StateFlow<Int> = _geometryQuestionElapsedSeconds.asStateFlow()

    private val _geometryTotalActiveSeconds = MutableStateFlow(0)
    val geometryTotalActiveSeconds: StateFlow<Int> = _geometryTotalActiveSeconds.asStateFlow()

    private val _geometryResult = MutableStateFlow<PracticeTestResult?>(null)
    val geometryResult: StateFlow<PracticeTestResult?> = _geometryResult.asStateFlow()

    private var geometryTimerJob: Job? = null
    private var geometryQuestionStartTime: Long = 0L

    // Message / Feedback Toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // UI Loading state
    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    // User time stats and Admin analytics cache
    private val _userTimeStats = MutableStateFlow<UserTimeStats?>(null)
    val userTimeStats: StateFlow<UserTimeStats?> = _userTimeStats.asStateFlow()

    private val _adminAnalytics = MutableStateFlow<AdminTimeAnalytics?>(null)
    val adminAnalytics: StateFlow<AdminTimeAnalytics?> = _adminAnalytics.asStateFlow()

    init {
        viewModelScope.launch {
            DatabaseInitializer.initializeIfNeeded(getApplication())
            _isDbInitialized.value = true
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.HOME) {
            checkForResumableSession()
        }
        if (screen == AppScreen.STATISTICS) {
            loadUserTimeStatistics()
        }
        if (screen == AppScreen.ADMIN_PANEL) {
            loadAdminAnalytics()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    // -------------------------------------------------------------
    // AUTHENTICATION
    // -------------------------------------------------------------
    fun login(identifier: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isActionLoading.value = true
            val result = repository.login(identifier, pass)
            _isActionLoading.value = false
            result.onSuccess {
                _toastMessage.value = "Xush kelibsiz, ${it.firstName}!"
                checkForResumableSession()
                onSuccess()
            }.onFailure {
                _toastMessage.value = it.message ?: "Kirishda xatolik yuz berdi"
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        pass: String,
        passConfirm: String = pass,
        onSuccess: () -> Unit
    ) {
        if (pass != passConfirm) {
            _toastMessage.value = "Kiritilgan parollar bir-biriga mos kelmadi!"
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val result = repository.register(firstName, lastName, username, email, pass)
            _isActionLoading.value = false
            result.onSuccess {
                _toastMessage.value = "Ro'yxatdan muvaffaqiyatli o'tdingiz!"
                onSuccess()
            }.onFailure {
                _toastMessage.value = it.message ?: "Ro'yxatdan o'tishda xatolik"
            }
        }
    }

    fun startPractice(difficulty: String = "ALL", topic: String = "Barcha mavzular") {
        _practiceDifficulty.value = difficulty
        _practiceTopic.value = topic
        _practiceState.value = PracticeModeState.CONFIGURING
        _currentScreen.value = AppScreen.PRACTICE
    }

    fun logout() {
        practiceTimerJob?.cancel()
        timerJob?.cancel()
        repository.logout()
        _currentScreen.value = AppScreen.AUTH
        _toastMessage.value = "Tizimdan chiqildi"
    }

    // -------------------------------------------------------------
    // PRACTICE CONFIGURATION & REAL-TIME TIMER ENGINE
    // -------------------------------------------------------------
    fun setPracticeQuestionCount(count: Int) {
        _practiceQuestionCount.value = count
    }

    fun setPracticeDifficulty(difficulty: String) {
        _practiceDifficulty.value = difficulty
    }

    fun setPracticeDurationMinutes(minutes: Int) {
        _practiceDurationMinutes.value = minutes
    }

    fun setPracticeTopic(topic: String) {
        _practiceTopic.value = topic
    }

    fun checkForResumableSession() {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            val pair = repository.getActivePracticeSession(user.id)
            if (pair != null) {
                _resumableSession.value = pair.first
            } else {
                _resumableSession.value = null
            }
        }
    }

    fun resumeActiveSession() {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            val pair = repository.getActivePracticeSession(user.id)
            _isActionLoading.value = false
            if (pair != null) {
                val (session, questions) = pair
                _currentSessionId.value = session.id
                _practiceQuestions.value = questions
                _practiceQuestionCount.value = session.questionCount
                _practiceDifficulty.value = session.difficulty
                _practiceTopic.value = session.topic
                _practiceIndex.value = session.currentQuestionIndex.coerceIn(0, maxOf(0, questions.size - 1))
                _totalActiveSolvingSeconds.value = session.totalActiveTimeSeconds

                // Load existing answers
                val recordedAnswers = repository.attemptAnswerDao.getAnswersBySession(session.id)
                val answerMap = recordedAnswers.filter { it.selectedAnswer != null }
                    .associate { it.questionId to it.selectedAnswer!! }
                val timeMap = recordedAnswers.associate { it.questionId to it.timeSpentSeconds }
                _practiceUserAnswers.value = answerMap
                _practiceQuestionTimes.value = timeMap

                // Resume session in repository
                repository.resumePracticeSession(session.id, user.id)

                _practiceState.value = PracticeModeState.IN_PROGRESS
                _currentScreen.value = AppScreen.PRACTICE
                _resumableSession.value = null
                startPracticeTimer()
            }
        }
    }

    fun startConfiguredPractice(
        count: Int,
        difficulty: String,
        durationMinutes: Int,
        topic: String
    ) {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val (session, questions) = repository.createPracticeSession(
                    userId = user.id,
                    module = "MATHEMATICS",
                    topic = topic,
                    difficulty = difficulty,
                    questionCount = count
                )

                _currentSessionId.value = session.id
                _practiceQuestions.value = questions
                _practiceIndex.value = 0
                _practiceUserAnswers.value = emptyMap()
                _practiceQuestionTimes.value = emptyMap()
                _totalActiveSolvingSeconds.value = 0
                _currentQuestionElapsedSeconds.value = 0
                _practiceDurationMinutes.value = durationMinutes
                _practiceRemainingSeconds.value = durationMinutes * 60

                _practiceState.value = PracticeModeState.IN_PROGRESS
                _practiceResult.value = null

                startPracticeTimer()
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik yuz berdi"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    private fun startPracticeTimer() {
        practiceTimerJob?.cancel()
        currentQuestionStartTime = System.currentTimeMillis()
        _currentQuestionElapsedSeconds.value = 0

        practiceTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentQuestionElapsedSeconds.value += 1
                _totalActiveSolvingSeconds.value += 1

                if (_practiceDurationMinutes.value > 0) {
                    if (_practiceRemainingSeconds.value > 0) {
                        _practiceRemainingSeconds.value -= 1
                    } else {
                        // Countdown finished -> Auto finish test
                        _toastMessage.value = "Vaqt tugadi! Test yakunlanmoqda..."
                        finishPracticeTest()
                        break
                    }
                }
            }
        }
    }

    fun pausePractice() {
        val user = authState.value.currentUser ?: return
        val sessionId = _currentSessionId.value ?: return
        practiceTimerJob?.cancel()
        _practiceState.value = PracticeModeState.PAUSED
        viewModelScope.launch {
            repository.pausePracticeSession(sessionId, user.id)
        }
    }

    fun resumePractice() {
        val user = authState.value.currentUser ?: return
        val sessionId = _currentSessionId.value ?: return
        _practiceState.value = PracticeModeState.IN_PROGRESS
        viewModelScope.launch {
            repository.resumePracticeSession(sessionId, user.id)
            startPracticeTimer()
        }
    }

    fun selectPracticeAnswer(questionId: Long, answer: String) {
        val user = authState.value.currentUser ?: return
        val sessionId = _currentSessionId.value ?: return

        // Update local map immediately
        val updated = _practiceUserAnswers.value.toMutableMap()
        updated[questionId] = answer
        _practiceUserAnswers.value = updated

        // Track time spent on question
        val timeSpent = _currentQuestionElapsedSeconds.value
        val times = _practiceQuestionTimes.value.toMutableMap()
        times[questionId] = timeSpent
        _practiceQuestionTimes.value = times

        // Save on backend authoritative engine
        viewModelScope.launch {
            repository.recordQuestionAnswer(
                sessionId = sessionId,
                userId = user.id,
                questionId = questionId,
                questionNumber = _practiceIndex.value + 1,
                selectedOption = answer
            )
        }
    }

    fun setPracticeIndex(index: Int) {
        if (index in _practiceQuestions.value.indices) {
            _practiceIndex.value = index
            currentQuestionStartTime = System.currentTimeMillis()
            _currentQuestionElapsedSeconds.value = 0

            val user = authState.value.currentUser
            val sessionId = _currentSessionId.value
            val currentQ = _practiceQuestions.value.getOrNull(index)
            if (user != null && sessionId != null && currentQ != null) {
                viewModelScope.launch {
                    repository.recordQuestionStart(sessionId, user.id, currentQ.id, index + 1)
                }
            }
        }
    }

    fun nextPracticeQuestion() {
        if (_practiceIndex.value < _practiceQuestions.value.size - 1) {
            setPracticeIndex(_practiceIndex.value + 1)
        }
    }

    fun previousPracticeQuestion() {
        if (_practiceIndex.value > 0) {
            setPracticeIndex(_practiceIndex.value - 1)
        }
    }

    fun finishPracticeTest() {
        practiceTimerJob?.cancel()
        val user = authState.value.currentUser ?: return
        val sessionId = _currentSessionId.value ?: return

        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                // Authoritative backend calculation
                val completedSession = repository.finishPracticeSession(sessionId, user.id)
                val questions = _practiceQuestions.value
                val answers = _practiceUserAnswers.value

                val result = PracticeTestResult(
                    totalQuestions = completedSession.questionCount,
                    correctCount = completedSession.correctCount,
                    wrongCount = completedSession.wrongCount,
                    unansweredCount = completedSession.unansweredCount,
                    score = completedSession.score,
                    percentage = completedSession.percentage,
                    certificateGrade = completedSession.certificateGrade,
                    timeSpentSeconds = completedSession.totalActiveTimeSeconds,
                    averageTimePerQuestionSeconds = completedSession.averageTimePerQuestionSeconds,
                    fastestQuestionSeconds = completedSession.fastestQuestionSeconds,
                    slowestQuestionSeconds = completedSession.slowestQuestionSeconds,
                    aiRecommendation = completedSession.aiRecommendation,
                    difficulty = completedSession.difficulty,
                    topic = completedSession.topic,
                    questions = questions,
                    userAnswers = answers,
                    questionTimes = _practiceQuestionTimes.value
                )

                _practiceResult.value = result
                _practiceState.value = PracticeModeState.COMPLETED
                _currentSessionId.value = null
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Natijani hisoblashda xatolik yuz berdi"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun resetPracticeToConfig() {
        practiceTimerJob?.cancel()
        _practiceState.value = PracticeModeState.CONFIGURING
        _practiceResult.value = null
        _practiceUserAnswers.value = emptyMap()
        _practiceQuestionTimes.value = emptyMap()
        _practiceIndex.value = 0
        _currentSessionId.value = null
    }

    fun toggleSaveCurrentPracticeQuestion() {
        val user = authState.value.currentUser ?: return
        val currentQ = _practiceQuestions.value.getOrNull(_practiceIndex.value) ?: return
        viewModelScope.launch {
            repository.toggleSaveQuestion(user.id, currentQ.id)
            _toastMessage.value = "Saqlangan savollar ro'yxati yangilandi"
        }
    }

    // -------------------------------------------------------------
    // GEOMETRY PRACTICE ENGINE (Puza 1 & Puza 2)
    // -------------------------------------------------------------
    fun startGeometryPracticeSetup(difficulty: String = "ALL", topic: String = "Barcha mavzular") {
        _geometryDifficulty.value = difficulty
        _geometryTopic.value = topic
        _geometryState.value = PracticeModeState.CONFIGURING
        _currentScreen.value = AppScreen.GEOMETRY
    }

    fun setGeometryQuestionCount(count: Int) {
        _geometryQuestionCount.value = count
    }

    fun setGeometryDifficulty(difficulty: String) {
        _geometryDifficulty.value = difficulty
    }

    fun setGeometryTopic(topic: String) {
        _geometryTopic.value = topic
    }

    fun setGeometryBookSource(bookSource: String) {
        _geometryBookSource.value = bookSource
    }

    fun setGeometryDurationMinutes(minutes: Int) {
        _geometryDurationMinutes.value = minutes
    }

    fun startConfiguredGeometryPractice(
        count: Int,
        difficulty: String,
        durationMinutes: Int,
        topic: String,
        bookSource: String
    ) {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val (session, questions) = repository.createPracticeSession(
                    userId = user.id,
                    module = "GEOMETRY",
                    topic = topic,
                    difficulty = difficulty,
                    questionCount = count,
                    bookSource = bookSource
                )

                _currentGeometrySessionId.value = session.id
                _geometryQuestions.value = questions
                _geometryIndex.value = 0
                _geometryUserAnswers.value = emptyMap()
                _geometryQuestionTimes.value = emptyMap()
                _geometryTotalActiveSeconds.value = 0
                _geometryQuestionElapsedSeconds.value = 0
                _geometryDurationMinutes.value = durationMinutes
                _geometryRemainingSeconds.value = durationMinutes * 60

                _geometryState.value = PracticeModeState.IN_PROGRESS
                _geometryResult.value = null

                startGeometryTimer()
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik yuz berdi"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    private fun startGeometryTimer() {
        geometryTimerJob?.cancel()
        geometryQuestionStartTime = System.currentTimeMillis()
        _geometryQuestionElapsedSeconds.value = 0

        geometryTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _geometryQuestionElapsedSeconds.value += 1
                _geometryTotalActiveSeconds.value += 1

                if (_geometryDurationMinutes.value > 0) {
                    if (_geometryRemainingSeconds.value > 0) {
                        _geometryRemainingSeconds.value -= 1
                    } else {
                        _toastMessage.value = "Vaqt tugadi! Test yakunlanmoqda..."
                        finishGeometryTest()
                        break
                    }
                }
            }
        }
    }

    fun pauseGeometryPractice() {
        val user = authState.value.currentUser ?: return
        val sessionId = _currentGeometrySessionId.value ?: return
        geometryTimerJob?.cancel()
        _geometryState.value = PracticeModeState.PAUSED
        viewModelScope.launch {
            repository.pausePracticeSession(sessionId, user.id)
        }
    }

    fun resumeGeometryPractice() {
        val user = authState.value.currentUser ?: return
        val sessionId = _currentGeometrySessionId.value ?: return
        _geometryState.value = PracticeModeState.IN_PROGRESS
        viewModelScope.launch {
            repository.resumePracticeSession(sessionId, user.id)
            startGeometryTimer()
        }
    }

    fun selectGeometryAnswer(questionId: Long, answer: String) {
        val user = authState.value.currentUser ?: return
        val sessionId = _currentGeometrySessionId.value ?: return

        val updated = _geometryUserAnswers.value.toMutableMap()
        updated[questionId] = answer
        _geometryUserAnswers.value = updated

        val timeSpent = _geometryQuestionElapsedSeconds.value
        val times = _geometryQuestionTimes.value.toMutableMap()
        times[questionId] = timeSpent
        _geometryQuestionTimes.value = times

        viewModelScope.launch {
            repository.recordQuestionAnswer(
                sessionId = sessionId,
                userId = user.id,
                questionId = questionId,
                questionNumber = _geometryIndex.value + 1,
                selectedOption = answer
            )
        }
    }

    fun setGeometryIndex(index: Int) {
        if (index in _geometryQuestions.value.indices) {
            _geometryIndex.value = index
            geometryQuestionStartTime = System.currentTimeMillis()
            _geometryQuestionElapsedSeconds.value = 0

            val user = authState.value.currentUser
            val sessionId = _currentGeometrySessionId.value
            val currentQ = _geometryQuestions.value.getOrNull(index)
            if (user != null && sessionId != null && currentQ != null) {
                viewModelScope.launch {
                    repository.recordQuestionStart(sessionId, user.id, currentQ.id, index + 1)
                }
            }
        }
    }

    fun nextGeometryQuestion() {
        if (_geometryIndex.value < _geometryQuestions.value.size - 1) {
            setGeometryIndex(_geometryIndex.value + 1)
        }
    }

    fun previousGeometryQuestion() {
        if (_geometryIndex.value > 0) {
            setGeometryIndex(_geometryIndex.value - 1)
        }
    }

    fun finishGeometryTest() {
        geometryTimerJob?.cancel()
        val user = authState.value.currentUser ?: return
        val sessionId = _currentGeometrySessionId.value ?: return

        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val completedSession = repository.finishPracticeSession(sessionId, user.id)
                val questions = _geometryQuestions.value
                val answers = _geometryUserAnswers.value

                val result = PracticeTestResult(
                    totalQuestions = completedSession.questionCount,
                    correctCount = completedSession.correctCount,
                    wrongCount = completedSession.wrongCount,
                    unansweredCount = completedSession.unansweredCount,
                    score = completedSession.score,
                    percentage = completedSession.percentage,
                    certificateGrade = completedSession.certificateGrade,
                    timeSpentSeconds = completedSession.totalActiveTimeSeconds,
                    averageTimePerQuestionSeconds = completedSession.averageTimePerQuestionSeconds,
                    fastestQuestionSeconds = completedSession.fastestQuestionSeconds,
                    slowestQuestionSeconds = completedSession.slowestQuestionSeconds,
                    aiRecommendation = completedSession.aiRecommendation,
                    difficulty = completedSession.difficulty,
                    topic = completedSession.topic,
                    questions = questions,
                    userAnswers = answers,
                    questionTimes = _geometryQuestionTimes.value
                )

                _geometryResult.value = result
                _geometryState.value = PracticeModeState.COMPLETED
                _currentGeometrySessionId.value = null
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Natijani hisoblashda xatolik yuz berdi"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun resetGeometryToConfig() {
        geometryTimerJob?.cancel()
        _geometryState.value = PracticeModeState.CONFIGURING
        _geometryResult.value = null
        _geometryUserAnswers.value = emptyMap()
        _geometryQuestionTimes.value = emptyMap()
        _geometryIndex.value = 0
        _currentGeometrySessionId.value = null
    }

    fun toggleSaveCurrentGeometryQuestion() {
        val user = authState.value.currentUser ?: return
        val currentQ = _geometryQuestions.value.getOrNull(_geometryIndex.value) ?: return
        viewModelScope.launch {
            repository.toggleSaveQuestion(user.id, currentQ.id)
            _toastMessage.value = "Saqlangan savollar ro'yxati yangilandi"
        }
    }

    // -------------------------------------------------------------
    // TEST RUNNER (MILLIY SERTIFIKAT MOCK TEST - 45 TA SAVOL)
    // -------------------------------------------------------------
    fun startTest(template: TestTemplateEntity) {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val (attempt, questions) = repository.startNewTestAttempt(user.id, template)
                _activeAttempt.value = attempt
                _testQuestions.value = questions
                _currentTestQuestionIndex.value = 0
                _testUserAnswers.value = emptyMap()
                _testRemainingSeconds.value = template.durationMinutes * 60
                _currentScreen.value = AppScreen.ACTIVE_TEST
                startMockTestTimer()
            } catch (e: Exception) {
                _toastMessage.value = "Testni boshlashda xatolik: ${e.message}"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    private fun startMockTestTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_testRemainingSeconds.value > 0) {
                delay(1000)
                _testRemainingSeconds.value -= 1
            }
            if (_testRemainingSeconds.value <= 0) {
                _toastMessage.value = "Imtihon vaqti tugadi! Natijangiz hisoblanmoqda..."
                submitTest()
            }
        }
    }

    fun selectTestAnswer(questionId: Long, answer: String) {
        val attempt = _activeAttempt.value ?: return
        val updated = _testUserAnswers.value.toMutableMap()
        updated[questionId] = answer
        _testUserAnswers.value = updated

        viewModelScope.launch {
            repository.saveAnswer(attempt.id, questionId, answer)
        }
    }

    fun setTestQuestionIndex(index: Int) {
        if (index in _testQuestions.value.indices) {
            _currentTestQuestionIndex.value = index
        }
    }

    fun submitTest() {
        timerJob?.cancel()
        val attempt = _activeAttempt.value ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val totalTimeSpent = (150 * 60) - _testRemainingSeconds.value
                val completed = repository.finishTestAttempt(attempt.id, maxOf(0, totalTimeSpent))
                _lastCompletedAttempt.value = completed
                _activeAttempt.value = null
                _currentScreen.value = AppScreen.TEST_RESULT
            } catch (e: Exception) {
                _toastMessage.value = "Testni yakunlashda xatolik: ${e.message}"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun submitActiveTest() = submitTest()

    // -------------------------------------------------------------
    // USER TIME & PROGRESS STATISTICS
    // -------------------------------------------------------------
    fun loadUserTimeStatistics() {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            val stats = repository.getUserTimeStatistics(user.id)
            _userTimeStats.value = stats
        }
    }

    fun loadAdminAnalytics() {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            try {
                val analytics = repository.getAdminTimeAnalytics(user.id)
                _adminAnalytics.value = analytics
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Admin tahlilini yuklashda xatolik"
            }
        }
    }

    // -------------------------------------------------------------
    // DATA PRIVACY & EXPORT (GDPR / O'RQ-547)
    // -------------------------------------------------------------
    fun exportUserData(onExported: (String) -> Unit) {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                val json = repository.exportUserDataJson(user.id)
                onExported(json)
            } catch (e: Exception) {
                _toastMessage.value = "Eksport qilishda xatolik: ${e.message}"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun deleteUserAccount(onDeleted: () -> Unit) {
        val user = authState.value.currentUser ?: return
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = repository.deleteUserAccountAndData(user.id)
            _isActionLoading.value = false
            res.onSuccess {
                _toastMessage.value = "Akkaunt va barcha shaxsiy ma'lumotlar to'liq o'chirildi."
                _currentScreen.value = AppScreen.AUTH
                onDeleted()
            }.onFailure {
                _toastMessage.value = "O'chirishda xatolik: ${it.message}"
            }
        }
    }

    // -------------------------------------------------------------
    // ADMIN DASHBOARD ACTIONS
    // -------------------------------------------------------------
    fun adminDeleteUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminDeleteUser(user.id)
                _toastMessage.value = "Foydalanuvchi o'chirildi"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }

    fun adminToggleBlockUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.adminToggleBlockUser(user.id)
                _toastMessage.value = if (!user.isBlocked) "Foydalanuvchi bloklandi" else "Foydalanuvchi blokdan chiqarildi"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }

    fun adminSaveQuestion(question: QuestionEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.adminSaveQuestion(question)
                _toastMessage.value = "Savol muvaffaqiyatli saqlandi"
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }

    fun adminDeleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            try {
                repository.adminDeleteQuestion(question.id)
                _toastMessage.value = "Savol o'chirildi"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }

    fun adminBulkGenerateQuestions(
        topic: String,
        difficulty: String,
        count: Int,
        module: String = "MATHEMATICS",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val generated = repository.adminBulkGenerateQuestions(module, topic, difficulty, count)
                _toastMessage.value = "$generated ta savol muvaffaqiyatli yaratildi"
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }

    // -------------------------------------------------------------
    // ADMIN JSON IMPORT & QUESTION MANAGEMENT
    // -------------------------------------------------------------
    private val _jsonImportPreview = MutableStateFlow<ImportPreview?>(null)
    val jsonImportPreview: StateFlow<ImportPreview?> = _jsonImportPreview.asStateFlow()

    private val _jsonImportResult = MutableStateFlow<ImportResult?>(null)
    val jsonImportResult: StateFlow<ImportResult?> = _jsonImportResult.asStateFlow()

    private val _isAnalyzingJson = MutableStateFlow(false)
    val isAnalyzingJson: StateFlow<Boolean> = _isAnalyzingJson.asStateFlow()

    fun previewJsonImport(
        jsonText: String,
        module: String,
        difficulty: String,
        conflictPolicy: com.example.data.service.ImportConflictPolicy = com.example.data.service.ImportConflictPolicy.SKIP_DUPLICATES
    ) {
        viewModelScope.launch {
            _isAnalyzingJson.value = true
            _jsonImportResult.value = null
            try {
                val preview = repository.previewJsonImport(jsonText, module, difficulty, conflictPolicy)
                _jsonImportPreview.value = preview
            } catch (e: Exception) {
                _toastMessage.value = "Tahlil qilishda xatolik: ${e.message}"
            } finally {
                _isAnalyzingJson.value = false
            }
        }
    }

    fun executeJsonImport(
        preview: ImportPreview,
        allowPartial: Boolean = false,
        conflictPolicy: com.example.data.service.ImportConflictPolicy = com.example.data.service.ImportConflictPolicy.SKIP_DUPLICATES,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAnalyzingJson.value = true
            try {
                val result = repository.executeJsonImport(preview, allowPartial, conflictPolicy)
                _jsonImportResult.value = result
                _toastMessage.value = result.message
                if (result.success) {
                    _jsonImportPreview.value = null
                    onSuccess()
                }
            } catch (e: Exception) {
                _toastMessage.value = "Import qilishda xatolik: ${e.message}"
            } finally {
                _isAnalyzingJson.value = false
            }
        }
    }

    fun clearJsonImport() {
        _jsonImportPreview.value = null
        _jsonImportResult.value = null
    }

    fun adminArchiveQuestion(questionId: Long) {
        viewModelScope.launch {
            try {
                repository.adminArchiveQuestion(questionId)
                _toastMessage.value = "Savol muvaffaqiyatli arxivlandi"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }

    fun adminUnarchiveQuestion(questionId: Long) {
        viewModelScope.launch {
            try {
                repository.adminUnarchiveQuestion(questionId)
                _toastMessage.value = "Savol arxivdan chiqarildi"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }

    fun adminToggleQuestionPublish(question: QuestionEntity) {
        viewModelScope.launch {
            try {
                val newStatus = if (question.status == "PUBLISHED" || question.status == "APPROVED") "DRAFT" else "PUBLISHED"
                repository.adminUpdateQuestionStatus(question.id, newStatus)
                _toastMessage.value = "Savol holati '$newStatus' ga o'zgartirildi"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Xatolik"
            }
        }
    }
}
