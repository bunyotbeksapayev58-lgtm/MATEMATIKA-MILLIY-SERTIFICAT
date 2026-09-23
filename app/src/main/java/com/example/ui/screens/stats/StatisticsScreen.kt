package com.example.ui.screens.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.Emerald500
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val user = authState.currentUser
    val attemptsCount by viewModel.repository.getUserCompletedAttemptsCount(user?.id ?: 0).collectAsState(initial = 0)
    val avgPercentage by viewModel.repository.getUserAveragePercentage(user?.id ?: 0).collectAsState(initial = 0f)
    val bestScore by viewModel.repository.getUserBestScore(user?.id ?: 0).collectAsState(initial = 0)
    val attempts by if (user != null) {
        viewModel.repository.getUserAttempts(user.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val userLevel = viewModel.repository.calculateUserLevel(
        avgPercent = avgPercentage ?: 0f,
        completedTests = attemptsCount,
        bestScore = bestScore ?: 0
    )

    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp)
    ) {
        Text(
            text = "MENING STATISTIKAM",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "O'zlashtirish darajasi va imtihon natijalari dinamikasi",
            fontSize = 12.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Level & Accuracy Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stats_level_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Navy900)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Hozirgi darajangiz:", color = Slate400, fontSize = 12.sp)
                        Text(
                            text = userLevel.titleUz,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Surface(
                        color = Color(userLevel.colorHex),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = userLevel.levelName,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { userLevel.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Amber400,
                    trackColor = Slate700
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$attemptsCount", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Ishlangan testlar", color = Slate400, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format("%.1f", avgPercentage ?: 0f)}%",
                            color = Emerald500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(text = "O'rtacha aniqlik", color = Slate400, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${bestScore ?: 0}", color = Amber400, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Eng yuqori ball", color = Slate400, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Module comparison
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Modullar bo'yicha tayyorgarlik:", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Matematika (1000 savol)", fontSize = 12.sp, color = Slate700)
                    Text("FAOL (100%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Geometriya (Planimetriya, Fazo)", fontSize = 12.sp, color = Slate700)
                    Text("Tez kunda", fontSize = 12.sp, color = Slate400)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "IMTIHON VA TEST TARIXI",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        val completedAttempts = attempts.filter { it.state == "COMPLETED" }

        if (completedAttempts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, tint = Slate400, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Hozircha yakunlangan testlar mavjud emas", color = Slate600, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(completedAttempts, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_attempt_${item.id}"),
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
                                Text(
                                    text = item.testTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = dateFormat.format(Date(item.completedAt ?: item.startedAt)),
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.score} ball",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Blue700
                                )
                                Text(
                                    text = "${String.format("%.1f", item.percentage)}% aniqlik",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (item.percentage >= 70f) Emerald600 else Slate600
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
