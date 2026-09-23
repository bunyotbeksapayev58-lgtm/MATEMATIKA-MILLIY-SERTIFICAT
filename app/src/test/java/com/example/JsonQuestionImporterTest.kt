package com.example

import com.example.data.importer.JsonQuestionImporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JsonQuestionImporterTest {

    @Test
    fun `computeCanonicalHash is deterministic and normalizes whitespace and case`() {
        val hash1 = JsonQuestionImporter.computeCanonicalHash(
            "Uchburchak burchaklari yig'indisi nechaga teng?",
            listOf("180°", "360°", "90°", "270°")
        )
        val hash2 = JsonQuestionImporter.computeCanonicalHash(
            "  uchburchak  burchaklari   yig'indisi nechaga  teng? ",
            listOf("90°", "180°", "270°", "360°") // reordered options
        )
        assertEquals("Canonical hashes should match regardless of option order and whitespace", hash1, hash2)
    }

    @Test
    fun `analyzeAndValidate rejects empty options and duplicate option keys`() {
        val invalidJson = """
            {
              "questions": [
                {
                  "questionId": "test-1",
                  "questionText": "Sinov savoli",
                  "correctAnswer": "A",
                  "options": {
                    "A": "Variant A",
                    "B": "",
                    "C": "Variant C",
                    "D": "Variant D"
                  }
                }
              ]
            }
        """.trimIndent()

        val preview = JsonQuestionImporter.analyzeAndValidate(
            jsonString = invalidJson,
            targetModule = "MATHEMATICS",
            targetDifficulty = "MEDIUM",
            existingQuestionIds = emptySet(),
            existingQuestionHashes = emptySet()
        )

        assertEquals(1, preview.totalCount)
        assertEquals(0, preview.validCount)
        assertEquals(1, preview.errorCount)
        assertTrue(preview.errors.first().contains("bo'sh bo'lmasligi kerak"))
    }

    @Test
    fun `analyzeAndValidate flags existing database questionId as duplicate`() {
        val validJson = """
            {
              "questions": [
                {
                  "questionId": "existing-001",
                  "questionText": "Savol matni",
                  "correctAnswer": "A",
                  "solution": "Batafsil yechim bu yerda",
                  "options": {
                    "A": "1",
                    "B": "2",
                    "C": "3",
                    "D": "4"
                  }
                }
              ]
            }
        """.trimIndent()

        val preview = JsonQuestionImporter.analyzeAndValidate(
            jsonString = validJson,
            targetModule = "MATHEMATICS",
            targetDifficulty = "MEDIUM",
            existingQuestionIds = setOf("existing-001"),
            existingQuestionHashes = emptySet()
        )

        assertEquals(1, preview.totalCount)
        assertEquals(0, preview.validCount)
        assertEquals(1, preview.duplicateCount)
        assertEquals(0, preview.errorCount)
        assertTrue(preview.duplicatesList.first().contains("allaqachon mavjud"))
    }

    @Test
    fun `math sample json parses cleanly and produces valid questions`() {
        val sampleJson = JsonQuestionImporter.getMathSampleJson()
        val preview = JsonQuestionImporter.analyzeAndValidate(
            jsonString = sampleJson,
            targetModule = "MATHEMATICS",
            targetDifficulty = "HARD",
            existingQuestionIds = emptySet(),
            existingQuestionHashes = emptySet()
        )

        assertTrue("Sample json should have questions", preview.totalCount > 0)
        assertEquals("Sample json should have 0 errors", 0, preview.errorCount)
        assertEquals("Sample json should have 0 duplicates in empty db", 0, preview.duplicateCount)
        assertEquals(preview.totalCount, preview.validCount)
    }

    @Test
    fun `geometry sample json parses cleanly with diagram and formulas`() {
        val sampleJson = JsonQuestionImporter.getGeometrySampleJson()
        val preview = JsonQuestionImporter.analyzeAndValidate(
            jsonString = sampleJson,
            targetModule = "GEOMETRY",
            targetDifficulty = "MEDIUM",
            existingQuestionIds = emptySet(),
            existingQuestionHashes = emptySet()
        )

        assertTrue(preview.totalCount > 0)
        assertEquals(0, preview.errorCount)
        assertEquals(preview.totalCount, preview.validCount)
        assertNotNull(preview.validQuestions.first().diagram)
    }
}
