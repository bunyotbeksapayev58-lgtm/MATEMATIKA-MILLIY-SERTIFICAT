package com.example.ui.screens.review

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
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.example.data.model.QuestionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.DifficultyBadge
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue700
import com.example.ui.theme.Emerald600
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

// ====================================================================
// 1. ERRORS SCREEN ("Xatolarim" - Mistake analysis with full solutions)
// ====================================================================
@Composable
fun ErrorsScreen(
    viewModel: MainViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val user = authState.currentUser
    val errors by if (user != null) {
        viewModel.repository.getUnresolvedErrors(user.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp)
    ) {
        Text(
            text = "XATOLARIM BILAN ISHLASH",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Testlar davomida yo'l qo'yilgan xatolar va ularning to'g'ri yechimlari",
            fontSize = 12.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (errors.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Emerald600,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Hozircha xatolaringiz yo'q!",
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Testlarni yechishda davom eting. Agar xato qilsangiz, tahlil uchun bu yerda saqlanadi.",
                        color = Slate600,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Text(
                text = "Tahlil qilinishi kerak bo'lgan xatolar: ${errors.size} ta",
                fontWeight = FontWeight.Bold,
                color = Rose600,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(errors, key = { it.id }) { question ->
                    ErrorItemCard(
                        question = question,
                        onResolve = {
                            if (user != null) {
                                viewModel.viewModelScope.launch {
                                    viewModel.repository.markErrorResolved(user.id, question.id)
                                    viewModel.showToast("Xato bartaraf etildi deb belgilandi")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorItemCard(
    question: QuestionEntity,
    onResolve: () -> Unit
) {
    var isSolutionVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("error_card_${question.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DifficultyBadge(difficulty = question.difficulty)

                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = question.topic,
                        color = Slate700,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // To'g'ri javob ko'rsatkichi
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Emerald600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "To'g'ri javob: ${question.correctAnswer}",
                        fontWeight = FontWeight.Bold,
                        color = Emerald600,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle Solution Button
            OutlinedButton(
                onClick = { isSolutionVisible = !isSolutionVisible },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSolutionVisible) "Yechimni yashirish" else "To'liq yechimni ko'rish",
                    fontSize = 12.sp,
                    color = Slate900
                )
            }

            if (isSolutionVisible) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tushuntirish:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Slate700
                        )
                        Text(
                            text = question.explanation,
                            fontSize = 12.sp,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bosqichma-bosqich yechim:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Slate700
                        )
                        Text(
                            text = question.solutionSteps,
                            fontSize = 12.sp,
                            color = Slate900,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onResolve,
                colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tushundim (Xatoni bartaraf etish)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ====================================================================
// 2. SAVED QUESTIONS SCREEN ("Saqlangan savollar")
// ====================================================================
@Composable
fun SavedQuestionsScreen(
    viewModel: MainViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val user = authState.currentUser
    val savedQuestions by if (user != null) {
        viewModel.repository.getSavedQuestions(user.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp)
    ) {
        Text(
            text = "SAQLANGAN SAVOLLAR",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Qayta takrorlash uchun xatcho'p qilingan savollar",
            fontSize = 12.sp,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (savedQuestions.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkRemove,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Saqlangan savollar mavjud emas",
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mashq qilish jarayonida o'zingizga ma'qul kelgan savollarni saqlab qo'yishingiz mumkin.",
                        color = Slate600,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(savedQuestions, key = { it.id }) { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_card_${question.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DifficultyBadge(difficulty = question.difficulty)

                                IconButton(
                                    onClick = {
                                        if (user != null) {
                                            viewModel.viewModelScope.launch {
                                                viewModel.repository.toggleSaveQuestion(user.id, question.id)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkRemove,
                                        contentDescription = "O'chirish",
                                        tint = Rose500
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "A) ${question.optionA}   B) ${question.optionB}\nC) ${question.optionC}   D) ${question.optionD}",
                                fontSize = 12.sp,
                                color = Slate700
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "To'g'ri javob: ${question.correctAnswer}",
                                fontWeight = FontWeight.Bold,
                                color = Emerald600,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
