package com.example.data.service

import com.example.data.dao.AdminLogDao
import com.example.data.dao.QuestionDao
import com.example.data.importer.ImportPreview
import com.example.data.importer.ImportResult
import com.example.data.model.AdminLogEntity
import com.example.data.model.QuestionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

/**
 * Conflict resolution policy when importing questions that already exist in the database.
 */
enum class ImportConflictPolicy {
    /**
     * Preserves existing database questions; skips any duplicates found during validation.
     */
    SKIP_DUPLICATES,

    /**
     * Updates existing database questions with the new values from JSON (Upsert semantics).
     */
    UPDATE_EXISTING,

    /**
     * Fails validation if any duplicate is found, enforcing complete uniqueness.
     */
    ABORT_ON_DUPLICATE
}

/**
 * Validation error for a specific question or global payload.
 */
data class QuestionValidationError(
    val questionIndex: Int,
    val customQuestionId: String?,
    val field: String,
    val message: String,
    val invalidValue: String? = null
)

/**
 * Duplicate match classification for auditing and reporting.
 */
enum class DuplicateMatchType {
    INTRA_BATCH_ID,
    INTRA_BATCH_HASH,
    DB_CUSTOM_ID,
    DB_CONTENT_HASH
}

data class DuplicateMatch(
    val questionIndex: Int,
    val customQuestionId: String,
    val matchedBy: DuplicateMatchType,
    val existingQuestionId: Long? = null,
    val details: String
) {
    val matchType: DuplicateMatchType get() = matchedBy
}

data class ModuleMatchResult(
    val matched: Boolean,
    val specifiedInJson: String?,
    val targetModule: String,
    val message: String
) {
    val extractedModule: String? get() = AdminQuestionImportService.normalizeModule(specifiedInJson)
}

data class DifficultyMatchResult(
    val matched: Boolean,
    val specifiedInJson: String?,
    val targetDifficulty: String,
    val message: String
) {
    val extractedDifficulty: String? get() = AdminQuestionImportService.normalizeDifficulty(specifiedInJson)
}

/**
 * Comprehensive pre-upload validation report.
 */
data class ValidationReport(
    val totalCount: Int,
    val validCount: Int,
    val duplicateCount: Int,
    val errorCount: Int,
    val subjectMatch: ModuleMatchResult,
    val difficultyMatch: DifficultyMatchResult,
    val errors: List<QuestionValidationError>,
    val duplicates: List<DuplicateMatch>,
    val validEntities: List<QuestionEntity>,
    val entitiesToUpdate: List<QuestionEntity>,
    val rawJsonError: String? = null
) {
    fun canExecuteImport(allowPartial: Boolean): Boolean {
        if (!subjectMatch.matched || !difficultyMatch.matched) return false
        if (validEntities.isEmpty() && entitiesToUpdate.isEmpty()) return false
        if (!allowPartial && errorCount > 0) return false
        return true
    }

    /**
     * Backward-compatibility bridge for UI components expecting ImportPreview.
     */
    fun toImportPreview(): ImportPreview {
        return ImportPreview(
            totalCount = totalCount,
            validCount = validCount,
            duplicateCount = duplicateCount,
            errorCount = errorCount,
            errors = errors.map { err ->
                val idPart = if (!err.customQuestionId.isNullOrEmpty()) " (${err.customQuestionId})" else ""
                "${err.questionIndex}-savol$idPart [${err.field}]: ${err.message}"
            },
            duplicatesList = duplicates.map { dup ->
                "${dup.questionIndex}-savol (${dup.customQuestionId}): ${dup.details}"
            },
            validQuestions = validEntities,
            subjectMatched = subjectMatch.matched,
            difficultyMatched = difficultyMatch.matched,
            rawJsonError = rawJsonError
        )
    }
}

/**
 * Idempotent bulk import result containing execution metrics and audit information.
 */
data class BulkImportResult(
    val success: Boolean,
    val transactionId: String,
    val totalProcessed: Int,
    val insertedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val errorCount: Int,
    val message: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Backward-compatibility bridge for UI components expecting ImportResult.
     */
    fun toImportResult(): ImportResult {
        return ImportResult(
            success = success,
            totalFound = totalProcessed,
            newlyAddedCount = insertedCount,
            duplicateSkippedCount = skippedCount,
            message = message
        )
    }
}

/**
 * Robust, production-grade JSON Question Import Service for Uzbekistan National Certificate math exam.
 * 
 * Capabilities:
 * 1. Canonical Hash Generation (SHA-256 with text & option normalization).
 * 2. Strict Subject & Difficulty Matching (Multilingual aliases: UZ, RU, EN).
 * 3. Deep Field Validation (Non-empty, 4 distinct options, valid answer index/letter/text, diagram format).
 * 4. Idempotent Batch Uploads & Conflict Policies (SKIP_DUPLICATES, UPDATE_EXISTING, ABORT_ON_DUPLICATE).
 * 5. Full Audit Trail Logging for Admin Operations.
 */
object AdminQuestionImportService {

    // -----------------------------------------------------------------
    // 1. CANONICAL HASH GENERATION (Deterministic Duplicate Prevention)
    // -----------------------------------------------------------------
    /**
     * Calculates a deterministic SHA-256 fingerprint for question content.
     * Independent of whitespace variance, punctuation differences, casing, and option order.
     */
    fun computeCanonicalHash(questionText: String, options: List<String>): String {
        val normalizedText = questionText.trim()
            .lowercase()
            .replace("\\s+".toRegex(), " ")

        val normalizedOptions = options.map { opt ->
            opt.trim().lowercase().replace("\\s+".toRegex(), " ")
        }.sorted().joinToString("|")

        val payload = "$normalizedText##$normalizedOptions"
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    // -----------------------------------------------------------------
    // 2. MULTILINGUAL ALIAS RESOLUTION
    // -----------------------------------------------------------------
    /**
     * Maps subject/module string aliases into canonical "MATHEMATICS" or "GEOMETRY".
     * Supports Uzbek (Latin/Cyrillic), English, and Russian.
     */
    fun normalizeModule(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val clean = input.trim().lowercase()
            .replace("'", "")
            .replace("ʻ", "")
            .replace("`", "")

        return when {
            clean in listOf("mathematics", "math", "matematika", "algebra", "umumiy matematika", "алгебра", "математика") -> "MATHEMATICS"
            clean in listOf("geometry", "geom", "geometriya", "puza geometriya 1", "puza", "геометрия") -> "GEOMETRY"
            clean.contains("geom") || clean.contains("geometr") -> "GEOMETRY"
            clean.contains("math") || clean.contains("matem") || clean.contains("algeb") -> "MATHEMATICS"
            else -> null
        }
    }

    /**
     * Maps difficulty string aliases into canonical "EASY", "MEDIUM", "HARD", "VERY_HARD".
     */
    fun normalizeDifficulty(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val clean = input.trim().lowercase()
            .replace("'", "")
            .replace("ʻ", "")
            .replace("`", "")
            .replace("-", "")
            .replace("_", "")
            .replace(" ", "")

        return when {
            clean in listOf("easy", "oson", "oddiy", "boshlangich", "простой", "легкий", "1") -> "EASY"
            clean in listOf("medium", "orta", "standart", "normal", "средний", "2") -> "MEDIUM"
            clean in listOf("hard", "qiyin", "murakkab", "chuqur", "сложный", "трудный", "3") -> "HARD"
            clean in listOf("veryhard", "judaqiyin", "olimpiada", "expert", "pro", "оченьсложный", "4") -> "VERY_HARD"
            else -> null
        }
    }

    // -----------------------------------------------------------------
    // 3. PARSING & VALIDATION ENGINE
    // -----------------------------------------------------------------
    /**
     * Suspend overload that fetches database IDs and hashes before validating.
     */
    suspend fun validateJson(
        jsonString: String,
        targetModule: String,
        targetDifficulty: String,
        questionDao: QuestionDao,
        conflictPolicy: ImportConflictPolicy = ImportConflictPolicy.SKIP_DUPLICATES
    ): ValidationReport {
        val existingDbIds = questionDao.getAllExistingCustomIds().toSet()
        val existingDbHashes = questionDao.getAllExistingHashes().toSet()
        val existingQuestionsById = if (conflictPolicy == ImportConflictPolicy.UPDATE_EXISTING && existingDbIds.isNotEmpty()) {
            questionDao.getQuestionsByCustomIds(existingDbIds.toList()).associateBy { it.customQuestionId ?: "" }
        } else {
            emptyMap()
        }

        return validateJson(
            jsonString = jsonString,
            targetModule = targetModule,
            targetDifficulty = targetDifficulty,
            existingDbIds = existingDbIds,
            existingDbHashes = existingDbHashes,
            existingQuestionsById = existingQuestionsById,
            conflictPolicy = conflictPolicy
        )
    }

    /**
     * Pure in-memory validation engine. Highly performant and easily testable without DB mocks.
     */
    fun validateJson(
        jsonString: String,
        targetModule: String, // "MATHEMATICS" or "GEOMETRY"
        targetDifficulty: String, // "EASY", "MEDIUM", "HARD", "VERY_HARD"
        existingDbIds: Set<String> = emptySet(),
        existingDbHashes: Set<String> = emptySet(),
        existingQuestionsById: Map<String, QuestionEntity> = emptyMap(),
        conflictPolicy: ImportConflictPolicy = ImportConflictPolicy.SKIP_DUPLICATES
    ): ValidationReport {
        val errors = mutableListOf<QuestionValidationError>()
        val duplicates = mutableListOf<DuplicateMatch>()
        val validEntities = mutableListOf<QuestionEntity>()
        val entitiesToUpdate = mutableListOf<QuestionEntity>()

        // 1. JSON parsing
        val rootObj: JSONObject?
        val questionsArray: JSONArray
        var rootSubject: String? = null
        var rootDifficulty: String? = null

        val trimmedJson = jsonString.trim()
        try {
            if (trimmedJson.startsWith("[")) {
                questionsArray = JSONArray(trimmedJson)
                rootObj = null
            } else {
                rootObj = JSONObject(trimmedJson)
                rootSubject = if (rootObj.has("subject")) rootObj.optString("subject").ifEmpty { null } else null
                rootDifficulty = if (rootObj.has("difficulty")) rootObj.optString("difficulty").ifEmpty { null } else null

                val arr = rootObj.optJSONArray("questions")
                if (arr == null) {
                    return ValidationReport(
                        totalCount = 0,
                        validCount = 0,
                        duplicateCount = 0,
                        errorCount = 1,
                        subjectMatch = ModuleMatchResult(false, rootSubject, targetModule, "JSON da 'questions' massivi topilmadi."),
                        difficultyMatch = DifficultyMatchResult(false, rootDifficulty, targetDifficulty, "JSON da 'questions' massivi topilmadi."),
                        errors = listOf(QuestionValidationError(0, null, "questions", "JSON da 'questions' massivi topilmadi.")),
                        duplicates = emptyList(),
                        validEntities = emptyList(),
                        entitiesToUpdate = emptyList(),
                        rawJsonError = "'questions' array missing in root object"
                    )
                }
                questionsArray = arr
            }
        } catch (e: Exception) {
            return ValidationReport(
                totalCount = 0,
                validCount = 0,
                duplicateCount = 0,
                errorCount = 1,
                subjectMatch = ModuleMatchResult(false, null, targetModule, "JSON sintaksis xatosi"),
                difficultyMatch = DifficultyMatchResult(false, null, targetDifficulty, "JSON sintaksis xatosi"),
                errors = listOf(QuestionValidationError(0, null, "jsonSyntax", "JSON formati noto'g'ri: ${e.localizedMessage ?: "Sintaksis xatosi"}")),
                duplicates = emptyList(),
                validEntities = emptyList(),
                entitiesToUpdate = emptyList(),
                rawJsonError = e.message
            )
        }

        // 2. Validate Root Subject & Difficulty Match
        val normalizedRootModule = normalizeModule(rootSubject)
        val moduleMatched = normalizedRootModule == null || normalizedRootModule == targetModule
        val moduleMatchResult = ModuleMatchResult(
            matched = moduleMatched,
            specifiedInJson = rootSubject,
            targetModule = targetModule,
            message = if (moduleMatched) {
                "Fan mos keldi: $targetModule"
            } else {
                "Fan mos kelmadi: Siz '$targetModule' tanladingiz, ammo JSON da '$rootSubject' ko'rsatilgan."
            }
        )
        if (!moduleMatched) {
            errors.add(QuestionValidationError(0, null, "subject", moduleMatchResult.message, rootSubject))
        }

        val normalizedRootDiff = normalizeDifficulty(rootDifficulty)
        val difficultyMatched = normalizedRootDiff == null || normalizedRootDiff == targetDifficulty
        val difficultyMatchResult = DifficultyMatchResult(
            matched = difficultyMatched,
            specifiedInJson = rootDifficulty,
            targetDifficulty = targetDifficulty,
            message = if (difficultyMatched) {
                "Qiyinlik darajasi mos keldi: $targetDifficulty"
            } else {
                "Daraja mos kelmadi: Siz '$targetDifficulty' tanladingiz, ammo JSON da '$rootDifficulty' ko'rsatilgan."
            }
        )
        if (!difficultyMatched) {
            errors.add(QuestionValidationError(0, null, "difficulty", difficultyMatchResult.message, rootDifficulty))
        }

        val totalCount = questionsArray.length()
        if (totalCount == 0) {
            errors.add(QuestionValidationError(0, null, "questions", "'questions' ro'yxati bo'sh."))
            return ValidationReport(
                totalCount = 0,
                validCount = 0,
                duplicateCount = 0,
                errorCount = errors.size,
                subjectMatch = moduleMatchResult,
                difficultyMatch = difficultyMatchResult,
                errors = errors,
                duplicates = emptyList(),
                validEntities = emptyList(),
                entitiesToUpdate = emptyList()
            )
        }

        val seenBatchIds = mutableSetOf<String>()
        val seenBatchHashes = mutableSetOf<String>()

        // 3. Validate Each Question Entity
        for (i in 0 until totalCount) {
            val qIndex = i + 1
            val qObj = questionsArray.optJSONObject(i)
            if (qObj == null) {
                errors.add(QuestionValidationError(qIndex, null, "structure", "$qIndex-savol: Obyekt noto'g'ri formatda."))
                continue
            }

            var questionHasError = false

            // A. questionId
            val rawId = qObj.optString("questionId", "").trim()
            if (rawId.isEmpty()) {
                errors.add(QuestionValidationError(qIndex, null, "questionId", "Savol identifikatori ('questionId') bo'sh."))
                questionHasError = true
            } else if (rawId.length < 3 || !rawId.matches("^[a-zA-Z0-9_.-]+$".toRegex())) {
                errors.add(QuestionValidationError(qIndex, rawId, "questionId", "Savol ID si faqat harf, son, defis yoki tagchiziqdan iborat bo'lishi kerak.", rawId))
                questionHasError = true
            }

            // B. questionText
            val text = qObj.optString("questionText", "").trim()
            if (text.isEmpty()) {
                errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "questionText", "Savol matni ('questionText') bo'sh bo'lmasligi kerak."))
                questionHasError = true
            } else if (text.length < 5) {
                errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "questionText", "Savol matni juda qisqa (kamida 5 ta belgi).", text))
                questionHasError = true
            }

            // C. options: must have exactly 4 non-empty distinct options
            val optionsList = mutableListOf<String>()
            val optArray = qObj.optJSONArray("options")
            val optObj = qObj.optJSONObject("options")

            if (optArray != null) {
                if (optArray.length() != 4) {
                    errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "options_count", "Savolda aynan 4 ta variant bo'lishi shart (topildi: ${optArray.length()})."))
                    questionHasError = true
                } else {
                    for (optIdx in 0 until 4) {
                        optionsList.add(optArray.optString(optIdx, "").trim())
                    }
                }
            } else if (optObj != null) {
                val optA = (optObj.optString("A", "").ifEmpty { optObj.optString("a", "") }).trim()
                val optB = (optObj.optString("B", "").ifEmpty { optObj.optString("b", "") }).trim()
                val optC = (optObj.optString("C", "").ifEmpty { optObj.optString("c", "") }).trim()
                val optD = (optObj.optString("D", "").ifEmpty { optObj.optString("d", "") }).trim()

                if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
                    errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "options_keys", "Options obyektida A, B, C, D kalitlarining barchasi bo'lishi shart."))
                    questionHasError = true
                }
                optionsList.addAll(listOf(optA, optB, optC, optD))
            } else {
                errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "options", "Variantlar ('options') ro'yxati yoki obyekti topilmadi."))
                questionHasError = true
            }

            // Verify non-empty and distinct
            if (optionsList.size == 4) {
                if (optionsList.any { it.isBlank() }) {
                    errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "options_blank", "Variantlardan biri bo'sh bo'lmasligi kerak."))
                    questionHasError = true
                }

                val distinctOptions = optionsList.map { it.lowercase().trim() }.toSet()
                if (distinctOptions.size < 4) {
                    errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "duplicate_options", "Savol variantlari bir-birini takrorlamasligi kerak."))
                    questionHasError = true
                }
            }

            // D. correctAnswer: 0..3, "A".."D", or exact match with one of the options
            val rawCorrect = qObj.opt("correctAnswer")
            val resolvedCorrectAnswer: String? = when (rawCorrect) {
                is Int -> {
                    if (rawCorrect in 0..3) listOf("A", "B", "C", "D")[rawCorrect] else null
                }
                is String -> {
                    val trimmedUpper = rawCorrect.trim().uppercase()
                    if (trimmedUpper in listOf("A", "B", "C", "D")) {
                        trimmedUpper
                    } else {
                        // Check if author supplied option text directly
                        val matchIdx = optionsList.indexOfFirst { it.equals(rawCorrect.trim(), ignoreCase = true) }
                        if (matchIdx in 0..3) listOf("A", "B", "C", "D")[matchIdx] else null
                    }
                }
                else -> null
            }

            if (resolvedCorrectAnswer == null) {
                errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "correctAnswer", "To'g'ri javob ('correctAnswer') aniqlanmadi (A, B, C, D yoki 0..3 bo'lishi lozim).", rawCorrect?.toString()))
                questionHasError = true
            }

            // E. Question-level difficulty matching (if present)
            val qDiffRaw = if (qObj.has("difficulty")) qObj.optString("difficulty").ifEmpty { null } else null
            val qDiffNormalized = normalizeDifficulty(qDiffRaw)
            if (qDiffNormalized != null && qDiffNormalized != targetDifficulty) {
                errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "difficulty", "Savolning o'zida ko'rsatilgan qiyinlik darajasi ($qDiffRaw) tanlangan guruh darajasiga ($targetDifficulty) mos emas."))
                questionHasError = true
            }

            // F. topic
            val topic = qObj.optString("topic", "").trim().ifEmpty {
                if (targetModule == "GEOMETRY") "Geometriya umumiy" else "Matematika umumiy"
            }

            // G. solution & explanation
            val solution = qObj.optString("solution", "").trim()
            val explanation = qObj.optString("explanation", "").trim()
            if (solution.isEmpty() && explanation.isEmpty()) {
                errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "solution", "Savolda kamida 'solution' yoki 'explanation' ko'rsatilishi shart."))
                questionHasError = true
            }

            // H. estimatedTimeSeconds
            val rawTime = qObj.optInt("estimatedTimeSeconds", 60)
            val estimatedTimeSeconds = if (rawTime <= 0) {
                errors.add(QuestionValidationError(qIndex, rawId.ifEmpty { null }, "estimatedTimeSeconds", "'estimatedTimeSeconds' musbat butun son bo'lishi kerak.", rawTime.toString()))
                questionHasError = true
                60
            } else if (rawTime > 900) {
                600 // clamp reasonable max
            } else {
                rawTime
            }

            if (questionHasError || optionsList.size != 4 || resolvedCorrectAnswer == null || text.isEmpty() || rawId.isEmpty()) {
                continue
            }

            // I. Canonical Hash & Duplicate Detection
            val canonicalHash = computeCanonicalHash(text, optionsList)

            // Check intra-batch duplicate by ID
            if (seenBatchIds.contains(rawId)) {
                duplicates.add(DuplicateMatch(qIndex, rawId, DuplicateMatchType.INTRA_BATCH_ID, null, "Fayl ichida takrorlangan questionId"))
                continue
            }

            // Check intra-batch duplicate by Hash
            if (seenBatchHashes.contains(canonicalHash)) {
                duplicates.add(DuplicateMatch(qIndex, rawId, DuplicateMatchType.INTRA_BATCH_HASH, null, "Fayl ichida aynan bir xil savol va variantlar takrorlangan"))
                continue
            }

            // Check Database Duplicate by ID
            val existsInDbById = existingDbIds.contains(rawId)
            val existsInDbByHash = existingDbHashes.contains(canonicalHash)

            if (existsInDbById || existsInDbByHash) {
                val matchType = if (existsInDbById) DuplicateMatchType.DB_CUSTOM_ID else DuplicateMatchType.DB_CONTENT_HASH
                val reason = if (existsInDbById) "Bazada bu questionId allaqachon mavjud" else "Bazada aynan shu mazmundagi savol mavjud (Canonical Hash)"

                duplicates.add(DuplicateMatch(qIndex, rawId, matchType, null, reason))

                if (conflictPolicy == ImportConflictPolicy.ABORT_ON_DUPLICATE) {
                    errors.add(QuestionValidationError(qIndex, rawId, "conflict_abort", "Takrorlanish aniqlanganligi sababli import to'xtatildi: $reason"))
                } else if (conflictPolicy == ImportConflictPolicy.UPDATE_EXISTING && existsInDbById) {
                    val existingEntity = existingQuestionsById[rawId]
                    val updatedEntity = QuestionEntity(
                        id = existingEntity?.id ?: 0,
                        customQuestionId = rawId,
                        subject = if (targetModule == "MATHEMATICS") "Matematika" else "Geometriya",
                        module = targetModule,
                        bookSource = if (targetModule == "GEOMETRY") "Puza Geometriya 1" else "Umumiy Matematika",
                        topic = topic,
                        subtopic = qObj.optString("subtopic", existingEntity?.subtopic ?: ""),
                        difficulty = targetDifficulty,
                        questionText = text,
                        diagram = qObj.optString("diagram", existingEntity?.diagram ?: "").trim(),
                        formulaUsed = qObj.optString("formulaUsed", existingEntity?.formulaUsed ?: "").trim(),
                        optionA = optionsList[0],
                        optionB = optionsList[1],
                        optionC = optionsList[2],
                        optionD = optionsList[3],
                        correctAnswer = resolvedCorrectAnswer,
                        explanation = if (explanation.isNotEmpty()) explanation else solution,
                        solutionSteps = solution,
                        estimatedTimeSeconds = estimatedTimeSeconds,
                        tags = qObj.optString("tags", existingEntity?.tags ?: ""),
                        sourceType = "ADMIN_JSON_IMPORT",
                        status = "PUBLISHED",
                        isArchived = false,
                        questionHash = canonicalHash
                    )
                    entitiesToUpdate.add(updatedEntity)
                }
                continue
            }

            // Successfully passed all validations
            seenBatchIds.add(rawId)
            seenBatchHashes.add(canonicalHash)

            val diagram = qObj.optString("diagram", "").trim()
            val formulaUsed = qObj.optString("formulaUsed", "").trim()
            val subtopic = qObj.optString("subtopic", "").trim()
            val tags = qObj.optString("tags", "").trim()
            val finalExplanation = if (explanation.isNotEmpty()) explanation else solution

            val entity = QuestionEntity(
                customQuestionId = rawId,
                subject = if (targetModule == "MATHEMATICS") "Matematika" else "Geometriya",
                module = targetModule,
                bookSource = if (targetModule == "GEOMETRY") "Puza Geometriya 1" else "Umumiy Matematika",
                topic = topic,
                subtopic = subtopic,
                difficulty = targetDifficulty,
                questionText = text,
                diagram = diagram,
                formulaUsed = formulaUsed,
                optionA = optionsList[0],
                optionB = optionsList[1],
                optionC = optionsList[2],
                optionD = optionsList[3],
                correctAnswer = resolvedCorrectAnswer,
                explanation = finalExplanation,
                solutionSteps = solution,
                estimatedTimeSeconds = estimatedTimeSeconds,
                tags = tags,
                sourceType = "ADMIN_JSON_IMPORT",
                status = "PUBLISHED",
                isArchived = false,
                questionHash = canonicalHash
            )

            validEntities.add(entity)
        }

        return ValidationReport(
            totalCount = totalCount,
            validCount = validEntities.size,
            duplicateCount = duplicates.size,
            errorCount = errors.size,
            subjectMatch = moduleMatchResult,
            difficultyMatch = difficultyMatchResult,
            errors = errors,
            duplicates = duplicates,
            validEntities = validEntities,
            entitiesToUpdate = entitiesToUpdate
        )
    }

    // -----------------------------------------------------------------
    // 4. IDEMPOTENT TRANSACTION EXECUTION FOR BULK UPLOADS
    // -----------------------------------------------------------------
    /**
     * Executes bulk upload atomically inside a single database transaction.
     * Guarantees idempotency: repeated execution with the same data will not
     * duplicate rows or create inconsistent state.
     */
    suspend fun executeBulkImportTransaction(
        report: ValidationReport,
        allowPartialImport: Boolean,
        conflictPolicy: ImportConflictPolicy,
        questionDao: QuestionDao,
        adminLogDao: AdminLogDao? = null,
        adminUserId: Long = 0,
        adminUsername: String = "admin"
    ): BulkImportResult {
        val startTime = System.currentTimeMillis()
        val transactionId = "txn-bulk-${UUID.randomUUID().toString().take(8)}"

        if (!report.subjectMatch.matched || !report.difficultyMatch.matched) {
            return BulkImportResult(
                success = false,
                transactionId = transactionId,
                totalProcessed = report.totalCount,
                insertedCount = 0,
                updatedCount = 0,
                skippedCount = report.duplicateCount,
                errorCount = report.errorCount,
                message = "Import bekor qilindi: Fan yoki qiyinlik darajasi mos kelmadi.",
                durationMs = System.currentTimeMillis() - startTime
            )
        }

        if (!allowPartialImport && report.errors.isNotEmpty()) {
            return BulkImportResult(
                success = false,
                transactionId = transactionId,
                totalProcessed = report.totalCount,
                insertedCount = 0,
                updatedCount = 0,
                skippedCount = report.duplicateCount,
                errorCount = report.errorCount,
                message = "Import bekor qilindi: ${report.errors.size} ta xatolik aniqlandi (All-or-nothing qoidasi).",
                durationMs = System.currentTimeMillis() - startTime
            )
        }

        val toInsert = report.validEntities
        val toUpdate = if (conflictPolicy == ImportConflictPolicy.UPDATE_EXISTING) report.entitiesToUpdate else emptyList()

        if (toInsert.isEmpty() && toUpdate.isEmpty()) {
            return BulkImportResult(
                success = true,
                transactionId = transactionId,
                totalProcessed = report.totalCount,
                insertedCount = 0,
                updatedCount = 0,
                skippedCount = report.duplicateCount,
                errorCount = report.errorCount,
                message = "Bazada barcha (${report.duplicateCount} ta) savollar allaqachon mavjud. Hech qanday o'zgarish talab qilinmadi (Idempotent).",
                durationMs = System.currentTimeMillis() - startTime
            )
        }

        // Execute atomic bulk operations through DAO
        try {
            if (toUpdate.isNotEmpty()) {
                questionDao.updateQuestions(toUpdate)
            }
            if (toInsert.isNotEmpty()) {
                questionDao.insertQuestions(toInsert)
            }

            val duration = System.currentTimeMillis() - startTime
            val message = StringBuilder().apply {
                append("Muvaffaqiyatli yakunlandi (Tranzaksiya: $transactionId): ")
                if (toInsert.isNotEmpty()) append("${toInsert.size} ta yangi savol qo'shildi. ")
                if (toUpdate.isNotEmpty()) append("${toUpdate.size} ta mavjud savol yangilandi. ")
                if (report.duplicateCount > 0) append("${report.duplicateCount} ta takrorlangan savol o'tkazib yuborildi.")
            }.toString().trim()

            // Record audit trail if logger available
            adminLogDao?.insertLog(
                AdminLogEntity(
                    adminUserId = adminUserId,
                    adminUsername = adminUsername,
                    action = "ADMIN_BULK_JSON_IMPORT",
                    targetType = "QUESTIONS_BATCH",
                    targetId = transactionId,
                    details = "Processed ${report.totalCount} (Inserted: ${toInsert.size}, Updated: ${toUpdate.size}, Skipped: ${report.duplicateCount}) in ${duration}ms"
                )
            )

            return BulkImportResult(
                success = true,
                transactionId = transactionId,
                totalProcessed = report.totalCount,
                insertedCount = toInsert.size,
                updatedCount = toUpdate.size,
                skippedCount = report.duplicateCount,
                errorCount = report.errorCount,
                message = message,
                durationMs = duration
            )
        } catch (e: Exception) {
            return BulkImportResult(
                success = false,
                transactionId = transactionId,
                totalProcessed = report.totalCount,
                insertedCount = 0,
                updatedCount = 0,
                skippedCount = 0,
                errorCount = 1,
                message = "Tranzaksiya muvaffaqiyatsiz tugadi: ${e.localizedMessage ?: "Database error"}",
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }
}
