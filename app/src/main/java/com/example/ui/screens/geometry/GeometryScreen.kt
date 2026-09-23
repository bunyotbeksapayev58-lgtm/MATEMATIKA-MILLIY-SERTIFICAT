package com.example.ui.screens.geometry

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.PracticeModeState
import com.example.ui.PracticeTestResult
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.GeometryDiagramView
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
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
fun GeometryScreen(
    viewModel: MainViewModel
) {
    val geometryState by viewModel.geometryState.collectAsState()

    when (geometryState) {
        PracticeModeState.CONFIGURING -> {
            GeometrySetupContent(viewModel = viewModel)
        }
        PracticeModeState.IN_PROGRESS, PracticeModeState.PAUSED -> {
            GeometryRunnerContent(viewModel = viewModel)
        }
        PracticeModeState.COMPLETED -> {
            GeometryResultContent(viewModel = viewModel)
        }
    }
}

// =============================================================================
// 1. GEOMETRY SETUP SCREEN (Puza Geometriya 1 & 2 Config)
// =============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeometrySetupContent(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val bookSource by viewModel.geometryBookSource.collectAsState()
    val difficulty by viewModel.geometryDifficulty.collectAsState()
    val questionCount by viewModel.geometryQuestionCount.collectAsState()
    val topic by viewModel.geometryTopic.collectAsState()
    val durationMinutes by viewModel.geometryDurationMinutes.collectAsState()
    val isLoading by viewModel.isActionLoading.collectAsState()

    var customCountText by remember { mutableStateOf("") }
    var isCustomCountActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("geometry_header_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Blue700,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Geometriya bo'yicha mashq",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Puza Geometriya 1 & 2 Tarjima asosida",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Ushbu modul Puza Geometriya 1-Kitob va 2-Kitob qoidalari, formulalari va chizmali masalalari asosida 1000+ original savollardan iborat.",
                        fontSize = 12.sp,
                        color = Blue700,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // 1. Manba tanlash (Book Source)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. KITOBLAR MANBASI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate700,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val sources = listOf(
                    "ALL" to "Barcha kitoblar (Puza 1 + Puza 2)",
                    "Puza Geometriya 1" to "Puza Geometriya 1 (Burchaklar, Uchburchak, Pifagor)",
                    "Puza Geometriya 2" to "Puza Geometriya 2 (To'rtburchaklar, Aylana, Fazoviy)"
                )

                sources.forEach { (srcKey, srcLabel) ->
                    val isSelected = bookSource == srcKey
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.setGeometryBookSource(srcKey) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFEEF2FF) else Slate100,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Blue700) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (isSelected) Blue700 else Slate400,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Blue700)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = srcLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Blue700 else Slate700
                            )
                        }
                    }
                }
            }
        }

        // 2. Darajani tanlash (Difficulty)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. QIYINLIK DARAJASI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate700,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val diffLevels = listOf(
                    Triple("ALL", "Barcha darajalar", "Aralash qiyinlikdagi geometriya masalalari."),
                    Triple("EASY", "🟢 Oson", "Formulalar va standart usullar asosidagi masalalar."),
                    Triple("MEDIUM", "🟡 O'rta", "Bir necha bosqichli hisoblash va mantiqiy fikrlash."),
                    Triple("HARD", "🔴 Qiyin", "Kombinatsiyalangan mavzular va chuqur tahlil."),
                    Triple("VERY_HARD", "🟣 Juda qiyin", "Olimpiada darajasidagi va murakkab geometrik konstruksiyalar.")
                )

                diffLevels.forEach { (diffKey, title, desc) ->
                    val isSelected = difficulty == diffKey
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.setGeometryDifficulty(diffKey) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFEFF6FF) else Slate100,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Blue700) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Blue700 else Slate900
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }
        }

        // 3. Misollar soni (Question count)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. MISOLLAR SONI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate700,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val presetCounts = listOf(10, 15, 20, 30, 45, 50)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetCounts.forEach { count ->
                        val isSelected = !isCustomCountActive && questionCount == count
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    isCustomCountActive = false
                                    viewModel.setGeometryQuestionCount(count)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Blue700 else Slate100
                        ) {
                            Text(
                                text = "$count ta",
                                color = if (isSelected) PureWhite else Slate700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customCountText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            customCountText = filtered
                            val num = filtered.toIntOrNull()
                            if (num != null && num in 1..100) {
                                isCustomCountActive = true
                                viewModel.setGeometryQuestionCount(num)
                            }
                        },
                        label = { Text("Boshqa miqdor (1-100)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        // 4. Geometriya Mavzulari (Topics)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. GEOMETRIYA MAVZUSI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate700,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val topics = listOf(
                    "Barcha mavzular",
                    "Burchaklar",
                    "Uchburchaklar",
                    "To‘rtburchaklar",
                    "Parallelogram",
                    "Trapetsiya",
                    "Romb",
                    "Aylana va doira",
                    "Yuzalar va Bo'yalgan sohalar",
                    "Ko'pburchaklar",
                    "Fazoviy geometriya (Stereometriya)"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    topics.forEach { top ->
                        val isSelected = topic == top
                        Surface(
                            modifier = Modifier.clickable { viewModel.setGeometryTopic(top) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF1E3A8A) else Slate100
                        ) {
                            Text(
                                text = top,
                                color = if (isSelected) PureWhite else Slate700,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Vaqt chegarasi (Timer)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "5. VAQT REJIMI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate700,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val timerOptions = listOf(
                    0 to "Cheksiz (vaqt nazoratisiz)",
                    15 to "15 daqiqa (Tezkor)",
                    30 to "30 daqiqa",
                    60 to "60 daqiqa (1 soat)",
                    90 to "90 daqiqa (Standart)"
                )

                timerOptions.forEach { (mins, label) ->
                    val isSelected = durationMinutes == mins
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { viewModel.setGeometryDurationMinutes(mins) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFFFEF3C7) else Slate100,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Amber600) else null
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Amber600 else Slate700,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Start Practice Button
        Button(
            onClick = {
                viewModel.startConfiguredGeometryPractice(
                    count = questionCount,
                    difficulty = difficulty,
                    durationMinutes = durationMinutes,
                    topic = topic,
                    bookSource = bookSource
                )
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_geometry_practice_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue700)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Geometriya Mashqini Boshlash",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// =============================================================================
// 2. GEOMETRY TEST RUNNER (Vector Diagram, Time Tracking, M3 Options)
// =============================================================================
@Composable
private fun GeometryRunnerContent(viewModel: MainViewModel) {
    val questions by viewModel.geometryQuestions.collectAsState()
    val currentIndex by viewModel.geometryIndex.collectAsState()
    val userAnswers by viewModel.geometryUserAnswers.collectAsState()
    val questionElapsed by viewModel.geometryQuestionElapsedSeconds.collectAsState()
    val totalActive by viewModel.geometryTotalActiveSeconds.collectAsState()
    val remainingSeconds by viewModel.geometryRemainingSeconds.collectAsState()
    val durationMinutes by viewModel.geometryDurationMinutes.collectAsState()
    val geometryState by viewModel.geometryState.collectAsState()

    val currentQ = questions.getOrNull(currentIndex)
    var showFinishConfirmDialog by remember { mutableStateOf(false) }
    var showQuestionGridModal by remember { mutableStateOf(false) }

    if (currentQ == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Savollar yuklanmoqda...", color = Slate600)
        }
        return
    }

    val selectedOption = userAnswers[currentQ.id]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Top Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PureWhite,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = currentQ.bookSource.ifEmpty { "Puza Geometriya" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue700,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        DifficultyBadge(difficulty = currentQ.difficulty)
                    }

                    // Timers
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (questionElapsed > 90) Rose500 else Blue700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatSeconds(questionElapsed),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (questionElapsed > 90) Rose500 else Slate900,
                            fontFamily = FontFamily.Monospace
                        )

                        if (durationMinutes > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "| Qoldi: ${formatSeconds(remainingSeconds)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (remainingSeconds < 180) Rose500 else Slate600,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (geometryState == PracticeModeState.PAUSED) {
                                    viewModel.resumeGeometryPractice()
                                } else {
                                    viewModel.pauseGeometryPractice()
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (geometryState == PracticeModeState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pauza",
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Indicator
                val progress = if (questions.isNotEmpty()) (currentIndex + 1).toFloat() / questions.size else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Blue700,
                    trackColor = Slate200
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Savol ${currentIndex + 1} / ${questions.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Slate700
                    )
                    Text(
                        text = currentQ.topic,
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }
        }

        // Main Question Content (Scrollable)
        val contentScroll = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(contentScroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Vector Diagram Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("geometry_diagram_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GeometryDiagramView(
                        diagramSpec = currentQ.diagram,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            // Question Text Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentQ.questionText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900,
                        lineHeight = 22.sp
                    )

                    if (!currentQ.formulaUsed.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Functions,
                                    contentDescription = null,
                                    tint = Blue700,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Formulasi: ${currentQ.formulaUsed}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate700
                                )
                            }
                        }
                    }
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
                    val isChosen = selectedOption == letter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectGeometryAnswer(currentQ.id, letter) }
                            .testTag("geometry_option_$letter"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isChosen) Color(0xFFEEF2FF) else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isChosen) 2.dp else 1.dp,
                            color = if (isChosen) Blue700 else Slate200
                        ),
                        shadowElevation = if (isChosen) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isChosen) Blue700 else Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isChosen) PureWhite else Slate700
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = optText,
                                fontSize = 14.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                color = if (isChosen) Blue700 else Slate900,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PureWhite,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Oldingi
                OutlinedButton(
                    onClick = { viewModel.previousGeometryQuestion() },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Oldingi",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Oldingi", fontSize = 12.sp)
                }

                // Bookmark / Saqlash
                IconButton(
                    onClick = { viewModel.toggleSaveCurrentGeometryQuestion() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Saqlash",
                        tint = Slate700
                    )
                }

                // Yakunlash or Keyingi
                if (currentIndex == questions.size - 1) {
                    Button(
                        onClick = { showFinishConfirmDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yakunlash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextGeometryQuestion() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue700)
                    ) {
                        Text("Keyingi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Keyingi",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Finish Confirmation Dialog
    if (showFinishConfirmDialog) {
        val answeredCount = userAnswers.size
        val totalCount = questions.size
        val unanswered = totalCount - answeredCount

        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            title = {
                Text(
                    text = "Testni yakunlaysizmi?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Javob berilgan: $answeredCount ta / $totalCount ta",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    if (unanswered > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Eslatma: $unanswered ta savolga javob berilmadi.",
                            fontSize = 12.sp,
                            color = Rose500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmDialog = false
                        viewModel.finishGeometryTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Ha, yakunlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text("Davom etish")
                }
            }
        )
    }
}

// =============================================================================
// 3. GEOMETRY RESULT SCREEN (Review, Diagrams, AI Recommendation)
// =============================================================================
@Composable
private fun GeometryResultContent(viewModel: MainViewModel) {
    val result by viewModel.geometryResult.collectAsState()
    val scrollState = rememberScrollState()

    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Natija hisoblanmoqda...", color = Slate600)
        }
        return
    }

    val res = result!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        res.percentage >= 85 -> Color(0xFFDCFCE7)
                        res.percentage >= 60 -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = "Sertifikat Darajasi: ${res.certificateGrade}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = when {
                            res.percentage >= 85 -> Emerald600
                            res.percentage >= 60 -> Amber600
                            else -> Rose500
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${res.percentage.toInt()}%",
                    fontWeight = FontWeight.Black,
                    fontSize = 44.sp,
                    color = Slate900
                )

                Text(
                    text = "${res.correctCount} ta to'g'ri / ${res.totalQuestions} ta savoldan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatMetric(label = "To'g'ri", value = "${res.correctCount}", color = Emerald600)
                    StatMetric(label = "Noto'g'ri", value = "${res.wrongCount}", color = Rose500)
                    StatMetric(label = "Javobsiz", value = "${res.unansweredCount}", color = Slate400)
                    StatMetric(label = "Umumiy vaqt", value = formatSeconds(res.timeSpentSeconds), color = Blue700)
                }
            }
        }

        // Timing Analytics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Amber600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vaqt Tahlili va Tezlik",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("O'rtacha vaqt / savol", fontSize = 11.sp, color = Slate600)
                        Text(
                            text = "${res.averageTimePerQuestionSeconds.toInt()} soniya",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate900
                        )
                    }
                    Column {
                        Text("Eng tez yechilgan", fontSize = 11.sp, color = Slate600)
                        Text(
                            text = "${res.fastestQuestionSeconds} soniya",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Emerald600
                        )
                    }
                    Column {
                        Text("Eng ko'p vaqt olgan", fontSize = 11.sp, color = Slate600)
                        Text(
                            text = "${res.slowestQuestionSeconds} soniya",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Rose500
                        )
                    }
                }
            }
        }

        // Pedagogical AI Recommendation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Emerald600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Puza Geometriya Bo'yicha Tavsiya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Emerald600
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = res.aiRecommendation,
                    fontSize = 12.sp,
                    color = Slate700,
                    lineHeight = 18.sp
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.resetGeometryToConfig() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Qayta boshlash", fontSize = 12.sp)
            }

            Button(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue700)
            ) {
                Text("Bosh sahifa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Question-by-Question Review with Diagrams
        Text(
            text = "SAVOLLAR TAHLILI VA CHIZMALAR",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Slate700,
            letterSpacing = 1.sp
        )

        res.questions.forEachIndexed { index, question ->
            val userAns = res.userAnswers[question.id]
            val isCorrect = userAns == question.correctAnswer
            val isAnswered = userAns != null

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}-savol (${question.topic})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate900
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                !isAnswered -> Slate100
                                isCorrect -> Color(0xFFDCFCE7)
                                else -> Color(0xFFFEE2E2)
                            }
                        ) {
                            Text(
                                text = when {
                                    !isAnswered -> "Javobsiz"
                                    isCorrect -> "To'g'ri"
                                    else -> "Noto'g'ri"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    !isAnswered -> Slate600
                                    isCorrect -> Emerald600
                                    else -> Rose500
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Diagram preview
                    GeometryDiagramView(
                        diagramSpec = question.diagram,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = question.questionText,
                        fontSize = 13.sp,
                        color = Slate900,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Sizning javobingiz: ${userAns ?: "Belgilanmagan"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCorrect) Emerald600 else Rose500
                        )
                        Text(
                            text = "To'g'ri javob: ${question.correctAnswer}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600
                        )
                    }

                    if (!question.formulaUsed.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📐 Formulalar: ${question.formulaUsed}",
                            fontSize = 11.sp,
                            color = Blue700,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!question.explanation.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Yechim: ${question.explanation}\n${question.solutionSteps ?: ""}",
                                fontSize = 11.sp,
                                color = Slate700,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = Slate600)
    }
}

private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
