package com.example.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import com.example.ui.components.ExamCountdownTimer
import com.example.ui.components.ExamTimerDisplayMode
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestTemplateEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.OptionButton
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import org.json.JSONObject

// ====================================================================
// 1. TEST LIST SCREEN
// ====================================================================
@Composable
fun TestListScreen(
    viewModel: MainViewModel
) {
    val templates by viewModel.repository.getAllTestTemplates().collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "MILLIY SERTIFIKAT VA MOCK TESTLAR",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Rasmiy imtihon qoidalari asosida o'z bilimingizni sinab ko'ring",
            fontSize = 12.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Warning banner: No results shown during test
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = Blue700,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Imtihon shartlari: Test davomida to'g'ri/noto'g'ri javoblar ko'rsatilmaydi. To'liq natija va tahlil test yakunlanganidan keyin beriladi.",
                    fontSize = 12.sp,
                    color = Blue900,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (template in templates) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("template_${template.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (template.isNationalCertMode) Color(0xFFFEF3C7) else Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (template.isNationalCertMode) "RASMIY FORMAT" else "MASHQ TESTI",
                                    color = if (template.isNationalCertMode) Amber600 else Blue700,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${template.durationMinutes} daqiqa",
                                    fontSize = 12.sp,
                                    color = Slate600,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Mavzu: ${template.topic} • Savollar soni: ${template.questionCount} ta",
                            fontSize = 12.sp,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.startTest(template) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_test_${template.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (template.isNationalCertMode) Blue700 else Navy900
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PureWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "TESTNI BOSHLASH",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ====================================================================
// 2. ACTIVE TEST SCREEN (STRICT RULE: NO RESULT/SCORE SHOWN BEFORE COMPLETION)
// ====================================================================
@Composable
fun ActiveTestScreen(
    viewModel: MainViewModel
) {
    val attempt by viewModel.activeAttempt.collectAsState()
    val questions by viewModel.testQuestions.collectAsState()
    val currentIndex by viewModel.currentTestQuestionIndex.collectAsState()
    val userAnswers by viewModel.testUserAnswers.collectAsState()
    val remainingSeconds by viewModel.testRemainingSeconds.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }

    val currentQ = questions.getOrNull(currentIndex)
    val scrollState = rememberScrollState()

    if (showSubmitDialog) {
        val answeredCount = userAnswers.size
        val totalCount = questions.size
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = {
                Text(
                    text = "Testni yakunlash",
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = "Siz $totalCount ta savoldan $answeredCount tasiga javob berdingiz.\n\nTestni haqiqatan ham yakunlamoqchimisiz? Natija hisoblanadi va saqlanadi.",
                    color = Slate700,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitActiveTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    modifier = Modifier.testTag("confirm_submit_test_button")
                ) {
                    Text("Ha, yakunlash", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Davom etish", color = Slate700)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // TOP REUSABLE TIMER & PROGRESS BANNER
        ExamCountdownTimer(
            remainingSeconds = remainingSeconds,
            totalSeconds = 150 * 60,
            mode = ExamTimerDisplayMode.BANNER_CARD,
            title = attempt?.testTitle ?: "Milliy Sertifikat Imtihoni",
            subtitle = "Javob berildi: ${userAnswers.size} / ${questions.size}",
            onTimeExpired = {
                // Auto submit when time expires
                viewModel.submitActiveTest()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // QUESTION NAVIGATOR PALETTE (1..45 chips)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(questions) { index, q ->
                val isAnswered = userAnswers.containsKey(q.id)
                val isCurrent = index == currentIndex

                val chipBg = when {
                    isCurrent -> Amber500
                    isAnswered -> Blue700
                    else -> Slate200
                }
                val chipFg = when {
                    isCurrent || isAnswered -> PureWhite
                    else -> Slate700
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .clickable { viewModel.setTestQuestionIndex(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = chipFg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (currentQ != null) {
            // QUESTION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_question_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentIndex + 1}-SAVOL",
                            color = Blue700,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        DifficultyBadge(difficulty = currentQ.difficulty)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Mavzu: ${currentQ.topic}",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Question text
                    Text(
                        text = currentQ.questionText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            lineHeight = 26.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 OPTIONS (A, B, C, D) - IN ACTIVE TEST: NO CORRECTNESS REVEALED
            val selected = userAnswers[currentQ.id]

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OptionButton(
                    letter = "A",
                    text = currentQ.optionA,
                    isSelected = selected == "A",
                    onClick = { viewModel.selectTestAnswer(currentQ.id, "A") }
                )
                OptionButton(
                    letter = "B",
                    text = currentQ.optionB,
                    isSelected = selected == "B",
                    onClick = { viewModel.selectTestAnswer(currentQ.id, "B") }
                )
                OptionButton(
                    letter = "C",
                    text = currentQ.optionC,
                    isSelected = selected == "C",
                    onClick = { viewModel.selectTestAnswer(currentQ.id, "C") }
                )
                OptionButton(
                    letter = "D",
                    text = currentQ.optionD,
                    isSelected = selected == "D",
                    onClick = { viewModel.selectTestAnswer(currentQ.id, "D") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // NAVIGATION BUTTONS: OLDINGI / KEYINGI / TESTNI YAKUNLASH
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.setTestQuestionIndex(currentIndex - 1) },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Oldingi")
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (currentIndex < questions.size - 1) {
                    Button(
                        onClick = { viewModel.setTestQuestionIndex(currentIndex + 1) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Keyingi")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = { showSubmitDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Testni yakunlash", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Always present quick submit button
            OutlinedButton(
                onClick = { showSubmitDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_test_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Testni yakunlash va natijani ko'rish", color = Slate700)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ====================================================================
// 3. TEST RESULT SCREEN (COMPREHENSIVE POST-TEST ANALYSIS)
// ====================================================================
@Composable
fun TestResultScreen(
    viewModel: MainViewModel
) {
    val result = viewModel.lastCompletedAttempt.collectAsState().value
    val scrollState = rememberScrollState()

    val level = viewModel.repository.calculateUserLevel(
        avgPercent = result?.percentage ?: 0f,
        completedTests = 1,
        bestScore = result?.score ?: 0
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Score Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Navy900)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(level.colorHex),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = level.levelName,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${result?.score ?: 0}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Amber400
                    )
                )
                Text(
                    text = "Umumiy to'plangan ball",
                    color = Slate400,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Aniqlik: ${String.format("%.1f", result?.percentage ?: 0f)}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats breakdown: Correct, Wrong, Unanswered, Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result?.correctCount ?: 0}",
                            color = Emerald500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(text = "To'g'ri", color = Slate400, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result?.wrongCount ?: 0}",
                            color = Rose500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(text = "Noto'g'ri", color = Slate400, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result?.unansweredCount ?: 0}",
                            color = Slate400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(text = "Javobsiz", color = Slate400, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val spentMins = (result?.timeSpentSeconds ?: 0) / 60
                        Text(
                            text = "${spentMins} daq",
                            color = Blue100,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(text = "Ketgan vaqt", color = Slate400, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // WEAK TOPICS / RECOMMENDATION CARD
        if (!result?.weakTopicsRecommendation.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Amber600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tavsiyalar va kamchiliklar tahlili",
                            fontWeight = FontWeight.Bold,
                            color = Amber600,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result?.weakTopicsRecommendation ?: "",
                        fontSize = 13.sp,
                        color = Slate900,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // TOPIC BREAKDOWN ACCURACY LIST
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Mavzular bo'yicha o'zlashtirish:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                val json = try {
                    JSONObject(result?.topicBreakdownJson ?: "{}")
                } catch (e: Exception) {
                    JSONObject()
                }

                if (json.length() == 0) {
                    Text(
                        text = "Mavzular bo'yicha to'liq taqsimot hisoblanmadi.",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                } else {
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val topicKey = keys.next()
                        val percentStr = json.optString(topicKey, "0%")
                        val percentFloat = percentStr.replace("%", "").toFloatOrNull() ?: 0f

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = topicKey,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate800
                                )
                                Text(
                                    text = percentStr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentFloat >= 70f) Emerald600 else Amber600
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percentFloat / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (percentFloat >= 70f) Emerald600 else Amber600,
                                trackColor = Slate100
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ACTIONS: VIEW ERRORS / RETRY / HOME
        Button(
            onClick = { viewModel.navigateTo(AppScreen.ERRORS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("review_errors_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Rose600),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = PureWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text("XATOLARNI TAHLIL QILISH", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.HOME) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("back_home_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bosh sahifaga qaytish")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
