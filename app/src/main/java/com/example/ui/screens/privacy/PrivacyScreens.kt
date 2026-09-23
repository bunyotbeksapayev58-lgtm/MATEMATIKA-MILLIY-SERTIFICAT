package com.example.ui.screens.privacy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue700
import com.example.ui.theme.Emerald50
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

// =========================================================================
// 1. PRIVACY POLICY SCREEN (O'zbekiston Respublikasi O'RQ-547 Qonuni)
// =========================================================================
@Composable
fun PrivacyPolicyScreen(
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
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
                    text = "Maxfiylik Siyosati",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "O'zbekiston Respublikasi O'RQ-547-sonli Qonuni",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        }

        // Legal Banner Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Navy900),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Blue700),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = PureWhite, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Shaxsga Doir Ma'lumotlar Himoyasi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ilovadagi barcha ma'lumotlar faqat o'quv jarayonini tahlil qilish uchun foydalaniladi va uchinchi shaxslarga berilmaydi.",
                        fontSize = 12.sp,
                        color = Blue100,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section 1: Yig'iladigan ma'lumotlar
        PolicySectionCard(
            title = "1. Yig'iladigan ma'lumotlar tarkibi",
            body = "Platformada ta'lim sifatini oshirish va milliy sertifikat darajasini belgilash uchun quyidagi ma'lumotlar saqlanadi:\n\n" +
                    "• Foydalanuvchining ismi, familiyasi va telefon raqami (identifikatsiya uchun);\n" +
                    "• Ishlangan testlar, to'plangan ballar va sertifikat darajalari (A+, A, B+, B, C+, C);\n" +
                    "• Har bir savolga sarflangan aniq vaqt (sekundlarda) va xatoliklar tahlili;\n" +
                    "• Mashg'ulotlar holati va seanslar tarixi."
        )

        // Section 2: Ma'lumotlarni saqlash va xavfsizlik
        PolicySectionCard(
            title = "2. Ma'lumotlarni saqlash xavfsizligi",
            body = "Barcha parollar kriptografik xesh-funksiyalar (SHA-256 va salt) orqali shifrlangan holatda saqlanadi. Ma'lumotlar bazasi mahalliy himoyalangan server arxitekturasida yuritiladi va doimiy zaxira nusxalari bilan ta'minlanadi."
        )

        // Section 3: Foydalanuvchi huquqlari (O'RQ-547 9-modda)
        PolicySectionCard(
            title = "3. Sizning huquqlaringiz (O'RQ-547)",
            body = "Qonunchilikka muvofiq har bir foydalanuvchi quyidagi huquqlarga ega:\n\n" +
                    "• O'z shaxsiy ma'lumotlari haqida to'liq axborot olish;\n" +
                    "• Barcha o'quv natijalari va statistikani JSON formatida yuklab olish (Eksport);\n" +
                    "• O'z akkauntini va barcha bog'liq o'quv ma'lumotlarini butunlay o'chirib tashlashni talab qilish."
        )

        // Go to Data Management Button
        Button(
            onClick = { viewModel.navigateTo(AppScreen.DATA_MANAGEMENT) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue700),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = PureWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ma'lumotlarni Boshqarish va Eksport", fontWeight = FontWeight.Bold, color = PureWhite)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PolicySectionCard(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                fontSize = 12.sp,
                color = Slate700,
                lineHeight = 18.sp
            )
        }
    }
}

// =========================================================================
// 2. DATA MANAGEMENT SCREEN (Export JSON & Delete Account O'RQ-547)
// =========================================================================
@Composable
fun DataManagementScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isActionLoading.collectAsState()
    val user = authState.currentUser

    var exportedJson by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Rose500, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = "Akkauntni butunlay o'chirish",
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            },
            text = {
                Text(
                    text = "Diqqat! Bu amal qaytarib bo'lmaydi. Sizning barcha test natijalaringiz, statistikalaringiz, xatolar tahlili va shaxsiy ma'lumotlaringiz bazadan to'liq o'chiriladi.",
                    color = Slate700,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteUserAccount {
                            // user logged out
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Ha, barchasini o'chirish", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
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
        // Header
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
                    text = "Shaxsiy Ma'lumotlar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Eksport va Ma'lumotlarni o'chirish huquqi",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        }

        // User info card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Foydalanuvchi Profili",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "F.I.Sh: ${user?.firstName ?: ""} ${user?.lastName ?: ""}",
                    fontSize = 13.sp,
                    color = Slate700
                )
                Text(
                    text = "Login: @${user?.username ?: ""}",
                    fontSize = 13.sp,
                    color = Slate700
                )
                Text(
                    text = "Email: ${user?.email ?: ""}",
                    fontSize = 13.sp,
                    color = Slate700
                )
                Text(
                    text = "Rol: ${user?.role ?: "USER"}",
                    fontSize = 13.sp,
                    color = Slate700
                )
            }
        }

        // Section 1: EXPORT DATA (JSON)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Blue700, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. Ma'lumotlarni eksport qilish (JSON)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Barcha test urinishlari, vaqt tahlillari va javoblaringizni ochiq standart JSON formatida yuklab oling.",
                    fontSize = 12.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.exportUserData { json ->
                            exportedJson = json
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoading) "Eksport qilinmoqda..." else "Ma'lumotlar arxivini tayyorlash",
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }

                // If exported, display preview and copy button
                if (exportedJson != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Eksport qilingan fayl (JSON)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald600
                                )
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("User Data Export", exportedJson)
                                        clipboard.setPrimaryClip(clip)
                                        isCopied = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = if (isCopied) Emerald600 else Blue700,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCopied) "Nusxalandi" else "Nusxa olish",
                                        fontSize = 11.sp,
                                        color = if (isCopied) Emerald600 else Blue700
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = exportedJson ?: "",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate700,
                                maxLines = 8
                            )
                        }
                    }
                }
            }
        }

        // Section 2: DELETE ACCOUNT (O'RQ-547 talabi)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Rose500.copy(alpha = 0.5f))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Rose500, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2. Akkaunt va ma'lumotlarni o'chirish",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Rose600
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "O'zbekiston Respublikasining 'Shaxsga doir ma'lumotlar to'g'risida'gi Qonuniga asosan, siz istalgan vaqtda o'z hisobingizni va barcha o'quv tarixingizni bazadan to'liq o'chirib tashlash huquqiga egasiz.",
                    fontSize = 12.sp,
                    color = Slate600,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Rose600)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Akkauntni butunlay o'chirish", fontWeight = FontWeight.Bold, color = Rose600)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
