package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.importer.JsonQuestionImporter
import com.example.ui.MainViewModel
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue700
import com.example.ui.theme.Emerald600
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun AdminJsonImportTab(viewModel: MainViewModel) {
    var selectedModule by remember { mutableStateOf("MATHEMATICS") }
    var selectedDifficulty by remember { mutableStateOf("MEDIUM") }
    var selectedConflictPolicy by remember { mutableStateOf(com.example.data.service.ImportConflictPolicy.SKIP_DUPLICATES) }
    var jsonInputText by remember { mutableStateOf("") }
    var allowPartialImport by remember { mutableStateOf(false) }

    val preview by viewModel.jsonImportPreview.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingJson.collectAsState()
    val importResult by viewModel.jsonImportResult.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Blue700, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Savollarni JSON Orqali Yuklash",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Boshlang'ich fan va qiyinlik darajasini tanlang, JSON matnini kiriting yoki andoza yuklab tahlil qiling. Tizim duplikatlarni Canonical Hash va Question ID bo'yicha avtomatik tekshiradi.",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        }

        // 1. Authoritative Module Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Fan yo'nalishini tanlang (Authoritative)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val modules = listOf(
                        "MATHEMATICS" to "Matematika",
                        "GEOMETRY" to "Geometriya (Puza)"
                    )
                    modules.forEach { (mKey, mLabel) ->
                        val isSelected = selectedModule == mKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedModule = mKey
                                    viewModel.clearJsonImport()
                                }
                                .testTag("import_module_$mKey"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Blue700 else Slate100,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = mLabel,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else Slate700
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Authoritative Difficulty Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Qiyinlik darajasini tanlang (Authoritative)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val difficulties = listOf(
                        "EASY" to "Oson",
                        "MEDIUM" to "O'rta",
                        "HARD" to "Qiyin",
                        "VERY_HARD" to "Juda qiyin"
                    )
                    difficulties.forEach { (dKey, dLabel) ->
                        val isSelected = selectedDifficulty == dKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedDifficulty = dKey
                                    viewModel.clearJsonImport()
                                }
                                .testTag("import_diff_$dKey"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Amber600 else Slate100,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = dLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else Slate700
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Conflict Resolution Policy (Idempotency Mode)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Takrorlanish siyosati (Idempotentlik qoidasi)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bir xil fayl bir necha bor yuklanganda yoki bazada mavjud savollar uchraganda tizim qanday yo'l tutishi kerak:",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val policies = listOf(
                        com.example.data.service.ImportConflictPolicy.SKIP_DUPLICATES to "Duplikatni o'tkazish (Skip)",
                        com.example.data.service.ImportConflictPolicy.UPDATE_EXISTING to "Mavjudni yangilash (Upsert)",
                        com.example.data.service.ImportConflictPolicy.ABORT_ON_DUPLICATE to "Qat'iy to'xtatish (Strict)"
                    )
                    policies.forEach { (pKey, pLabel) ->
                        val isSelected = selectedConflictPolicy == pKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedConflictPolicy = pKey
                                    if (preview != null) viewModel.clearJsonImport()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Blue700 else Slate100,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = pLabel,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else Slate700,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Template Actions & Paste Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "4. JSON Ma'lumotlarni Kiriting",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate800
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                jsonInputText = if (selectedModule == "MATHEMATICS") {
                                    JsonQuestionImporter.getMathSampleJson()
                                } else {
                                    JsonQuestionImporter.getGeometrySampleJson()
                                }
                                viewModel.clearJsonImport()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("load_sample_json_button")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Namuna yuklash", fontSize = 11.sp)
                        }

                        if (jsonInputText.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    jsonInputText = ""
                                    viewModel.clearJsonImport()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Tozalash", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = jsonInputText,
                    onValueChange = {
                        jsonInputText = it
                        if (preview != null) viewModel.clearJsonImport()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .testTag("json_input_field"),
                    placeholder = {
                        Text(
                            "Bu yerga JSON matnini joylang (masalan, {\"version\": \"1.0\", \"subject\": \"mathematics\", \"questions\": [...]})",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Slate900
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.previewJsonImport(jsonInputText, selectedModule, selectedDifficulty, selectedConflictPolicy)
                    },
                    enabled = jsonInputText.isNotBlank() && !isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analyze_json_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PureWhite, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tahlil qilinmoqda...")
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tahlil qilish (Tekshirish)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Preview & Validation Summary
        preview?.let { prev ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Tahlil Natijalari (Import Preview)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Slate100
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Jami", fontSize = 11.sp, color = Slate600)
                                Text("${prev.totalCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Yangi", fontSize = 11.sp, color = Emerald600)
                                Text("${prev.validCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Duplikat", fontSize = 11.sp, color = Amber600)
                                Text("${prev.duplicateCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Amber600)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Xato", fontSize = 11.sp, color = Rose500)
                                Text("${prev.errorCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Rose500)
                            }
                        }
                    }

                    // Duplicates details
                    if (prev.duplicatesList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Amber600, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Takrorlangan savollar (Duplikatlar):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber600)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                prev.duplicatesList.forEach { dup ->
                                    Text("• $dup", fontSize = 11.sp, color = Slate700)
                                }
                            }
                        }
                    }

                    // Errors details
                    if (prev.errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Rose500, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Xatoliklar ro'yxati:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Rose500)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                prev.errors.forEach { err ->
                                    Text("• $err", fontSize = 11.sp, color = Rose500)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // All-or-nothing toggle
                    if (prev.errors.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { allowPartialImport = !allowPartialImport }
                        ) {
                            Checkbox(
                                checked = allowPartialImport,
                                onCheckedChange = { allowPartialImport = it },
                                colors = CheckboxDefaults.colors(checkedColor = Amber600)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Xatolarni o'tkazib yuborib, faqat to'g'ri (${prev.validCount} ta) savollarni yuklash (Partial)",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Final Execute Button
                    val canImport = prev.validCount > 0 && (prev.errors.isEmpty() || allowPartialImport)
                    Button(
                        onClick = {
                            viewModel.executeJsonImport(prev, allowPartialImport, selectedConflictPolicy) {
                                jsonInputText = ""
                            }
                        },
                        enabled = canImport && !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("execute_import_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (prev.validCount > 0) "${prev.validCount} ta yangi savolni bazaga saqlash" else "Yuklash uchun yangi savol yo'q",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Import Result Banner
        importResult?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (res.success) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (res.success) Emerald600 else Rose500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (res.success) "Muvaffaqiyatli yakunlandi!" else "Import xatoligi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (res.success) Emerald600 else Rose500
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = res.message,
                            fontSize = 11.sp,
                            color = Slate700
                        )
                    }
                }
            }
        }
    }
}
