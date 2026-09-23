package com.example.data.generator

import com.example.data.model.QuestionEntity

data class ValidationResult(
    val isValid: Boolean,
    val issues: List<String>
)

object QuestionValidationPipeline {

    private val VALID_MODULES = setOf("MATHEMATICS", "GEOMETRY")
    private val VALID_DIFFICULTIES = setOf("EASY", "MEDIUM", "HARD", "VERY_HARD")
    private val VALID_ANSWERS = setOf("A", "B", "C", "D")
    private val VALID_BOOK_SOURCES = setOf(
        "Puza Geometriya 1",
        "Puza Geometriya 2",
        "Puza Geometriya 1 + Puza Geometriya 2",
        "Umumiy Matematika"
    )

    fun validate(question: QuestionEntity, existingQuestions: Set<String> = emptySet()): ValidationResult {
        val issues = mutableListOf<String>()

        // 1. Module and Difficulty Check
        if (question.module !in VALID_MODULES) {
            issues.add("Noto'g'ri modul: ${question.module}")
        }
        if (question.difficulty !in VALID_DIFFICULTIES) {
            issues.add("Noto'g'ri qiyinlik darajasi: ${question.difficulty}")
        }

        // 2. Question Text Check
        if (question.questionText.isBlank() || question.questionText.trim().length < 5) {
            issues.add("Savol matni juda qisqa yoki bo'sh")
        }

        // 3. Option Consistency Check (all 4 options must be non-empty and unique)
        val options = listOf(
            question.optionA.trim(),
            question.optionB.trim(),
            question.optionC.trim(),
            question.optionD.trim()
        )
        if (options.any { it.isBlank() }) {
            issues.add("Barcha A, B, C, D variantlar to'ldirilgan bo'lishi shart")
        }
        if (options.distinct().size != 4) {
            issues.add("Variantlar takrorlanmasligi kerak (A, B, C, D unikal bo'lishi shart)")
        }

        // 4. Correct Answer Check (Section 5: Faqat bitta to'g'ri javob mavjudmi)
        if (question.correctAnswer !in VALID_ANSWERS) {
            issues.add("To'g'ri javob A, B, C yoki D bo'lishi kerak")
        }

        // 5. Explanation and Solution Steps Check (Section 5: Yechimdagi barcha bosqichlar to'g'rimi)
        if (question.explanation.isBlank() && question.solutionSteps.isBlank()) {
            issues.add("Savol tushuntirishi (explanation) yoki yechim bosqichlari bo'lishi shart")
        }

        // 6. Geometry Specific Validation (Section 5: formula, diagram, bookSource)
        if (question.module == "GEOMETRY") {
            if (question.formulaUsed.isBlank() && question.explanation.isBlank()) {
                issues.add("Geometriya savolida formula yoki qoida ko'rsatilishi kerak")
            }
            if (question.bookSource.isNotBlank() && question.bookSource !in VALID_BOOK_SOURCES) {
                issues.add("Noma'lum kitob manbasi: ${question.bookSource}")
            }
        }

        // 7. Duplicate Detection
        val normalizedText = question.questionText.trim().lowercase()
        if (existingQuestions.contains(normalizedText)) {
            issues.add("Bir xil savol bazada mavjud (takroriy savol aniqlandi)")
        }

        // 8. Domain/Topic Validity
        if (question.topic.isBlank()) {
            issues.add("Mavzu (topic) ko'rsatilishi shart")
        }

        return ValidationResult(
            isValid = issues.isEmpty(),
            issues = issues
        )
    }
}
