package com.example

import com.example.ui.components.ExamTimerState
import com.example.ui.components.TimerUrgencyLevel
import com.example.ui.components.formatSecondsToTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@OptIn(ExperimentalCoroutinesApi::class)
class ExamTimerComponentTest {

    @Test
    fun `formatSecondsToTime formats correctly for MM-SS and HH-MM-SS`() {
        assertEquals("00:00", formatSecondsToTime(0))
        assertEquals("00:45", formatSecondsToTime(45))
        assertEquals("01:00", formatSecondsToTime(60))
        assertEquals("15:30", formatSecondsToTime(930))
        assertEquals("59:59", formatSecondsToTime(3599))
        assertEquals("01:00:00", formatSecondsToTime(3600))
        assertEquals("02:30:15", formatSecondsToTime(9015))
    }

    @Test
    fun `urgencyLevel reflects remaining time thresholds accurately`() {
        // Total: 1000s
        val normalState = ExamTimerState(initialTotalSeconds = 1000, initialRemainingSeconds = 800)
        assertEquals(TimerUrgencyLevel.NORMAL, normalState.urgencyLevel)

        // 25% remaining (250s) -> WARNING
        val warningState = ExamTimerState(initialTotalSeconds = 1000, initialRemainingSeconds = 250)
        assertEquals(TimerUrgencyLevel.WARNING, warningState.urgencyLevel)

        // 10% remaining (100s) -> CRITICAL
        val criticalState = ExamTimerState(initialTotalSeconds = 1000, initialRemainingSeconds = 90)
        assertEquals(TimerUrgencyLevel.CRITICAL, criticalState.urgencyLevel)

        // <= 60 seconds is always CRITICAL even if ratio is large
        val sixtySecondsState = ExamTimerState(initialTotalSeconds = 120, initialRemainingSeconds = 55)
        assertEquals(TimerUrgencyLevel.CRITICAL, sixtySecondsState.urgencyLevel)

        // 0 seconds remaining -> EXPIRED
        val expiredState = ExamTimerState(initialTotalSeconds = 1000, initialRemainingSeconds = 0)
        assertEquals(TimerUrgencyLevel.EXPIRED, expiredState.urgencyLevel)
        assertTrue(expiredState.isExpired)
    }

    @Test
    fun `ExamTimerState ticks down and triggers onTimeExpired callback exactly once`() = runTest {
        var callbackFiredCount = 0
        val testScope = TestScope(StandardTestDispatcher(testScheduler))

        val timerState = ExamTimerState(
            initialTotalSeconds = 3,
            initialRemainingSeconds = 3,
            autoStart = false,
            onTimeExpired = {
                callbackFiredCount++
            }
        )

        assertEquals(3, timerState.remainingSeconds)
        assertFalse(timerState.isRunning)
        assertFalse(timerState.isExpired)

        timerState.start(testScope)
        assertTrue(timerState.isRunning)

        // Advance 1 second
        testScope.advanceTimeBy(1001)
        assertEquals(2, timerState.remainingSeconds)
        assertEquals(0, callbackFiredCount)

        // Advance 1 second
        testScope.advanceTimeBy(1001)
        assertEquals(1, timerState.remainingSeconds)
        assertEquals(0, callbackFiredCount)

        // Advance to 0
        testScope.advanceTimeBy(1001)
        assertEquals(0, timerState.remainingSeconds)
        assertTrue(timerState.isExpired)
        assertFalse(timerState.isRunning)
        assertEquals(1, callbackFiredCount)

        // Advance further - callback should NOT fire again
        testScope.advanceTimeBy(3000)
        assertEquals(0, timerState.remainingSeconds)
        assertEquals(1, callbackFiredCount)
    }

    @Test
    fun `ExamTimerState pause, resume, reset, and addTime behave predictably`() = runTest {
        val testScope = TestScope(StandardTestDispatcher(testScheduler))

        val timerState = ExamTimerState(
            initialTotalSeconds = 100,
            initialRemainingSeconds = 100,
            autoStart = false
        )

        timerState.start(testScope)
        testScope.advanceTimeBy(2005)
        assertEquals(98, timerState.remainingSeconds)

        // Pause
        timerState.pause()
        assertTrue(timerState.isPaused)
        assertFalse(timerState.isRunning)

        // Time should not advance while paused
        testScope.advanceTimeBy(5000)
        assertEquals(98, timerState.remainingSeconds)

        // Resume
        timerState.resume(testScope)
        assertTrue(timerState.isRunning)
        assertFalse(timerState.isPaused)
        testScope.advanceTimeBy(1005)
        assertEquals(97, timerState.remainingSeconds)

        // Add 30 seconds
        timerState.addTime(30)
        assertEquals(127, timerState.remainingSeconds)
        assertEquals(130, timerState.totalSeconds)

        // Reset
        timerState.reset(60)
        assertEquals(60, timerState.remainingSeconds)
        assertEquals(60, timerState.totalSeconds)
        assertFalse(timerState.isRunning)
        assertFalse(timerState.isPaused)
        assertFalse(timerState.isExpired)
    }
}
