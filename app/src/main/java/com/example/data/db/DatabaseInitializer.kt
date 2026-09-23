package com.example.data.db

import android.content.Context
import com.example.data.generator.MathQuestionBankGenerator
import com.example.data.model.TestTemplateEntity
import com.example.data.model.UserEntity
import com.example.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun initializeIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val userDao = db.userDao()
        val questionDao = db.questionDao()
        val templateDao = db.testTemplateDao()

        // 1. Seed Admin & Demo User if no admin exists
        val adminUser = userDao.getUserByUsername("sapayev1231")
        if (adminUser == null) {
            userDao.insertUser(
                UserEntity(
                    username = "sapayev1231",
                    email = "admin@milliy.uz",
                    firstName = "Admin",
                    lastName = "Sapayev",
                    passwordHash = PasswordHasher.hashPassword("sapayev3112"),
                    role = "ADMIN",
                    isBlocked = false
                )
            )
            // Demo student user
            val demoUser = userDao.getUserByUsername("talaba")
            if (demoUser == null) {
                userDao.insertUser(
                    UserEntity(
                        username = "talaba",
                        email = "talaba@milliy.uz",
                        firstName = "Azizbek",
                        lastName = "Karimov",
                        passwordHash = PasswordHasher.hashPassword("talaba123"),
                        role = "USER",
                        isBlocked = false
                    )
                )
            }
        }

        // 2. Seed 8000 Questions (2000 per difficulty level: Easy, Medium, Hard, Very Hard)
        val currentCount = questionDao.countApprovedQuestions().first()
        if (currentCount < 8000) {
            if (currentCount > 0) {
                questionDao.deleteAllQuestions()
            }
            val generated = MathQuestionBankGenerator.generateComprehensive8000Questions()
            // Insert in chunks of 250 for optimal database performance
            val chunkSize = 250
            for (i in generated.indices step chunkSize) {
                val chunk = generated.subList(i, minOf(i + chunkSize, generated.size))
                questionDao.insertQuestions(chunk)
            }
        }

        // 3. Seed 1000 Puza Geometriya Questions (Puza 1 and Puza 2)
        val currentGeometryCount = questionDao.countQuestionsByModule("GEOMETRY").first()
        if (currentGeometryCount < 1000) {
            val geometryQuestions = com.example.data.generator.PuzaGeometryQuestionBankGenerator.generateComprehensive1000GeometryQuestions()
            val chunkSize = 250
            for (i in geometryQuestions.indices step chunkSize) {
                val chunk = geometryQuestions.subList(i, minOf(i + chunkSize, geometryQuestions.size))
                questionDao.insertQuestions(chunk)
            }
        }

        // 4. Seed Test Templates if empty
        val existingTemplates = templateDao.getAllTemplates().first()
        if (existingTemplates.isEmpty()) {
            templateDao.insertTemplate(
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
            templateDao.insertTemplate(
                TestTemplateEntity(
                    title = "Algebra va Tenglamalar — 20 Talik Mashq",
                    module = "MATHEMATICS",
                    topic = "Tenglamalar",
                    questionCount = 20,
                    durationMinutes = 60,
                    difficulty = "ALL",
                    isNationalCertMode = false
                )
            )
            templateDao.insertTemplate(
                TestTemplateEntity(
                    title = "Qiyin va Yuqori Darajali Savollar — 25 Talik",
                    module = "MATHEMATICS",
                    topic = "Barcha mavzular",
                    questionCount = 25,
                    durationMinutes = 90,
                    difficulty = "HARD",
                    isNationalCertMode = false
                )
            )
            templateDao.insertTemplate(
                TestTemplateEntity(
                    title = "Tezkor Sinov — 15 Talik Vaqtli Test",
                    module = "MATHEMATICS",
                    topic = "Barcha mavzular",
                    questionCount = 15,
                    durationMinutes = 30,
                    difficulty = "MEDIUM",
                    isNationalCertMode = false
                )
            )
        }
    }
}
