package com.example.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.PracticeModeState
import com.example.ui.PracticeTestResult
import com.example.ui.components.DifficultyBadge
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun PracticeScreen(
    viewModel: MainViewModel
) {
    val practiceState by viewModel.practiceState.collectAsState()

    when (practiceState) {
        PracticeModeState.CONFIGURING -> {
            PracticeConfigView(viewModel = viewModel)
        }
        PracticeModeState.IN_PROGRESS, PracticeModeState.PAUSED -> {
            PracticeActiveView(viewModel = viewModel)
        }
        PracticeModeState.COMPLETED -> {
            PracticeResultView(viewModel = viewModel)
        }
    }
}

// =========================================================================
// 1. CONFIGURATION VIEW (Practice Setup - Matematika bo'yicha mashq)
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticeConfigView(
    viewModel: MainViewModel
) {
    val count by viewModel.practiceQuestionCount.collectAsState()
    val difficulty by viewModel.practiceDifficulty.collectAsState()
    val durationMinutes by viewModel.practiceDurationMinutes.collectAsState()
    val topic by viewModel.practiceTopic.collectAsState()
    val isLoading by viewModel.isActionLoading.collectAsState()

    val countOptions = listOf(5, 10, 15, 20, 30, 45, 50, 100)

    val difficultyList = listOf(
        DifficultyOption("EASY", "OSON", "Formulalar va standart usullar asosidagi masalalar.", Color(0xFF10B981)),
        DifficultyOption("MEDIUM", "O'RTA", "Bir necha bosqichli hisoblash va mantiqiy fikrlash.", Color(0xFFF59E0B)),
        DifficultyOption("HARD", "QIYIN", "Kombinatsiyalangan masalalar, nostandart yondashuv.", Color(0xFFEF4444)),
        DifficultyOption("VERY_HARD", "JUDA QIYIN", "Olimpiada va murakkab parametrli masalalar.", Color(0xFF8B5CF6)),
        DifficultyOption("ALL", "BARCHASI (ARALASH)", "Turli xil qiyinlikdagi saralangan masalalar to'plami.", Color(0xFF2563EB))
    )

    val durationOptions = listOf(
        0 to "Cheksiz (Vaqtsiz)",
        5 to "5 daqiqa",
        10 to "10 daqiqa",
        15 to "15 daqiqa",
        20 to "20 daqiqa",
        30 to "30 daqiqa",
        45 to "45 daqiqa",
        60 to "60 daqiqa"
    )

    val topicOptions = listOf(
        "Barcha mavzular",
        "Sonlar va hisoblashlar",
        "Tenglamalar",
        "Tengsizliklar",
        "Arifmetik va geometrik progressiya",
        "Funksiyalar va grafiklar",
        "Ko'rsatkichli va logarifmik ifodalar",
        "Trigonometriya",
        "Hosila va uning tatbiqlari",
        "Integrallar",
        "To'plamlar va kombinatorika",
        "Geometriya"
    )

    var topicDropdownOpen by remember { mutableStateOf(false) }
    var showCustomCountDialog by remember { mutableStateOf(false) }
    var customCountInput by remember { mutableStateOf("") }
    var customCountError by remember { mutableStateOf<String?>(null) }

    if (showCustomCountDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCountDialog = false },
            title = {
                Text(
                    text = "Boshqa misollar sonini kiriting",
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "1 dan 100 gacha ixtiyoriy son kiriting:",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customCountInput,
                        onValueChange = {
                            customCountInput = it.filter { ch -> ch.isDigit() }
                            customCountError = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("Masalan: 35") },
                        isError = customCountError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (customCountError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = customCountError ?: "",
                            color = Rose500,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customCountInput.toIntOrNull()
                        if (num != null && num in 1..100) {
                            viewModel.setPracticeQuestionCount(num)
                            showCustomCountDialog = false
                        } else {
                            customCountError = "Faqat 1 dan 100 gacha son kiriting"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700)
                ) {
                    Text("Tasdiqlash", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCountDialog = false }) {
                    Text("Bekor qilish", color = Slate600)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sarlavha (Section 1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier
                    .background(PureWhite, CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Orqaga", tint = Slate700)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Matematika bo'yicha mashq",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Daraja va misollar sonini tanlang",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        }

        // Section 2: DARAJANI TANLASH
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. DARAJANI TANLANG",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    difficultyList.forEach { item ->
                        val isSelected = difficulty == item.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) item.color.copy(alpha = 0.08f) else Slate100.copy(alpha = 0.5f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) item.color else Slate200,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setPracticeDifficulty(item.code) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) item.color else Slate900
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    color = Slate600,
                                    lineHeight = 16.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 3: MISOLLAR SONI
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "2. MISOLLAR SONI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$count ta misol",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue700
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    countOptions.forEach { optCount ->
                        val isSelected = count == optCount
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Blue700 else Slate100)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Blue700 else Slate200,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setPracticeQuestionCount(optCount) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$optCount ta",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PureWhite else Slate700
                            )
                        }
                    }

                    // Custom count button
                    val isCustom = count !in countOptions
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCustom) Blue700 else Slate100)
                            .border(
                                width = 1.dp,
                                color = if (isCustom) Blue700 else Slate200,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                customCountInput = count.toString()
                                showCustomCountDialog = true
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = if (isCustom) PureWhite else Slate600, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCustom) "$count ta (Boshqa)" else "Boshqa son",
                                fontSize = 13.sp,
                                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCustom) PureWhite else Slate700
                            )
                        }
                    }
                }
            }
        }

        // Section 4: VAQT REJIMI
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = Amber500, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "3. VAQT REJIMI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (durationMinutes == 0) "Cheksiz" else "$durationMinutes daqiqa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durationOptions.forEach { (mins, label) ->
                        val isSelected = durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Amber500 else Slate100)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Amber500 else Slate200,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setPracticeDurationMinutes(mins) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PureWhite else Slate700
                            )
                        }
                    }
                }
            }
        }

        // Section 5: MAVZU
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. MAVZU",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                        .clickable { topicDropdownOpen = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = topic,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate900
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Slate600
                        )
                    }

                    DropdownMenu(
                        expanded = topicDropdownOpen,
                        onDismissRequest = { topicDropdownOpen = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        topicOptions.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = t,
                                        fontWeight = if (t == topic) FontWeight.Bold else FontWeight.Normal,
                                        color = if (t == topic) Blue700 else Slate900
                                    )
                                },
                                onClick = {
                                    viewModel.setPracticeTopic(t)
                                    topicDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Boshlash tugmasi
        Button(
            onClick = {
                viewModel.startConfiguredPractice(
                    count = count,
                    difficulty = difficulty,
                    durationMinutes = durationMinutes,
                    topic = topic
                )
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue700),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_configured_practice_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PureWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoading) "Savollar yuklanmoqda..." else "MASHQNI BOSHLASH ($count ta misol)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class DifficultyOption(
    val code: String,
    val title: String,
    val description: String,
    val color: Color
)

// =========================================================================
// 2. ACTIVE PRACTICE VIEW (Real-time timer & Backend engine)
// =========================================================================
@Composable
private fun PracticeActiveView(
    viewModel: MainViewModel
) {
    val questions by viewModel.practiceQuestions.collectAsState()
    val currentIndex by viewModel.practiceIndex.collectAsState()
    val userAnswers by viewModel.practiceUserAnswers.collectAsState()
    val practiceState by viewModel.practiceState.collectAsState()
    val currentQuestionElapsed by viewModel.currentQuestionElapsedSeconds.collectAsState()
    val totalActiveSeconds by viewModel.totalActiveSolvingSeconds.collectAsState()
    val remainingSeconds by viewModel.practiceRemainingSeconds.collectAsState()
    val durationMinutes by viewModel.practiceDurationMinutes.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val user = authState.currentUser

    val currentQ = questions.getOrNull(currentIndex)
    var showSolution by remember { mutableStateOf(false) }
    var showFinishConfirmDialog by remember { mutableStateOf(false) }

    val isPaused = practiceState == PracticeModeState.PAUSED

    val isSaved by if (user != null && currentQ != null) {
        viewModel.repository.isQuestionSaved(user.id, currentQ.id).collectAsState(initial = false)
    } else {
        remember { mutableStateOf(false) }
    }

    // Confirmation dialog before completing
    if (showFinishConfirmDialog) {
        val answeredCount = userAnswers.size
        val unansweredCount = questions.size - answeredCount
        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            title = {
                Text(
                    text = "Testni yakunlaysizmi?",
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = if (unansweredCount > 0) {
                        "Sizda hali $unansweredCount ta javob berilmagan savol bor. Testni hozir yakunlab, natijalarni ko'rishni xohlaysizmi?"
                    } else {
                        "Barcha $answeredCount ta savolga javob berdingiz. Testni yakunlab, natijalarni chiqaramizmi?"
                    },
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmDialog = false
                        viewModel.finishPracticeTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Ha, yakunlash", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text("Davom etish", color = Slate600)
                }
            }
        )
    }

    // Pauza Overlay Dialog (Section 13, 14 Requirement)
    if (isPaused) {
        AlertDialog(
            onDismissRequest = { /* Must click resume */ },
            icon = {
                Icon(Icons.Default.Pause, contentDescription = null, tint = Amber500, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = "Mashg'ulot to'xtatildi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Slate900
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Vaqt hisobi to'xtatildi. Faol yechish vaqti hisoblanmaydi.",
                        fontSize = 14.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Jami faol vaqt: ${formatTime(totalActiveSeconds)}",
                        fontWeight = FontWeight.Bold,
                        color = Blue700,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resumePractice() },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Davom ettirish", fontWeight = FontWeight.Bold, color = PureWhite)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Top Bar: Timers, Pause, Question Counter
        Surface(
            color = PureWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back / Setup button
                    IconButton(
                        onClick = { viewModel.resetPracticeToConfig() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Sozlamalarga qaytish", tint = Slate600)
                    }

                    // Question timer & Total timer badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Savol vaqti (real-time)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Blue700.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Blue700, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Savol: ${formatTime(currentQuestionElapsed)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue700,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Jami faol vaqt
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Amber500.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Amber500, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (durationMinutes > 0) {
                                    formatTime(remainingSeconds)
                                } else {
                                    formatTime(totalActiveSeconds)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber500,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Pauza button
                    IconButton(
                        onClick = { viewModel.pausePractice() },
                        modifier = Modifier
                            .background(Slate100, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pauza", tint = Slate700, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Indicator
                val progress = if (questions.isNotEmpty()) (currentIndex + 1).toFloat() / questions.size.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Blue700,
                    trackColor = Slate200,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fast question selector chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    questions.forEachIndexed { idx, q ->
                        val isCurrent = idx == currentIndex
                        val isAnswered = userAnswers.containsKey(q.id)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> Blue700
                                        isAnswered -> Emerald600
                                        else -> Slate200
                                    }
                                )
                                .clickable {
                                    showSolution = false
                                    viewModel.setPracticeIndex(idx)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent || isAnswered) PureWhite else Slate700
                            )
                        }
                    }
                }
            }
        }

        // Active Question Content
        if (currentQ != null) {
            val selectedOption = userAnswers[currentQ.id]

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Topic & Difficulty Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DifficultyBadge(difficulty = currentQ.difficulty)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentQ.topic,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleSaveCurrentPracticeQuestion() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Saqlash",
                            tint = if (isSaved) Amber500 else Slate400
                        )
                    }
                }

                // Question Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Savol ${currentIndex + 1} / ${questions.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue700
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQ.questionText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Options A, B, C, D
                val options = listOf(
                    "A" to currentQ.optionA,
                    "B" to currentQ.optionB,
                    "C" to currentQ.optionC,
                    "D" to currentQ.optionD
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEach { (letter, optText) ->
                        val isSelected = selectedOption == letter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Blue100.copy(alpha = 0.5f) else PureWhite)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Blue700 else Slate200,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.selectPracticeAnswer(currentQ.id, letter)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Blue700 else Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else Slate700
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = optText,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = Slate900
                            )
                        }
                    }
                }

                // Optional Hint / Solution toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showSolution = !showSolution }) {
                        Icon(
                            if (showSolution) Icons.Default.VisibilityOff else Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showSolution) "Yechimni yashirish" else "Yechimni ko'rish",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber500
                        )
                    }
                }

                if (showSolution) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "To'g'ri javob: ${currentQ.correctAnswer}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Emerald600
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 12.sp,
                                color = Slate700
                            )
                            if (currentQ.solutionSteps.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentQ.solutionSteps,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        val isLastQuestion = currentIndex == questions.size - 1

        Surface(
            color = PureWhite,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // "Oldingi" Button
                OutlinedButton(
                    onClick = {
                        showSolution = false
                        viewModel.previousPracticeQuestion()
                    },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Oldingi", fontWeight = FontWeight.Bold)
                }

                // If not last question -> "Keyingi"
                // If last question -> "Testni yakunlash"
                if (!isLastQuestion) {
                    Button(
                        onClick = {
                            showSolution = false
                            viewModel.nextPracticeQuestion()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("next_practice_question_button")
                    ) {
                        Text("Keyingi", fontWeight = FontWeight.Bold, color = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            val unanswered = questions.size - userAnswers.size
                            if (unanswered > 0) {
                                showFinishConfirmDialog = true
                            } else {
                                viewModel.finishPracticeTest()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("finish_practice_test_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testni yakunlash", fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. COMPLETED RESULT VIEW (Natijalar, Vaqt Tahlili, AI Tavsiya va Yechimlar)
// =========================================================================
@Composable
private fun PracticeResultView(
    viewModel: MainViewModel
) {
    val result by viewModel.practiceResult.collectAsState()
    var showReviewList by remember { mutableStateOf(true) }

    val res = result ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Top Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Navy900),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MASHQ NATIJALARI",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue100
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Certificate Grade Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            when (res.certificateGrade) {
                                "A+" -> Emerald600
                                "A" -> Blue700
                                "B+", "B" -> Amber500
                                else -> Rose500
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = res.certificateGrade,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${res.correctCount} / ${res.totalQuestions} ta to'g'ri",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                Text(
                    text = "Umumiy ko'rsatkich: ${String.format("%.1f", res.percentage)}% • To'plangan ball: ${res.score}",
                    fontSize = 14.sp,
                    color = Blue100
                )
            }
        }

        // 3 Stats Boxes (To'g'ri, Noto'g'ri, Belgilanmagan)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "To'g'ri", fontSize = 11.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${res.correctCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Noto'g'ri", fontSize = 11.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${res.wrongCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Rose500)
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "O'tkazilgan", fontSize = 11.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${res.unansweredCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate600)
                }
            }
        }

        // VAQT TAHLILI (Section 17 Requirement)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Blue700, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vaqt Tahlili (Time Analytics)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Jami faol vaqt", fontSize = 11.sp, color = Slate600)
                        Text(text = formatTime(res.timeSpentSeconds), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    Column {
                        Text(text = "O'rtacha har bir savolga", fontSize = 11.sp, color = Slate600)
                        Text(text = "${String.format("%.0f", res.averageTimePerQuestionSeconds)} soniya", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Blue700)
                    }
                    Column {
                        Text(text = "Eng tez yechilgan", fontSize = 11.sp, color = Slate600)
                        Text(text = "${res.fastestQuestionSeconds} s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                    }
                    Column {
                        Text(text = "Eng sekin yechilgan", fontSize = 11.sp, color = Slate600)
                        Text(text = "${res.slowestQuestionSeconds} s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Rose500)
                    }
                }
            }
        }

        // AI / Pedagogik Tavsiya Kartochkasi (Section 36)
        if (res.aiRecommendation.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Murabbiy Tavsiyasi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.aiRecommendation,
                            fontSize = 13.sp,
                            color = Slate900,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Action Buttons: New Test & Home
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.resetPracticeToConfig() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Qayta ishlash", fontWeight = FontWeight.Bold, color = PureWhite)
            }

            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Bosh sahifa", fontWeight = FontWeight.Bold, color = Slate700)
            }
        }

        // Savollar va Yechimlar Tahlili Accordion
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReviewList = !showReviewList },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Savollar va Yechimlar Tahlili",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Icon(
                        if (showReviewList) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Slate600
                    )
                }

                if (showReviewList) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        res.questions.forEachIndexed { idx, q ->
                            val userChoice = res.userAnswers[q.id]
                            val isCorrect = userChoice == q.correctAnswer
                            val isSkipped = userChoice == null
                            val qTime = res.questionTimes[q.id] ?: 0

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isCorrect -> Emerald50
                                        isSkipped -> Slate100
                                        else -> Rose500.copy(alpha = 0.08f)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${idx + 1}-savol",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Slate900
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (qTime > 0) {
                                                Text(
                                                    text = "⏱️ $qTime s",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate600
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when {
                                                            isCorrect -> Emerald600
                                                            isSkipped -> Slate400
                                                            else -> Rose500
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = when {
                                                        isCorrect -> "To'g'ri"
                                                        isSkipped -> "Belgilanmagan"
                                                        else -> "Noto'g'ri"
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PureWhite
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = q.questionText,
                                        fontSize = 14.sp,
                                        color = Slate900,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text(
                                            text = "Sizning javobingiz: ${userChoice ?: "—"}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCorrect) Emerald600 else if (isSkipped) Slate600 else Rose500
                                        )
                                        Text(
                                            text = "To'g'ri javob: ${q.correctAnswer}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald600
                                        )
                                    }

                                    if (q.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Izoh: ${q.explanation}",
                                            fontSize = 11.sp,
                                            color = Slate600
                                        )
                                    }

                                    if (q.solutionSteps.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = q.solutionSteps,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Slate700
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}
