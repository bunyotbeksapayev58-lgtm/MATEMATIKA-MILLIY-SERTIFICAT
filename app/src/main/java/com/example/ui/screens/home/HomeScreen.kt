package com.example.ui.screens.home

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.ShutterSpeed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestTemplateEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue600
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
import com.example.ui.theme.Slate900

data class HomeActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    viewModel: MainViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val user = authState.currentUser
    val attemptsCount by viewModel.repository.getUserCompletedAttemptsCount(user?.id ?: 0).collectAsState(initial = 0)
    val avgPercentage by viewModel.repository.getUserAveragePercentage(user?.id ?: 0).collectAsState(initial = 0f)
    val bestScore by viewModel.repository.getUserBestScore(user?.id ?: 0).collectAsState(initial = 0)
    val resumableSession by viewModel.resumableSession.collectAsState()

    val userLevel = viewModel.repository.calculateUserLevel(
        avgPercent = avgPercentage ?: 0f,
        completedTests = attemptsCount,
        bestScore = bestScore ?: 0
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero Greeting Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_hero_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Navy900)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Assalomu alaykum,",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${user?.firstName ?: "Foydalanuvchi"} ${user?.lastName ?: ""}",
                            color = PureWhite,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Level Badge
                    Surface(
                        color = Color(userLevel.colorHex),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = userLevel.levelName,
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar to next level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = userLevel.titleUz,
                        color = Amber400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${userLevel.progressPercent}%",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { userLevel.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Amber400,
                    trackColor = Slate700
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$attemptsCount ta", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Ishlangan testlar", color = Slate400, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${String.format("%.1f", avgPercentage ?: 0f)}%", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "O'rtacha aniqlik", color = Slate400, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${bestScore ?: 0} ball", color = Amber400, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Eng yaxshi natija", color = Slate400, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section 16 & Resumable Session Card
        if (resumableSession != null) {
            val session = resumableSession!!
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resumable_session_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Amber500))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Amber500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Amber600, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Davom ettirilayotgan mashg'ulot",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "${session.topic} • ${session.difficulty}",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }

                        Surface(
                            color = Amber50,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${session.currentQuestionIndex + 1} / ${session.questionCount} ta",
                                color = Amber600,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val progress = if (session.questionCount > 0) {
                        (session.currentQuestionIndex + 1).toFloat() / session.questionCount.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Amber500,
                        trackColor = Slate200
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { viewModel.resumeActiveSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Davom ettirish", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PureWhite, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // MODULE CHANGER: MATEMATIKA VS GEOMETRIYA
        Text(
            text = "O'QUV MODULLARI",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate700,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // MATEMATIKA MODULE (ACTIVE) -> Opens Practice Setup (Mega-Prompt Requirement 1)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.resetPracticeToConfig()
                        viewModel.navigateTo(AppScreen.PRACTICE)
                    }
                    .testTag("module_card_mathematics"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Blue700))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Blue100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = Blue700, modifier = Modifier.size(22.dp))
                        }
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "FAOL",
                                color = Emerald600,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Matematika",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Text(
                        text = "1000 ta mashq savollari",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }

            // GEOMETRIYA MODULE (PUZA GEOMETRIYA 1 & 2 - ACTIVE)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        viewModel.resetGeometryToConfig()
                        viewModel.navigateTo(AppScreen.GEOMETRY)
                    }
                    .testTag("module_card_geometry"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Blue700))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = Blue700, modifier = Modifier.size(22.dp))
                        }
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "FAOL",
                                color = Emerald600,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Geometriya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Text(
                        text = "Puza 1 & 2 (1000 ta savol)",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // NATIONAL CERTIFICATE MOCK TEST HERO BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.startTest(
                        TestTemplateEntity(
                            title = "Milliy Sertifikat — 45 Talik Rasmiy Format",
                            module = "MATHEMATICS",
                            topic = "Barcha mavzular (Kompleks)",
                            questionCount = 45,
                            durationMinutes = 150,
                            difficulty = "ALL",
                            isNationalCertMode = true
                        )
                    )
                }
                .testTag("start_mock_banner"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Blue700)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(PureWhite.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = Amber400,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Milliy Sertifikat (45 ta savol)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    )
                    Text(
                        text = "150 daqiqa • Rasmiy imtihon formati",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Blue100
                        )
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Boshlash",
                    tint = PureWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 9 ASOSIY BO'LIMLAR GRID
        Text(
            text = "MASHG'ULOT VA IMTIHON BO'LIMLARI",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate700,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        val actions = listOf(
            HomeActionItem(
                title = "1. Misol ishlash va Test sozlash",
                subtitle = "Soni, qiyinligi va vaqtini tanlash",
                icon = Icons.Default.EditNote,
                color = Blue700,
                onClick = {
                    viewModel.resetPracticeToConfig()
                    viewModel.navigateTo(AppScreen.PRACTICE)
                }
            ),
            HomeActionItem(
                title = "2. Oson savollar (EASY)",
                subtitle = "2 000 ta baza misoli",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF0284C7),
                onClick = { viewModel.startPractice("EASY", "Barcha mavzular") }
            ),
            HomeActionItem(
                title = "3. O'rta savollar (MEDIUM)",
                subtitle = "2 000 ta o'rta darajali",
                icon = Icons.Default.FitnessCenter,
                color = Amber600,
                onClick = { viewModel.startPractice("MEDIUM", "Barcha mavzular") }
            ),
            HomeActionItem(
                title = "4. Qiyin savollar (HARD)",
                subtitle = "2 000 ta murakkab savol",
                icon = Icons.Default.AutoAwesome,
                color = Rose600,
                onClick = { viewModel.startPractice("HARD", "Barcha mavzular") }
            ),
            HomeActionItem(
                title = "5. O'ta qiyin savollar (VERY HARD)",
                subtitle = "2 000 ta A+ darajali savol",
                icon = Icons.Default.MilitaryTech,
                color = Color(0xFF4C1D95),
                onClick = { viewModel.startPractice("VERY_HARD", "Barcha mavzular") }
            ),
            HomeActionItem(
                title = "6. Milliy testlar",
                subtitle = "45 talik rasmiy mock testlar",
                icon = Icons.Default.Quiz,
                color = Blue900,
                onClick = { viewModel.navigateTo(AppScreen.TESTS) }
            ),
            HomeActionItem(
                title = "7. Vaqtli test",
                subtitle = "15 talik tezkor sinov",
                icon = Icons.Default.Timer,
                color = Color(0xFF7C3AED),
                onClick = {
                    viewModel.startTest(
                        TestTemplateEntity(
                            title = "Tezkor Vaqtli Sinov Testi",
                            module = "MATHEMATICS",
                            topic = "Barcha mavzular",
                            questionCount = 15,
                            durationMinutes = 30,
                            difficulty = "MEDIUM",
                            isNationalCertMode = false
                        )
                    )
                }
            ),
            HomeActionItem(
                title = "8. Xatolarim",
                subtitle = "Tahlil va to'liq yechimlar",
                icon = Icons.Default.Warning,
                color = Rose500,
                onClick = { viewModel.navigateTo(AppScreen.ERRORS) }
            ),
            HomeActionItem(
                title = "9. Saqlanganlar",
                subtitle = "Muhim deb belgilanganlar",
                icon = Icons.Default.Bookmark,
                color = Amber500,
                onClick = { viewModel.navigateTo(AppScreen.SAVED) }
            ),
            HomeActionItem(
                title = "10. Maxfiylik va Shaxsiy Ma'lumotlar",
                subtitle = "O'RQ-547 Qonuni • Eksport va Ma'lumotlarni o'chirish",
                icon = Icons.Default.Lock,
                color = Color(0xFF0D9488),
                onClick = { viewModel.navigateTo(AppScreen.PRIVACY_POLICY) }
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (item in actions) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = item.onClick)
                        .testTag("action_${item.title.take(3).trim()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(item.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Slate900
                            )
                            Text(
                                text = item.subtitle,
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Compliance Legal Footer
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "O'zbekiston Respublikasi O'RQ-547-sonli 'Shaxsga doir ma'lumotlar to'g'risida'gi Qonuniga muvofiq himoyalangan.",
                    fontSize = 11.sp,
                    color = Slate600,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.PRIVACY_POLICY) }) {
                        Text("Maxfiylik Siyosati", fontSize = 12.sp, color = Blue700, fontWeight = FontWeight.SemiBold)
                    }
                    Text("•", color = Slate400, modifier = Modifier.align(Alignment.CenterVertically))
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.DATA_MANAGEMENT) }) {
                        Text("Ma'lumotlarimni Boshqarish", fontSize = 12.sp, color = Blue700, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
