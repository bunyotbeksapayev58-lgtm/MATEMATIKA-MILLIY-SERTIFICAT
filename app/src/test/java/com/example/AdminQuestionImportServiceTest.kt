package com.example

import com.example.data.service.AdminQuestionImportService
import com.example.data.service.DifficultyMatchResult
import com.example.data.service.DuplicateMatchType
import com.example.data.service.ImportConflictPolicy
import com.example.data.service.ModuleMatchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdminQuestionImportServiceTest {

    @Test
    fun `computeCanonicalHash creates identical hashes for semantically identical questions`() {
        val qText1 = "2x + 5 = 17 tenglamani yeching."
        val opts1 = listOf("x = 4", "x = 6", "x = 8", "x = 10")

        val qText2 = "  2x  +  5 = 17   tenglamani  yeching.  "
        val opts2 = listOf("x = 10", "x = 4", "x = 6", "x = 8") // different order

        val hash1 = AdminQuestionImportService.computeCanonicalHash(qText1, opts1)
        val hash2 = AdminQuestionImportService.computeCanonicalHash(qText2, opts2)

        assertEquals("Canonical hashes must be identical regardless of whitespace and option order", hash1, hash2)
    }

    @Test
    fun `normalizeModule handles multilingual aliases correctly`() {
        // Mathematics
        assertEquals("MATHEMATICS", AdminQuestionImportService.normalizeModule("MATHEMATICS"))
        assertEquals("MATHEMATICS", AdminQuestionImportService.normalizeModule("Matematika"))
        assertEquals("MATHEMATICS", AdminQuestionImportService.normalizeModule("math"))
        assertEquals("MATHEMATICS", AdminQuestionImportService.normalizeModule("алгебра"))
        assertEquals("MATHEMATICS", AdminQuestionImportService.normalizeModule("MATEMATIKA"))

        // Geometry
        assertEquals("GEOMETRY", AdminQuestionImportService.normalizeModule("GEOMETRY"))
        assertEquals("GEOMETRY", AdminQuestionImportService.normalizeModule("Geometriya"))
        assertEquals("GEOMETRY", AdminQuestionImportService.normalizeModule("geom"))
        assertEquals("GEOMETRY", AdminQuestionImportService.normalizeModule("геометрия"))
        assertEquals("GEOMETRY", AdminQuestionImportService.normalizeModule("Puza Geometriya 1"))

        // Unknown
        assertNull(AdminQuestionImportService.normalizeModule("fizika"))
        assertNull(AdminQuestionImportService.normalizeModule(null))
    }

    @Test
    fun `normalizeDifficulty handles Uzbek, English, and Russian aliases`() {
        // EASY
        assertEquals("EASY", AdminQuestionImportService.normalizeDifficulty("EASY"))
        assertEquals("EASY", AdminQuestionImportService.normalizeDifficulty("oson"))
        assertEquals("EASY", AdminQuestionImportService.normalizeDifficulty("простой"))
        assertEquals("EASY", AdminQuestionImportService.normalizeDifficulty("1"))

        // MEDIUM
        assertEquals("MEDIUM", AdminQuestionImportService.normalizeDifficulty("MEDIUM"))
        assertEquals("MEDIUM", AdminQuestionImportService.normalizeDifficulty("o'rta"))
        assertEquals("MEDIUM", AdminQuestionImportService.normalizeDifficulty("orta"))
        assertEquals("MEDIUM", AdminQuestionImportService.normalizeDifficulty("средний"))
        assertEquals("MEDIUM", AdminQuestionImportService.normalizeDifficulty("2"))

        // HARD
        assertEquals("HARD", AdminQuestionImportService.normalizeDifficulty("HARD"))
        assertEquals("HARD", AdminQuestionImportService.normalizeDifficulty("qiyin"))
        assertEquals("HARD", AdminQuestionImportService.normalizeDifficulty("сложный"))
        assertEquals("HARD", AdminQuestionImportService.normalizeDifficulty("3"))

        // VERY_HARD
        assertEquals("VERY_HARD", AdminQuestionImportService.normalizeDifficulty("VERY_HARD"))
        assertEquals("VERY_HARD", AdminQuestionImportService.normalizeDifficulty("juda qiyin"))
        assertEquals("VERY_HARD", AdminQuestionImportService.normalizeDifficulty("olimpiada"))
        assertEquals("VERY_HARD", AdminQuestionImportService.normalizeDifficulty("4"))

        // Unknown
        assertNull(AdminQuestionImportService.normalizeDifficulty("noma'lum"))
        assertNull(AdminQuestionImportService.normalizeDifficulty(null))
    }

    @Test
    fun `validateJson detects module and difficulty mismatches accurately`() {
        val json = """
            {
              "subject": "geometriya",
              "difficulty": "hard",
              "questions": []
            }
        """.trimIndent()

        // Checking against target MATHEMATICS and EASY
        val report = AdminQuestionImportService.validateJson(
            jsonString = json,
            targetModule = "MATHEMATICS",
            targetDifficulty = "EASY",
            existingDbIds = emptySet(),
            existingDbHashes = emptySet()
        )

        assertFalse("Subject should mismatch", report.subjectMatch.matched)
        assertEquals("GEOMETRY", report.subjectMatch.extractedModule)
        assertFalse("Difficulty should mismatch", report.difficultyMatch.matched)
        assertEquals("HARD", report.difficultyMatch.extractedDifficulty)
        assertTrue(report.errors.any { it.field == "subject" })
        assertTrue(report.errors.any { it.field == "difficulty" })
    }

    @Test
    fun `validateJson performs comprehensive field validations`() {
        val badJson = """
            {
              "subject": "mathematics",
              "difficulty": "medium",
              "questions": [
                {
                  "questionId": "",
                  "questionText": "",
                  "options": ["A", "B", "C"],
                  "correctAnswer": 5,
                  "solution": ""
                },
                {
                  "questionId": "q-valid-1",
                  "questionText": "Savol 2",
                  "options": ["10", "10", "20", "30"],
                  "correctAnswer": "A",
                  "solution": "Yechim bor"
                }
              ]
            }
        """.trimIndent()

        val report = AdminQuestionImportService.validateJson(
            jsonString = badJson,
            targetModule = "MATHEMATICS",
            targetDifficulty = "MEDIUM",
            existingDbIds = emptySet(),
            existingDbHashes = emptySet()
        )

        assertEquals(2, report.totalCount)
        assertEquals(0, report.validCount)
        assertTrue("Should contain multiple errors", report.errors.size >= 4)

        // Verifying specific errors
        assertTrue(report.errors.any { it.field == "questionId" })
        assertTrue(report.errors.any { it.field == "questionText" })
        assertTrue(report.errors.any { it.field == "options_count" })
        assertTrue(report.errors.any { it.field == "duplicate_options" })
    }

    @Test
    fun `validateJson correctly handles correctAnswer formats`() {
        val json = """
            {
              "subject": "mathematics",
              "difficulty": "medium",
              "questions": [
                {
                  "questionId": "q-idx-answer",
                  "questionText": "Indeks orqali to'g'ri javob",
                  "options": ["Bir", "Ikki", "Uch", "To'rt"],
                  "correctAnswer": 2,
                  "solution": "Uchinchi element ya'ni C"
                },
                {
                  "questionId": "q-text-answer",
                  "questionText": "Matn orqali to'g'ri javob",
                  "options": ["Bir", "Ikki", "Uch", "To'rt"],
                  "correctAnswer": "Ikki",
                  "solution": "Ikkinchi variant ya'ni B"
                },
                {
                  "questionId": "q-letter-answer",
                  "questionText": "Harf orqali to'g'ri javob",
                  "options": ["Bir", "Ikki", "Uch", "To'rt"],
                  "correctAnswer": "D",
                  "solution": "To'rtinchi variant"
                }
              ]
            }
        """.trimIndent()

        val report = AdminQuestionImportService.validateJson(
            jsonString = json,
            targetModule = "MATHEMATICS",
            targetDifficulty = "MEDIUM",
            existingDbIds = emptySet(),
            existingDbHashes = emptySet()
        )

        assertEquals(3, report.totalCount)
        assertEquals(3, report.validCount)
        assertEquals(0, report.errorCount)

        assertEquals("C", report.validEntities[0].correctAnswer)
        assertEquals("B", report.validEntities[1].correctAnswer)
        assertEquals("D", report.validEntities[2].correctAnswer)
    }

    @Test
    fun `validateJson detects intra-batch and database duplicates with idempotent handling`() {
        val jsonWithDuplicates = """
            {
              "questions": [
                {
                  "questionId": "existing-in-db-id",
                  "questionText": "Yangi matn 1",
                  "options": ["A", "B", "C", "D"],
                  "correctAnswer": "A",
                  "solution": "Yechim 1"
                },
                {
                  "questionId": "batch-unique-1",
                  "questionText": "Bazada aynan shu savol bor",
                  "options": ["A", "B", "C", "D"],
                  "correctAnswer": "A",
                  "solution": "Yechim 2"
                },
                {
                  "questionId": "batch-unique-2",
                  "questionText": "Yangi unikal savol",
                  "options": ["1", "2", "3", "4"],
                  "correctAnswer": "A",
                  "solution": "Yechim 3"
                },
                {
                  "questionId": "batch-unique-2",
                  "questionText": "Takroriy questionId batch ichida",
                  "options": ["5", "6", "7", "8"],
                  "correctAnswer": "A",
                  "solution": "Yechim 4"
                }
              ]
            }
        """.trimIndent()

        val existingHash = AdminQuestionImportService.computeCanonicalHash("Bazada aynan shu savol bor", listOf("A", "B", "C", "D"))

        val report = AdminQuestionImportService.validateJson(
            jsonString = jsonWithDuplicates,
            targetModule = "MATHEMATICS",
            targetDifficulty = "MEDIUM",
            existingDbIds = setOf("existing-in-db-id"),
            existingDbHashes = setOf(existingHash),
            conflictPolicy = ImportConflictPolicy.SKIP_DUPLICATES
        )

        assertEquals(4, report.totalCount)
        assertEquals(1, report.validCount) // Only batch-unique-2 first instance is valid
        assertEquals(3, report.duplicateCount)

        assertTrue(report.duplicates.any { it.matchType == DuplicateMatchType.DB_CUSTOM_ID })
        assertTrue(report.duplicates.any { it.matchType == DuplicateMatchType.DB_CONTENT_HASH })
        assertTrue(report.duplicates.any { it.matchType == DuplicateMatchType.INTRA_BATCH_ID })
    }

    @Test
    fun `validateJson with UPDATE_EXISTING policy marks DB duplicates for update`() {
        val json = """
            {
              "questions": [
                {
                  "questionId": "existing-question-id",
                  "questionText": "Yangilangan matn",
                  "options": ["A1", "B1", "C1", "D1"],
                  "correctAnswer": "A",
                  "solution": "Yangi yechim"
                }
              ]
            }
        """.trimIndent()

        val report = AdminQuestionImportService.validateJson(
            jsonString = json,
            targetModule = "MATHEMATICS",
            targetDifficulty = "MEDIUM",
            existingDbIds = setOf("existing-question-id"),
            existingDbHashes = emptySet(),
            conflictPolicy = ImportConflictPolicy.UPDATE_EXISTING
        )

        assertEquals(1, report.totalCount)
        assertEquals(0, report.validCount) // 0 new inserts
        assertEquals(1, report.entitiesToUpdate.size) // 1 update
        assertEquals(1, report.duplicateCount)
        assertEquals("existing-question-id", report.entitiesToUpdate.first().customQuestionId)
        assertEquals("Yangilangan matn", report.entitiesToUpdate.first().questionText)
    }

    @Test
    fun `validateJson with ABORT_ON_DUPLICATE generates fatal error when duplicate found`() {
        val json = """
            {
              "questions": [
                {
                  "questionId": "duplicate-id",
                  "questionText": "Savol matni",
                  "options": ["A", "B", "C", "D"],
                  "correctAnswer": "A",
                  "solution": "Yechim"
                }
              ]
            }
        """.trimIndent()

        val report = AdminQuestionImportService.validateJson(
            jsonString = json,
            targetModule = "MATHEMATICS",
            targetDifficulty = "MEDIUM",
            existingDbIds = setOf("duplicate-id"),
            existingDbHashes = emptySet(),
            conflictPolicy = ImportConflictPolicy.ABORT_ON_DUPLICATE
        )

        assertTrue(report.errors.any { it.field == "conflict_abort" })
        assertEquals(0, report.validCount)
    }
}
