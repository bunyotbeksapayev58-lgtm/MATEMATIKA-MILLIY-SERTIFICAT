package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionEntity
import com.example.data.model.UserEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.DifficultyBadge
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Users, 2: Questions, 3: AI Generator, 4: Logs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Top Admin Header
        Surface(
            color = Navy900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Ortga", tint = PureWhite)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "ADMINISTRATOR PANELI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Amber400,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Boshqaruv, statistika va AI generator",
                                color = Blue100,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = Amber500,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "sapayev1231",
                            color = Navy900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Navy900,
                    contentColor = Amber400,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Amber400,
                            height = 3.dp
                        )
                    }
                ) {
                    val tabs = listOf("Ko'rsatkichlar", "JSON Import", "Savollar Bazasi", "Foydalanuvchilar", "AI Generator", "Jurnal")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selectedTab == index) Amber400 else Slate400
                                )
                            },
                            modifier = Modifier.testTag("admin_tab_$index")
                        )
                    }
                }
            }
        }

        // Tab Body
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedTab) {
                0 -> AdminOverviewTab(viewModel)
                1 -> AdminJsonImportTab(viewModel)
                2 -> AdminQuestionsTab(viewModel)
                3 -> AdminUsersTab(viewModel)
                4 -> AdminAiGeneratorTab(viewModel)
                5 -> AdminLogsTab(viewModel)
            }
        }
    }
}

// ====================================================================
// TAB 0: OVERVIEW & METRICS
// ====================================================================
@Composable
fun AdminOverviewTab(viewModel: MainViewModel) {
    val totalUsers by viewModel.repository.countUsers().collectAsState(initial = 0)
    val activeUsers by viewModel.repository.countActiveUsers().collectAsState(initial = 0)
    val totalQuestions by viewModel.repository.countApprovedQuestions().collectAsState(initial = 0)
    val mathQuestions by viewModel.repository.countQuestionsByModule("MATHEMATICS").collectAsState(initial = 0)
    val geometryQuestions by viewModel.repository.countQuestionsByModule("GEOMETRY").collectAsState(initial = 0)
    val totalAttempts by viewModel.repository.countCompletedAttemptsGlobal().collectAsState(initial = 0)
    val globalAvg by viewModel.repository.getGlobalAveragePercentage().collectAsState(initial = 0f)

    val easyCount by viewModel.repository.countQuestionsByDifficultyOverall("EASY").collectAsState(initial = 0)
    val mediumCount by viewModel.repository.countQuestionsByDifficultyOverall("MEDIUM").collectAsState(initial = 0)
    val hardCount by viewModel.repository.countQuestionsByDifficultyOverall("HARD").collectAsState(initial = 0)
    val veryHardCount by viewModel.repository.countQuestionsByDifficultyOverall("VERY_HARD").collectAsState(initial = 0)
    val todaySolvedCount by viewModel.repository.countQuestionsAnsweredToday().collectAsState(initial = 0)
    val publishedCount by viewModel.repository.countQuestionsByStatus("PUBLISHED").collectAsState(initial = 0)
    val archivedCount by viewModel.repository.countQuestionsByStatus("ARCHIVED").collectAsState(initial = 0)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main metrics row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Jami foydalanuvchilar",
                value = "$totalUsers",
                subtitle = "$activeUsers ta faol (faoliyat ko'rsatgan)",
                color = Blue700,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Savollar bazasi",
                value = "$totalQuestions",
                subtitle = "$mathQuestions mat | $geometryQuestions geom",
                color = Emerald600,
                modifier = Modifier.weight(1f)
            )
        }

        // Main metrics row 2
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Topshirilgan testlar",
                value = "$totalAttempts",
                subtitle = "Jami ishlangan sessiyalar",
                color = Amber600,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "O'rtacha natija",
                value = "${String.format("%.1f", globalAvg ?: 0f)}%",
                subtitle = "Platforma bo'yicha",
                color = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f)
            )
        }

        // Section 12: Difficulty breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Qiyinlik darajalari bo'yicha taqsimot",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Oson", fontSize = 11.sp, color = Emerald600)
                            Text("$easyCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("O'rta", fontSize = 11.sp, color = Blue700)
                            Text("$mediumCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Blue700)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Qiyin", fontSize = 11.sp, color = Amber600)
                            Text("$hardCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Amber600)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Juda qiyin", fontSize = 11.sp, color = Rose500)
                            Text("$veryHardCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Rose500)
                        }
                    }
                }
            }
        }

        // Section 12: Today's activity & Statuses
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Bugun ishlangan savollar",
                value = "$todaySolvedCount",
                subtitle = "Oxirgi 24 soat ichida",
                color = Blue700,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Holat (Status)",
                value = "$publishedCount faol",
                subtitle = "$archivedCount ta arxivlangan",
                color = if (archivedCount > 0) Amber600 else Emerald600,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Tizim holati va konfiguratsiyasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("• Matematika moduli: FAOL (1000 ta mashq savoli tayyor)", fontSize = 12.sp, color = Slate700)
                Text("• Geometriya moduli: FAOL (Puza Geometriya 1 & 2 asosida chizmali va analitik masalalar bazasi)", fontSize = 12.sp, color = Slate700)
                Text("• Duplikat nazorati: FAOL (UNIQUE userId + questionId, SHA-256 Canonical Hash)", fontSize = 12.sp, color = Slate700)
                Text("• Gemini AI integratsiyasi: Faol (gemini-3.5-flash)", fontSize = 12.sp, color = Slate700)
                Text("• Ma'lumotlar bazasi: SQLite / Room (Offline + Multi-device persistence)", fontSize = 12.sp, color = Slate700)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = Slate400)
        }
    }
}

// ====================================================================
// TAB 1: USERS MANAGEMENT
// ====================================================================
@Composable
fun AdminUsersTab(viewModel: MainViewModel) {
    val users by viewModel.repository.getAllUsersAdmin().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    val filtered = users.filter {
        it.username.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.firstName.contains(searchQuery, ignoreCase = true)
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Foydalanuvchini o'chirish") },
            text = { Text("Haqiqatan ham '${userToDelete?.username}' foydalanuvchisini butunlay o'chirmoqchimisiz?") },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.let { viewModel.adminDeleteUser(it) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("O'chirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Username yoki email orqali qidirish...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_search_user_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Jami foydalanuvchilar: ${filtered.size} ta",
            fontSize = 12.sp,
            color = Slate600,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${user.firstName} ${user.lastName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (user.role == "ADMIN") Amber500 else Blue100,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = user.role,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.role == "ADMIN") PureWhite else Blue700,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "@${user.username} • ${user.email}",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                            if (user.isBlocked) {
                                Text(
                                    text = "BLOKLANGAN",
                                    color = Rose600,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.adminToggleBlockUser(user) },
                                modifier = Modifier.testTag("block_user_${user.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Bloklash",
                                    tint = if (user.isBlocked) Rose600 else Slate400
                                )
                            }
                            IconButton(
                                onClick = { userToDelete = user },
                                modifier = Modifier.testTag("delete_user_${user.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "O'chirish",
                                    tint = Slate400
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// TAB 2: QUESTIONS BANK MANAGEMENT (CRUD)
// ====================================================================
@Composable
fun AdminQuestionsTab(viewModel: MainViewModel) {
    val questions by viewModel.repository.getAllQuestionsAdmin().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedModuleFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedDifficultyFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    var questionToArchive by remember { mutableStateOf<QuestionEntity?>(null) }
    var questionToDelete by remember { mutableStateOf<QuestionEntity?>(null) }

    val filtered = questions.filter {
        (selectedModuleFilter == "ALL" || it.module == selectedModuleFilter) &&
        (selectedDifficultyFilter == "ALL" || it.difficulty == selectedDifficultyFilter) &&
        (selectedStatusFilter == "ALL" ||
                (selectedStatusFilter == "ARCHIVED" && it.isArchived) ||
                (selectedStatusFilter == "PUBLISHED" && !it.isArchived && (it.status == "PUBLISHED" || it.status == "APPROVED")) ||
                (selectedStatusFilter == "DRAFT" && !it.isArchived && it.status == "DRAFT")) &&
        (it.questionText.contains(searchQuery, ignoreCase = true) ||
                it.topic.contains(searchQuery, ignoreCase = true) ||
                (!it.customQuestionId.isNullOrEmpty() && it.customQuestionId!!.contains(searchQuery, ignoreCase = true)))
    }

    if (showAddDialog) {
        AddQuestionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newQ ->
                viewModel.adminSaveQuestion(newQ) {
                    showAddDialog = false
                }
            }
        )
    }

    // Section 19: Archive (Soft Delete) Confirmation Dialog
    questionToArchive?.let { q ->
        AlertDialog(
            onDismissRequest = { questionToArchive = null },
            title = {
                Text(
                    text = if (q.isArchived) "Arxivdan chiqarish" else "Savolni arxivlash",
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = if (q.isArchived) {
                        "Bu savolni yana faol holatga qaytarishni xohlaysizmi? Foydalanuvchilar uni yana ishlashi mumkin bo'ladi."
                    } else {
                        "Bu savolni arxivlashni xohlaysizmi? Arxivlangan savollar foydalanuvchilarga berilmaydi, ammo natijalar tarixi saqlanib qoladi."
                    },
                    fontSize = 13.sp,
                    color = Slate700
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (q.isArchived) {
                            viewModel.adminUnarchiveQuestion(q.id)
                        } else {
                            viewModel.adminArchiveQuestion(q.id)
                        }
                        questionToArchive = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (q.isArchived) Emerald600 else Amber600)
                ) {
                    Text(if (q.isArchived) "Arxivdan chiqarish" else "Arxivlash")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { questionToArchive = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    // Hard Delete Confirmation Dialog
    questionToDelete?.let { q ->
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("Savolni butunlay o'chirish", fontWeight = FontWeight.Bold, color = Rose500) },
            text = { Text("Bu savol bazadan butunlay o'chiriladi. Ushbu amalni qaytarib bo'lmaydi. Davom etasizmi?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeleteQuestion(q)
                        questionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("O'chirish")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { questionToDelete = null }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ID, savol matni yoki mavzu...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_search_question_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("admin_add_question_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Qo'shish", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Module Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val moduleFilters = listOf(
                "ALL" to "Barcha fanlar",
                "MATHEMATICS" to "Matematika",
                "GEOMETRY" to "Geometriya (Puza)"
            )
            moduleFilters.forEach { (modKey, modLabel) ->
                val isSelected = selectedModuleFilter == modKey
                Surface(
                    modifier = Modifier.clickable { selectedModuleFilter = modKey },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Blue700 else Slate200
                ) {
                    Text(
                        text = modLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PureWhite else Slate700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val statusFilters = listOf(
                "ALL" to "Barcha holatlar",
                "PUBLISHED" to "Nashr",
                "DRAFT" to "Qoralama",
                "ARCHIVED" to "Arxiv"
            )
            statusFilters.forEach { (stKey, stLabel) ->
                val isSelected = selectedStatusFilter == stKey
                Surface(
                    modifier = Modifier.clickable { selectedStatusFilter = stKey },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Amber600 else Slate200
                ) {
                    Text(
                        text = stLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PureWhite else Slate700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Filtrlangan savollar: ${filtered.size} ta",
            fontSize = 12.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered.take(100), key = { it.id }) { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (q.isArchived) Color(0xFFF8FAFC) else PureWhite),
                    border = if (q.isArchived) androidx.compose.foundation.BorderStroke(1.dp, Slate200) else null
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DifficultyBadge(difficulty = q.difficulty)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (q.isArchived) Slate200 else Slate100,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${q.module} • ${q.topic}${if (q.bookSource.isNotBlank()) " • " + q.bookSource else ""}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate700,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (q.isArchived) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) {
                                        Text("ARXIV", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Amber600, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                } else if (q.status == "DRAFT") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(4.dp)) {
                                        Text("QORALAMA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate600, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Toggle Publish / Draft
                                IconButton(
                                    onClick = { viewModel.adminToggleQuestionPublish(q) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (q.status == "PUBLISHED" || q.status == "APPROVED") Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Holat",
                                        tint = if (q.status == "PUBLISHED" || q.status == "APPROVED") Emerald600 else Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Archive / Unarchive Button
                                IconButton(
                                    onClick = { questionToArchive = q },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (q.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                        contentDescription = if (q.isArchived) "Arxivdan chiqarish" else "Arxivlash",
                                        tint = if (q.isArchived) Blue700 else Amber600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Hard Delete
                                IconButton(
                                    onClick = { questionToDelete = q },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "O'chirish", tint = Rose500, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (!q.customQuestionId.isNullOrEmpty()) {
                            Text(
                                text = "ID: ${q.customQuestionId}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Text(
                            text = q.questionText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate900
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "To'g'ri: ${q.correctAnswer}  |  A) ${q.optionA}  B) ${q.optionB}  C) ${q.optionC}  D) ${q.optionD}",
                            fontSize = 11.sp,
                            color = Slate600
                        )

                        if (!q.formulaUsed.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Formula: ${q.formulaUsed}",
                                fontSize = 10.sp,
                                color = Blue700
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddQuestionDialog(
    onDismiss: () -> Unit,
    onSave: (QuestionEntity) -> Unit
) {
    var module by remember { mutableStateOf("GEOMETRY") }
    var bookSource by remember { mutableStateOf("Puza Geometriya 1") }
    var topic by remember { mutableStateOf("Uchburchaklar") }
    var difficulty by remember { mutableStateOf("MEDIUM") }
    var questionText by remember { mutableStateOf("") }
    var diagram by remember { mutableStateOf("TYPE:TRIANGLE_RIGHT;a=3;b=4;c=5") }
    var formulaUsed by remember { mutableStateOf("Pifagor teoremasi: c² = a² + b²") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correct by remember { mutableStateOf("A") }
    var explanation by remember { mutableStateOf("") }
    var solutionSteps by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi savol qo'shish", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = module,
                        onValueChange = { module = it },
                        label = { Text("Modul (MATHEMATICS / GEOMETRY)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = { difficulty = it },
                        label = { Text("Qiyinlik") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bookSource,
                        onValueChange = { bookSource = it },
                        label = { Text("Manba (Puza 1 / Puza 2)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Mavzu") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Savol matni") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = diagram,
                    onValueChange = { diagram = it },
                    label = { Text("Chizma spetsifikatsiyasi (TYPE:TRIANGLE_RIGHT;...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formulaUsed,
                    onValueChange = { formulaUsed = it },
                    label = { Text("Qo'llanilgan formula") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("A") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("B") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("C") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("D") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = correct,
                    onValueChange = { correct = it.uppercase() },
                    label = { Text("To'g'ri javob (A, B, C, yoki D)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Tushuntirish") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = solutionSteps,
                    onValueChange = { solutionSteps = it },
                    label = { Text("Yechish bosqichlari") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank() && optA.isNotBlank() && optB.isNotBlank()) {
                        val q = QuestionEntity(
                            subject = if (module == "GEOMETRY") "Geometriya" else "Matematika",
                            module = module,
                            bookSource = bookSource,
                            topic = topic,
                            subtopic = topic,
                            difficulty = difficulty,
                            questionText = questionText,
                            diagram = diagram,
                            formulaUsed = formulaUsed,
                            optionA = optA,
                            optionB = optB,
                            optionC = optC,
                            optionD = optD,
                            correctAnswer = correct,
                            explanation = explanation,
                            solutionSteps = solutionSteps,
                            sourceType = "ADMIN_ADDED",
                            status = "APPROVED"
                        )
                        onSave(q)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
            ) {
                Text("Saqlash")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}

// ====================================================================
// TAB 3: AI BULK GENERATOR (GEMINI AI + VALIDATION PIPELINE)
// ====================================================================
@Composable
fun AdminAiGeneratorTab(viewModel: MainViewModel) {
    var selectedTopic by remember { mutableStateOf("Tenglamalar") }
    var selectedDifficulty by remember { mutableStateOf("MEDIUM") }
    var count by remember { mutableIntStateOf(10) }
    var module by remember { mutableStateOf("MATHEMATICS") }
    val isLoading by viewModel.isActionLoading.collectAsState()

    val topics = listOf(
        "Sonlar va hisoblashlar",
        "Algebraik ifodalar",
        "Tenglamalar",
        "Tengsizliklar",
        "Funksiyalar va grafiklar",
        "Arifmetik va geometrik progressiya",
        "Ko'rsatkichli va logarifmik ifodalar",
        "Trigonometriya",
        "To'plamlar va kombinatorika",
        "Planimetriya",
        "Stereometriya"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Amber500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gemini AI Avtomatik Savollar Generatori",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Google AI Gemini modeli yordamida Milliy sertifikat standartlari bo'yicha yangi unikal savollar to'plamini yarating va bazaga to'g'ridan-to'g'ri integratsiya qiling.",
                    fontSize = 12.sp,
                    color = Slate600,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Modul:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { module = "MATHEMATICS" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (module == "MATHEMATICS") Blue700 else Slate200
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Matematika", color = if (module == "MATHEMATICS") PureWhite else Slate700, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { module = "GEOMETRY" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (module == "GEOMETRY") Blue700 else Slate200
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Geometriya", color = if (module == "GEOMETRY") PureWhite else Slate700, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Mavzu:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                OutlinedTextField(
                    value = selectedTopic,
                    onValueChange = { selectedTopic = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Qiyinlik darajasi:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EASY", "MEDIUM", "HARD", "VERY_HARD").forEach { diff ->
                        Button(
                            onClick = { selectedDifficulty = diff },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDifficulty == diff) Amber500 else Slate200
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(diff, color = if (selectedDifficulty == diff) Navy900 else Slate700, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Generatsiya qilinadigan savollar soni:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 20, 50).forEach { cnt ->
                        Button(
                            onClick = { count = cnt },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (count == cnt) Emerald600 else Slate200
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("$cnt ta", color = if (count == cnt) PureWhite else Slate700, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.adminBulkGenerateQuestions(
                            topic = selectedTopic,
                            difficulty = selectedDifficulty,
                            count = count,
                            module = module
                        ) {}
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_run_ai_generator"),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("AI Savollarni tuzmoqda va tekshirmoqda...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Amber400)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI ORQALI GENERATSIYA QILISH VA SAQLASH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ====================================================================
// TAB 4: ACTIVITY LOGS
// ====================================================================
@Composable
fun AdminLogsTab(viewModel: MainViewModel) {
    val logs by viewModel.repository.getRecentAdminLogs().collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "TIZIM AMALLARI JURNALI (AUDIT LOGS)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (logs.isEmpty()) {
            Text("Jurnalda hozircha yozuvlar yo'q.", color = Slate600, fontSize = 12.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.action,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Blue700
                                )
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Admin: ${log.adminUsername} • ${log.details}",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                        }
                    }
                }
            }
        }
    }
}
