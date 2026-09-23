package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Urgency level for visual alerts during practice and national certificate exams.
 */
enum class TimerUrgencyLevel {
    NORMAL,    // Plenty of time remaining (> 25% or > 5 min)
    WARNING,   // Approaching time limit (<= 25% or <= 5 min)
    CRITICAL,  // Final countdown (<= 10% or <= 60 seconds)
    EXPIRED    // Time has run out (0 seconds remaining)
}

/**
 * Visual presentation style for the timer component.
 */
enum class ExamTimerDisplayMode {
    COMPACT_CHIP, // Clean compact badge with time & icon (ideal for top bars / sticky headers)
    BANNER_CARD,  // Full-width card with progress bar, status, and optional pause controls
    CIRCULAR_RING // Radial circular gauge with centered time readout
}

/**
 * Production-ready state holder for practice exam countdown timers.
 * 
 * Features:
 * - Tracks total and remaining duration.
 * - Dispatches [onTimeExpired] callback strictly once upon timer reaching 0.
 * - Supports start, pause, resume, reset, and addTime.
 * - Automatically computes urgency level, progress fractions, and formatted string.
 */
@Stable
class ExamTimerState(
    initialTotalSeconds: Int,
    initialRemainingSeconds: Int = initialTotalSeconds,
    autoStart: Boolean = true,
    private val onTimeExpired: (() -> Unit)? = null
) {
    var totalSeconds by mutableIntStateOf(initialTotalSeconds.coerceAtLeast(1))
        private set

    var remainingSeconds by mutableIntStateOf(initialRemainingSeconds.coerceIn(0, initialTotalSeconds))
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isPaused by mutableStateOf(false)
        private set

    var isExpired by mutableStateOf(initialRemainingSeconds <= 0)
        private set

    private var timerJob: Job? = null
    private var hasFiredExpiredCallback = initialRemainingSeconds <= 0

    val progress: Float
        get() = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f) else 0f

    val urgencyLevel: TimerUrgencyLevel
        get() = when {
            remainingSeconds <= 0 -> TimerUrgencyLevel.EXPIRED
            remainingSeconds <= 60 || progress <= 0.10f -> TimerUrgencyLevel.CRITICAL
            remainingSeconds <= 300 || progress <= 0.25f -> TimerUrgencyLevel.WARNING
            else -> TimerUrgencyLevel.NORMAL
        }

    val formattedTime: String
        get() = formatSecondsToTime(remainingSeconds)

    val spokenTimeDescription: String
        get() = buildSpokenDescription(remainingSeconds)

    fun start(scope: CoroutineScope) {
        if (remainingSeconds <= 0) {
            isExpired = true
            return
        }
        timerJob?.cancel()
        isRunning = true
        isPaused = false
        isExpired = false
        hasFiredExpiredCallback = false

        timerJob = scope.launch {
            while (isActive && remainingSeconds > 0) {
                delay(1000L)
                if (remainingSeconds > 0) {
                    remainingSeconds -= 1
                    if (remainingSeconds == 0) {
                        isRunning = false
                        isExpired = true
                        if (!hasFiredExpiredCallback) {
                            hasFiredExpiredCallback = true
                            onTimeExpired?.invoke()
                        }
                        break
                    }
                }
            }
        }
    }

    fun pause() {
        if (isRunning && !isExpired) {
            timerJob?.cancel()
            timerJob = null
            isRunning = false
            isPaused = true
        }
    }

    fun resume(scope: CoroutineScope) {
        if (isPaused && remainingSeconds > 0 && !isExpired) {
            start(scope)
        }
    }

    fun reset(newTotalSeconds: Int? = null) {
        timerJob?.cancel()
        timerJob = null
        if (newTotalSeconds != null && newTotalSeconds > 0) {
            totalSeconds = newTotalSeconds
        }
        remainingSeconds = totalSeconds
        isRunning = false
        isPaused = false
        isExpired = false
        hasFiredExpiredCallback = false
    }

    fun addTime(secondsToAdd: Int) {
        if (secondsToAdd <= 0) return
        totalSeconds += secondsToAdd
        remainingSeconds = (remainingSeconds + secondsToAdd).coerceAtMost(totalSeconds)
        if (remainingSeconds > 0) {
            isExpired = false
            hasFiredExpiredCallback = false
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        isRunning = false
        isPaused = false
    }

    init {
        if (autoStart && initialRemainingSeconds > 0) {
            // Note: start must be triggered inside a CoroutineScope via rememberExamTimerState
        }
    }
}

/**
 * Creates and remembers an [ExamTimerState] tied to the Composable lifecycle.
 *
 * @param totalSeconds Total exam duration in seconds (e.g., 9000 for 150 minutes, 3600 for 1 hour).
 * @param remainingSeconds Optional initial remaining seconds (defaults to totalSeconds).
 * @param autoStart Whether timer automatically ticks upon entering the composition.
 * @param onTimeExpired Callback fired on the main thread when remainingSeconds drops to 0.
 */
@Composable
fun rememberExamTimerState(
    totalSeconds: Int,
    remainingSeconds: Int = totalSeconds,
    autoStart: Boolean = true,
    onTimeExpired: () -> Unit = {}
): ExamTimerState {
    val coroutineScope = rememberCoroutineScope()
    val updatedExpiredCallback by rememberUpdatedState(onTimeExpired)

    val timerState = remember(totalSeconds) {
        ExamTimerState(
            initialTotalSeconds = totalSeconds,
            initialRemainingSeconds = remainingSeconds,
            autoStart = autoStart,
            onTimeExpired = { updatedExpiredCallback() }
        )
    }

    LaunchedEffect(timerState, autoStart) {
        if (autoStart && timerState.remainingSeconds > 0 && !timerState.isRunning && !timerState.isPaused) {
            timerState.start(coroutineScope)
        }
    }

    DisposableEffect(timerState) {
        onDispose {
            timerState.stop()
        }
    }

    return timerState
}

/**
 * Helper to format seconds into HH:MM:SS or MM:SS strings.
 */
fun formatSecondsToTime(totalSec: Int): String {
    val sec = totalSec.coerceAtLeast(0)
    val hours = sec / 3600
    val minutes = (sec % 3600) / 60
    val seconds = sec % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun buildSpokenDescription(seconds: Int): String {
    if (seconds <= 0) return "Vaqt tugadi"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val sec = seconds % 60
    return buildString {
        append("Qolgan vaqt: ")
        if (hours > 0) append("$hours soat ")
        if (minutes > 0) append("$minutes daqiqa ")
        if (sec > 0 || (hours == 0 && minutes == 0)) append("$sec soniya")
    }.trim()
}

// =========================================================================
// UI COMPONENT: REUSABLE EXAM TIMER
// =========================================================================

/**
 * Master Reusable Timer Component for Practice and National Certificate Exams.
 *
 * Supports multiple presentation modes:
 * - [ExamTimerDisplayMode.COMPACT_CHIP]: Sleek chip for sticky top bars or headers.
 * - [ExamTimerDisplayMode.BANNER_CARD]: Rich full-width card with progress line, statistics & controls.
 * - [ExamTimerDisplayMode.CIRCULAR_RING]: Radial countdown gauge with center clock readout.
 *
 * @param state The [ExamTimerState] controlling timer execution.
 * @param modifier External styling modifier.
 * @param mode Presentation style.
 * @param title Optional title displayed in banner mode (e.g. "Matematika Milliy Sertifikat").
 * @param subtitle Optional subtitle (e.g. "Javob berildi: 24/45").
 * @param allowPause Whether pause/resume buttons should be exposed (common in practice mode, restricted in real mock exams).
 * @param onTimeExpired Callback fired when time expires.
 */
@Composable
fun ExamTimerComponent(
    state: ExamTimerState,
    modifier: Modifier = Modifier,
    mode: ExamTimerDisplayMode = ExamTimerDisplayMode.BANNER_CARD,
    title: String? = null,
    subtitle: String? = null,
    allowPause: Boolean = false,
    onTimeExpired: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    // Trigger local callback if provided
    LaunchedEffect(state.isExpired) {
        if (state.isExpired && onTimeExpired != null) {
            onTimeExpired()
        }
    }

    when (mode) {
        ExamTimerDisplayMode.COMPACT_CHIP -> {
            ExamTimerCompactChip(
                formattedTime = state.formattedTime,
                urgencyLevel = state.urgencyLevel,
                spokenDescription = state.spokenTimeDescription,
                modifier = modifier
            )
        }
        ExamTimerDisplayMode.BANNER_CARD -> {
            ExamTimerBannerCard(
                formattedTime = state.formattedTime,
                progress = state.progress,
                urgencyLevel = state.urgencyLevel,
                title = title ?: "Imtihon vaqti",
                subtitle = subtitle,
                isPaused = state.isPaused,
                allowPause = allowPause,
                onPauseToggle = {
                    if (state.isPaused) state.resume(scope) else state.pause()
                },
                spokenDescription = state.spokenTimeDescription,
                modifier = modifier
            )
        }
        ExamTimerDisplayMode.CIRCULAR_RING -> {
            ExamTimerCircularRing(
                formattedTime = state.formattedTime,
                progress = state.progress,
                urgencyLevel = state.urgencyLevel,
                spokenDescription = state.spokenTimeDescription,
                modifier = modifier
            )
        }
    }
}

/**
 * Stateless Countdown Timer variant for direct integration with existing ViewModels
 * (where remainingSeconds and totalSeconds are provided by an external StateFlow).
 *
 * @param remainingSeconds Number of seconds left.
 * @param totalSeconds Total duration allocated.
 * @param onTimeExpired Callback invoked once remainingSeconds hits 0.
 */
@Composable
fun ExamCountdownTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    mode: ExamTimerDisplayMode = ExamTimerDisplayMode.COMPACT_CHIP,
    title: String? = null,
    subtitle: String? = null,
    onTimeExpired: () -> Unit = {}
) {
    val updatedExpiredCallback by rememberUpdatedState(onTimeExpired)
    var hasExpiredFired by remember { mutableStateOf(false) }

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds <= 0 && !hasExpiredFired) {
            hasExpiredFired = true
            updatedExpiredCallback()
        } else if (remainingSeconds > 0) {
            hasExpiredFired = false
        }
    }

    val progress = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f) else 0f
    val urgencyLevel = when {
        remainingSeconds <= 0 -> TimerUrgencyLevel.EXPIRED
        remainingSeconds <= 60 || progress <= 0.10f -> TimerUrgencyLevel.CRITICAL
        remainingSeconds <= 300 || progress <= 0.25f -> TimerUrgencyLevel.WARNING
        else -> TimerUrgencyLevel.NORMAL
    }

    val formattedTime = formatSecondsToTime(remainingSeconds)
    val spokenDescription = buildSpokenDescription(remainingSeconds)

    when (mode) {
        ExamTimerDisplayMode.COMPACT_CHIP -> {
            ExamTimerCompactChip(
                formattedTime = formattedTime,
                urgencyLevel = urgencyLevel,
                spokenDescription = spokenDescription,
                modifier = modifier
            )
        }
        ExamTimerDisplayMode.BANNER_CARD -> {
            ExamTimerBannerCard(
                formattedTime = formattedTime,
                progress = progress,
                urgencyLevel = urgencyLevel,
                title = title ?: "Imtihon vaqti",
                subtitle = subtitle,
                isPaused = false,
                allowPause = false,
                onPauseToggle = {},
                spokenDescription = spokenDescription,
                modifier = modifier
            )
        }
        ExamTimerDisplayMode.CIRCULAR_RING -> {
            ExamTimerCircularRing(
                formattedTime = formattedTime,
                progress = progress,
                urgencyLevel = urgencyLevel,
                spokenDescription = spokenDescription,
                modifier = modifier
            )
        }
    }
}

// =========================================================================
// PRESENTATION SUB-COMPONENTS
// =========================================================================

/**
 * Compact pill-shaped countdown chip. Perfect for app bars and sticky question headers.
 */
@Composable
fun ExamTimerCompactChip(
    formattedTime: String,
    urgencyLevel: TimerUrgencyLevel,
    spokenDescription: String,
    modifier: Modifier = Modifier
) {
    val isCritical = urgencyLevel == TimerUrgencyLevel.CRITICAL

    // Subtle pulsing animation when time is critical
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by if (isCritical) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (urgencyLevel) {
            TimerUrgencyLevel.NORMAL -> Blue700
            TimerUrgencyLevel.WARNING -> Amber500
            TimerUrgencyLevel.CRITICAL -> Rose500
            TimerUrgencyLevel.EXPIRED -> Slate600
        },
        animationSpec = tween(400),
        label = "chip_bg"
    )

    Surface(
        modifier = modifier
            .scale(pulseScale)
            .testTag("exam_timer_chip")
            .semantics { contentDescription = spokenDescription },
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = if (isCritical) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (urgencyLevel) {
                    TimerUrgencyLevel.CRITICAL -> Icons.Default.NotificationsActive
                    TimerUrgencyLevel.EXPIRED -> Icons.Default.HourglassBottom
                    else -> Icons.Default.Timer
                },
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formattedTime,
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.testTag("exam_timer_text")
            )
        }
    }
}

/**
 * Rich full-width banner card with smooth animated progress bar and exam stats.
 */
@Composable
fun ExamTimerBannerCard(
    formattedTime: String,
    progress: Float,
    urgencyLevel: TimerUrgencyLevel,
    title: String,
    subtitle: String?,
    isPaused: Boolean,
    allowPause: Boolean,
    onPauseToggle: () -> Unit,
    spokenDescription: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "timer_progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when (urgencyLevel) {
            TimerUrgencyLevel.NORMAL -> Emerald500
            TimerUrgencyLevel.WARNING -> Amber500
            TimerUrgencyLevel.CRITICAL -> Rose500
            TimerUrgencyLevel.EXPIRED -> Slate500
        },
        animationSpec = tween(400),
        label = "progress_bar_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_timer_card")
            .semantics { contentDescription = spokenDescription },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy900),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (allowPause) {
                        IconButton(
                            onClick = onPauseToggle,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag(if (isPaused) "exam_timer_resume_btn" else "exam_timer_pause_btn")
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isPaused) "Davom ettirish" else "Pauza",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Compact clock pill
                    Surface(
                        color = when (urgencyLevel) {
                            TimerUrgencyLevel.NORMAL -> Color(0xFF1E293B)
                            TimerUrgencyLevel.WARNING -> Amber500.copy(alpha = 0.2f)
                            TimerUrgencyLevel.CRITICAL -> Rose500.copy(alpha = 0.25f)
                            TimerUrgencyLevel.EXPIRED -> Slate700
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (urgencyLevel) {
                                TimerUrgencyLevel.NORMAL -> Slate700
                                TimerUrgencyLevel.WARNING -> Amber500
                                TimerUrgencyLevel.CRITICAL -> Rose500
                                TimerUrgencyLevel.EXPIRED -> Slate500
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (urgencyLevel) {
                                    TimerUrgencyLevel.CRITICAL -> Icons.Default.Warning
                                    TimerUrgencyLevel.EXPIRED -> Icons.Default.HourglassBottom
                                    else -> Icons.Default.Timer
                                },
                                contentDescription = null,
                                tint = when (urgencyLevel) {
                                    TimerUrgencyLevel.NORMAL -> PureWhite
                                    TimerUrgencyLevel.WARNING -> Amber400
                                    TimerUrgencyLevel.CRITICAL -> Rose500
                                    TimerUrgencyLevel.EXPIRED -> Slate400
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPaused) "$formattedTime (Pauza)" else formattedTime,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("exam_timer_display")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Linear Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF334155))
                    .testTag("exam_timer_progress")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressColor)
                )
            }

            // Bottom Alert Text if Urgent or Expired
            AnimatedVisibility(
                visible = urgencyLevel == TimerUrgencyLevel.WARNING || urgencyLevel == TimerUrgencyLevel.CRITICAL || urgencyLevel == TimerUrgencyLevel.EXPIRED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val alertText = when (urgencyLevel) {
                        TimerUrgencyLevel.WARNING -> "Diqqat: Imtihon vaqti yakunlanmoqda (25% dan kam qoldi)."
                        TimerUrgencyLevel.CRITICAL -> "Shoshiling! Oxirgi daqiqalar davom etmoqda."
                        TimerUrgencyLevel.EXPIRED -> "Vaqt tugadi! Javoblaringiz hisoblandi."
                        else -> ""
                    }
                    Text(
                        text = alertText,
                        color = when (urgencyLevel) {
                            TimerUrgencyLevel.WARNING -> Amber400
                            TimerUrgencyLevel.CRITICAL -> Rose500
                            TimerUrgencyLevel.EXPIRED -> Slate400
                            else -> Slate400
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val percentLeft = (progress * 100).toInt()
                    Text(
                        text = "$percentLeft% qoldi",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Circular radial gauge countdown timer with smooth animated sweep stroke.
 */
@Composable
fun ExamTimerCircularRing(
    formattedTime: String,
    progress: Float,
    urgencyLevel: TimerUrgencyLevel,
    spokenDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    strokeWidth: Dp = 10.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "circular_timer_progress"
    )

    val ringColor by animateColorAsState(
        targetValue = when (urgencyLevel) {
            TimerUrgencyLevel.NORMAL -> Blue700
            TimerUrgencyLevel.WARNING -> Amber500
            TimerUrgencyLevel.CRITICAL -> Rose500
            TimerUrgencyLevel.EXPIRED -> Slate500
        },
        animationSpec = tween(400),
        label = "circular_ring_color"
    )

    val trackColor = Slate200

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("exam_timer_circular")
            .semantics { contentDescription = spokenDescription }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            // Track background circle
            drawCircle(
                color = trackColor,
                radius = (size.toPx() - strokePx) / 2f,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated progress arc (starting at 12 o'clock / -90 degrees)
            val sweepAngle = animatedProgress * 360f
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Inner Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedTime,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate900,
                textAlign = TextAlign.Center
            )
            Text(
                text = when (urgencyLevel) {
                    TimerUrgencyLevel.EXPIRED -> "Tugadi"
                    TimerUrgencyLevel.CRITICAL -> "Oxirgi daqiqa"
                    TimerUrgencyLevel.WARNING -> "Oz qoldi"
                    TimerUrgencyLevel.NORMAL -> "Qolgan vaqt"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = when (urgencyLevel) {
                    TimerUrgencyLevel.CRITICAL -> Rose600
                    TimerUrgencyLevel.WARNING -> Amber600
                    else -> Slate500
                }
            )
        }
    }
}
