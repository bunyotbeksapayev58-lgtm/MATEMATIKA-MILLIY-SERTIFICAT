package com.example.data.importer

import com.example.data.dao.AdminLogDao
import com.example.data.dao.QuestionDao
import com.example.data.model.QuestionEntity
import com.example.data.service.AdminQuestionImportService
import com.example.data.service.DuplicateMatch
import com.example.data.service.DuplicateMatchType
import com.example.data.service.DifficultyMatchResult
import com.example.data.service.ImportConflictPolicy
import com.example.data.service.ModuleMatchResult
import com.example.data.service.QuestionValidationError
import com.example.data.service.ValidationReport
import org.json.JSONArray
import org.json.JSONObject

data class ImportPreview(
    val totalCount: Int,
    val validCount: Int,
    val duplicateCount: Int,
    val errorCount: Int,
    val errors: List<String>,
    val duplicatesList: List<String>,
    val validQuestions: List<QuestionEntity>,
    val subjectMatched: Boolean,
    val difficultyMatched: Boolean,
    val rawJsonError: String? = null
)

data class ImportResult(
    val success: Boolean,
    val totalFound: Int,
    val newlyAddedCount: Int,
    val duplicateSkippedCount: Int,
    val message: String
)

object JsonQuestionImporter {

    fun computeQuestionHash(questionText: String, options: List<String>): String =
        AdminQuestionImportService.computeCanonicalHash(questionText, options)

    fun computeCanonicalHash(questionText: String, options: List<String>): String =
        AdminQuestionImportService.computeCanonicalHash(questionText, options)

    suspend fun analyzeAndValidate(
        jsonString: String,
        selectedModule: String, // "MATHEMATICS" or "GEOMETRY"
        selectedDifficulty: String, // "EASY", "MEDIUM", "HARD", "VERY_HARD"
        questionDao: QuestionDao,
        conflictPolicy: ImportConflictPolicy = ImportConflictPolicy.SKIP_DUPLICATES
    ): ImportPreview {
        val report = AdminQuestionImportService.validateJson(
            jsonString = jsonString,
            targetModule = selectedModule,
            targetDifficulty = selectedDifficulty,
            questionDao = questionDao,
            conflictPolicy = conflictPolicy
        )
        return report.toImportPreview()
    }

    /**
     * In-memory validation overload for unit tests and offline verification.
     */
    fun analyzeAndValidate(
        jsonString: String,
        targetModule: String,
        targetDifficulty: String,
        existingQuestionIds: Set<String> = emptySet(),
        existingQuestionHashes: Set<String> = emptySet()
    ): ImportPreview {
        val errors = mutableListOf<String>()
        val duplicates = mutableListOf<String>()
        val validQuestions = mutableListOf<QuestionEntity>()

        val trimmed = jsonString.trim()
        val questionsArray: JSONArray
        var jsonSubject: String? = null
        var jsonDifficulty: String? = null

        try {
            if (trimmed.startsWith("[")) {
                questionsArray = JSONArray(trimmed)
            } else {
                val root = JSONObject(trimmed)
                jsonSubject = if (root.has("subject")) root.optString("subject").ifEmpty { null } else null
                jsonDifficulty = if (root.has("difficulty")) root.optString("difficulty").ifEmpty { null } else null
                val arr = root.optJSONArray("questions")
                if (arr == null) {
                    return ImportPreview(
                        totalCount = 0, validCount = 0, duplicateCount = 0, errorCount = 1,
                        errors = listOf("'questions' massivi topilmadi."), duplicatesList = emptyList(),
                        validQuestions = emptyList(), subjectMatched = false, difficultyMatched = false
                    )
                }
                questionsArray = arr
            }
        } catch (e: Exception) {
            return ImportPreview(
                totalCount = 0, validCount = 0, duplicateCount = 0, errorCount = 1,
                errors = listOf("JSON formati noto'g'ri: ${e.localizedMessage ?: "Sintaksis xatosi"}"),
                duplicatesList = emptyList(), validQuestions = emptyList(),
                subjectMatched = false, difficultyMatched = false, rawJsonError = e.message
            )
        }

        val normModule = AdminQuestionImportService.normalizeModule(jsonSubject)
        val subjectMatched = normModule == null || normModule == targetModule
        if (!subjectMatched) {
            errors.add("Fan mos kelmadi: Siz '$targetModule' tanladingiz, ammo JSON da '$jsonSubject' ko'rsatilgan.")
        }

        val normDiff = AdminQuestionImportService.normalizeDifficulty(jsonDifficulty)
        val difficultyMatched = normDiff == null || normDiff == targetDifficulty
        if (!difficultyMatched) {
            errors.add("Daraja mos kelmadi: Siz '$targetDifficulty' tanladingiz, ammo JSON da '$jsonDifficulty' ko'rsatilgan.")
        }

        val totalCount = questionsArray.length()
        val seenIds = mutableSetOf<String>()
        val seenHashes = mutableSetOf<String>()

        for (i in 0 until totalCount) {
            val qIndex = i + 1
            val qObj = questionsArray.optJSONObject(i)
            if (qObj == null) {
                errors.add("$qIndex-savol: Obyekt noto'g'ri formatda.")
                continue
            }

            val qId = qObj.optString("questionId", "").trim()
            if (qId.isEmpty()) {
                errors.add("$qIndex-savol: 'questionId' bo'sh yoki berilmagan.")
                continue
            }

            val text = qObj.optString("questionText", "").trim()
            if (text.isEmpty()) {
                errors.add("$qIndex-savol ($qId): 'questionText' bo'sh.")
                continue
            }

            // Options: array or object
            val optionsList = mutableListOf<String>()
            val optArray = qObj.optJSONArray("options")
            val optObj = qObj.optJSONObject("options")

            if (optArray != null) {
                if (optArray.length() != 4) {
                    errors.add("$qIndex-savol ($qId): 'options' soni to'g'ri emas (aynan 4 ta variant bo'lishi shart).")
                    continue
                }
                for (optIdx in 0 until 4) {
                    optionsList.add(optArray.optString(optIdx, "").trim())
                }
            } else if (optObj != null) {
                val optA = (optObj.optString("A", "").ifEmpty { optObj.optString("a", "") }).trim()
                val optB = (optObj.optString("B", "").ifEmpty { optObj.optString("b", "") }).trim()
                val optC = (optObj.optString("C", "").ifEmpty { optObj.optString("c", "") }).trim()
                val optD = (optObj.optString("D", "").ifEmpty { optObj.optString("d", "") }).trim()
                optionsList.addAll(listOf(optA, optB, optC, optD))
            } else {
                errors.add("$qIndex-savol ($qId): 'options' topilmadi.")
                continue
            }

            if (optionsList.any { it.isBlank() }) {
                errors.add("$qIndex-savol ($qId): Variantlardan biri bo'sh bo'lmasligi kerak.")
                continue
            }

            if (optionsList.map { it.lowercase() }.toSet().size < 4) {
                errors.add("$qIndex-savol ($qId): Variantlar takrorlanmasligi kerak.")
                continue
            }

            val rawCorrect = qObj.opt("correctAnswer")
            val resolvedCorrectAnswer: String? = when (rawCorrect) {
                is Int -> if (rawCorrect in 0..3) listOf("A", "B", "C", "D")[rawCorrect] else null
                is String -> {
                    val trimmedUpper = rawCorrect.trim().uppercase()
                    if (trimmedUpper in listOf("A", "B", "C", "D")) trimmedUpper
                    else {
                        val matchIndex = optionsList.indexOfFirst { it.equals(rawCorrect.trim(), ignoreCase = true) }
                        if (matchIndex in 0..3) listOf("A", "B", "C", "D")[matchIndex] else null
                    }
                }
                else -> null
            }

            if (resolvedCorrectAnswer == null) {
                errors.add("$qIndex-savol ($qId): 'correctAnswer' noto'g'ri yoki variantlar ichida topilmadi (qiymat: $rawCorrect).")
                continue
            }

            val topic = qObj.optString("topic", "").trim().ifEmpty {
                if (targetModule == "GEOMETRY") "Geometriya umumiy" else "Matematika umumiy"
            }

            val solution = qObj.optString("solution", "").trim()
            val explanation = qObj.optString("explanation", "").trim()
            if (solution.isEmpty() && explanation.isEmpty()) {
                errors.add("$qIndex-savol ($qId): 'solution' yoki 'explanation' bo'sh.")
                continue
            }

            val estimatedTime = qObj.optInt("estimatedTimeSeconds", 60)
            if (estimatedTime <= 0) {
                errors.add("$qIndex-savol ($qId): 'estimatedTimeSeconds' 0 dan katta bo'lishi kerak.")
                continue
            }

            val hash = computeCanonicalHash(text, optionsList)

            if (seenIds.contains(qId)) {
                duplicates.add("$qIndex-savol ($qId): Fayl ichida takrorlangan questionId.")
                continue
            }
            if (seenHashes.contains(hash)) {
                duplicates.add("$qIndex-savol ($qId): Fayl ichida aynan bir xil savol matni va variantlari takrorlangan.")
                continue
            }

            if (existingQuestionIds.contains(qId)) {
                duplicates.add("$qIndex-savol ($qId): Bazada shu questionId li savol allaqachon mavjud.")
                continue
            }
            if (existingQuestionHashes.contains(hash)) {
                duplicates.add("$qIndex-savol ($qId): Bazada aynan shu mazmundagi savol mavjud.")
                continue
            }

            seenIds.add(qId)
            seenHashes.add(hash)

            val diagram = qObj.optString("diagram", "").trim()
            val finalExplanation = if (explanation.isNotEmpty()) explanation else solution

            val questionEntity = QuestionEntity(
                customQuestionId = qId,
                subject = if (targetModule == "MATHEMATICS") "Matematika" else "Geometriya",
                module = targetModule,
                bookSource = if (targetModule == "GEOMETRY") "Puza Geometriya 1" else "Umumiy Matematika",
                topic = topic,
                subtopic = qObj.optString("subtopic", ""),
                difficulty = targetDifficulty,
                questionText = text,
                diagram = diagram,
                formulaUsed = qObj.optString("formulaUsed", ""),
                optionA = optionsList[0],
                optionB = optionsList[1],
                optionC = optionsList[2],
                optionD = optionsList[3],
                correctAnswer = resolvedCorrectAnswer,
                explanation = finalExplanation,
                solutionSteps = solution,
                estimatedTimeSeconds = estimatedTime,
                tags = qObj.optString("tags", ""),
                sourceType = "ADMIN_JSON_IMPORT",
                status = "PUBLISHED",
                isArchived = false,
                questionHash = hash
            )

            validQuestions.add(questionEntity)
        }

        return ImportPreview(
            totalCount = totalCount,
            validCount = validQuestions.size,
            duplicateCount = duplicates.size,
            errorCount = errors.size,
            errors = errors,
            duplicatesList = duplicates,
            validQuestions = validQuestions,
            subjectMatched = subjectMatched,
            difficultyMatched = difficultyMatched
        )
    }

    suspend fun executeImport(
        preview: ImportPreview,
        allowPartialImport: Boolean,
        questionDao: QuestionDao,
        conflictPolicy: ImportConflictPolicy = ImportConflictPolicy.SKIP_DUPLICATES,
        adminLogDao: AdminLogDao? = null,
        adminUserId: Long = 0,
        adminUsername: String = "admin"
    ): ImportResult {
        val report = ValidationReport(
            totalCount = preview.totalCount,
            validCount = preview.validCount,
            duplicateCount = preview.duplicateCount,
            errorCount = preview.errorCount,
            subjectMatch = ModuleMatchResult(preview.subjectMatched, null, "", ""),
            difficultyMatch = DifficultyMatchResult(preview.difficultyMatched, null, "", ""),
            errors = preview.errors.map { QuestionValidationError(0, null, "error", it) },
            duplicates = preview.duplicatesList.map { DuplicateMatch(0, "", DuplicateMatchType.DB_CUSTOM_ID, null, it) },
            validEntities = preview.validQuestions,
            entitiesToUpdate = emptyList(),
            rawJsonError = preview.rawJsonError
        )

        val bulkResult = AdminQuestionImportService.executeBulkImportTransaction(
            report = report,
            allowPartialImport = allowPartialImport,
            conflictPolicy = conflictPolicy,
            questionDao = questionDao,
            adminLogDao = adminLogDao,
            adminUserId = adminUserId,
            adminUsername = adminUsername
        )

        return bulkResult.toImportResult()
    }

    fun getMathSampleJson(): String {
        return """{
  "version": "1.0",
  "subject": "mathematics",
  "difficulty": "hard",
  "questions": [
    {
      "questionId": "math-hard-000001",
      "topic": "Tenglamalar",
      "questionText": "2x + 5 = 17 bo‘lsa, x ni toping.",
      "options": ["4", "5", "6", "7"],
      "correctAnswer": 2,
      "solution": "2x + 5 = 17 => 2x = 12 => x = 6",
      "explanation": "Tenglamani yechish orqali x=6 hosil bo‘ladi.",
      "estimatedTimeSeconds": 60,
      "diagram": ""
    },
    {
      "questionId": "math-hard-000002",
      "topic": "Logarifmik ifodalar",
      "questionText": "log2(x) + log2(x - 2) = 3 tenglamaning ildizini toping.",
      "options": ["2", "4", "-2", "6"],
      "correctAnswer": "B",
      "solution": "log2(x(x-2)) = 3 => x^2 - 2x = 8 => x^2 - 2x - 8 = 0 => (x-4)(x+2)=0. x>2 bo'lgani uchun x=4.",
      "explanation": "Logarifm aniqlanish sohasi x > 2 bo'lib, yagona ildiz x = 4 dir.",
      "estimatedTimeSeconds": 90,
      "diagram": ""
    }
  ]
}"""
    }

    fun getGeometrySampleJson(): String {
        return """{
  "version": "1.0",
  "subject": "geometry",
  "difficulty": "medium",
  "questions": [
    {
      "questionId": "geom-med-000001",
      "topic": "Uchburchaklar",
      "questionText": "To'g'ri burchakli uchburchakda katetlar 6 va 8 ga teng. Gipotenuzaga tushirilgan balandlikni toping.",
      "options": ["4.8", "5.0", "4.5", "5.2"],
      "correctAnswer": "A",
      "solution": "Gipotenuza c = sqrt(6^2 + 8^2) = 10. Maydon S = (6*8)/2 = 24. Balandlik h = 2S/c = 48/10 = 4.8.",
      "explanation": "h = (a*b)/c formulasi bo'yicha h = (6*8)/10 = 4.8.",
      "estimatedTimeSeconds": 80,
      "diagram": "RIGHT_TRIANGLE:a=6,b=8,c=10"
    }
  ]
}"""
    }
}
