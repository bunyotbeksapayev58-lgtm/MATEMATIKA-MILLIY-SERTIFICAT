package com.example.data.generator

import com.example.data.model.QuestionEntity

/**
 * High-precision generator for 1000+ authentic Geometry questions based on
 * "Puza Geometriya 1" and "Puza Geometriya 2" translated textbooks.
 *
 * Strict distribution:
 * - 250 Easy
 * - 250 Medium
 * - 250 Hard
 * - 250 Very Hard
 * Module: GEOMETRY
 * Sources: "Puza Geometriya 1", "Puza Geometriya 2", "Puza Geometriya 1 + Puza Geometriya 2"
 */
object PuzaGeometryQuestionBankGenerator {

    fun generateComprehensive1000GeometryQuestions(): List<QuestionEntity> {
        val questions = ArrayList<QuestionEntity>(1000)

        questions.addAll(generateEasyGeometryQuestions(250))
        questions.addAll(generateMediumGeometryQuestions(250))
        questions.addAll(generateHardGeometryQuestions(250))
        questions.addAll(generateVeryHardGeometryQuestions(250))

        return questions
    }

    private fun createQuestion(
        bookSource: String,
        topic: String,
        subtopic: String,
        difficulty: String,
        questionText: String,
        diagram: String,
        formulaUsed: String,
        correctValue: String,
        distractors: List<String>,
        explanation: String,
        solutionSteps: String,
        estimatedTime: Int,
        tags: String,
        idIndex: Int
    ): QuestionEntity {
        val distinctDistractors = distractors.filter { it.trim() != correctValue.trim() }.distinct().take(3)
        val finalDistractors = if (distinctDistractors.size < 3) {
            val padded = distinctDistractors.toMutableList()
            var offset = 1
            while (padded.size < 3) {
                val num = correctValue.toIntOrNull()
                val fallback = if (num != null) (num + offset).toString() else "${correctValue}_$offset"
                if (fallback != correctValue && !padded.contains(fallback)) {
                    padded.add(fallback)
                }
                offset++
            }
            padded
        } else {
            distinctDistractors
        }

        val correctLetter = when (idIndex % 4) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            else -> "D"
        }

        val optA: String
        val optB: String
        val optC: String
        val optD: String

        when (correctLetter) {
            "A" -> {
                optA = correctValue
                optB = finalDistractors.getOrElse(0) { "0" }
                optC = finalDistractors.getOrElse(1) { "1" }
                optD = finalDistractors.getOrElse(2) { "2" }
            }
            "B" -> {
                optA = finalDistractors.getOrElse(0) { "0" }
                optB = correctValue
                optC = finalDistractors.getOrElse(1) { "1" }
                optD = finalDistractors.getOrElse(2) { "2" }
            }
            "C" -> {
                optA = finalDistractors.getOrElse(0) { "0" }
                optB = finalDistractors.getOrElse(1) { "1" }
                optC = correctValue
                optD = finalDistractors.getOrElse(2) { "2" }
            }
            else -> {
                optA = finalDistractors.getOrElse(0) { "0" }
                optB = finalDistractors.getOrElse(1) { "1" }
                optC = finalDistractors.getOrElse(2) { "2" }
                optD = correctValue
            }
        }

        return QuestionEntity(
            subject = "Geometriya",
            module = "GEOMETRY",
            bookSource = bookSource,
            topic = topic,
            subtopic = subtopic,
            difficulty = difficulty,
            questionText = questionText,
            diagram = diagram,
            formulaUsed = formulaUsed,
            optionA = optA,
            optionB = optB,
            optionC = optC,
            optionD = optD,
            correctAnswer = correctLetter,
            explanation = explanation,
            solutionSteps = solutionSteps,
            estimatedTimeSeconds = estimatedTime,
            tags = tags,
            sourceType = "PRACTICE",
            status = "APPROVED"
        )
    }

    // =========================================================================
    // 1. EASY GEOMETRY QUESTIONS (250 items)
    // =========================================================================
    private fun generateEasyGeometryQuestions(count: Int): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)
        var idx = 0

        // Subtopic 1.1: Burchaklar (Angles) - Qo'shni, to'ldiruvchi, vertikal, parallel chiziqlar (60 questions)
        for (i in 1..60) {
            val angleA = 20 + (i * 2) % 65
            when (i % 4) {
                0 -> {
                    // Qo'shni burchaklar
                    val angleB = 180 - angleA
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 1",
                            topic = "Burchaklar",
                            subtopic = "Qo'shni burchaklar",
                            difficulty = "EASY",
                            questionText = "Chizmada ikkita qo'shni burchakdan biri $angleA° ga teng bo'lsa, ikkinchi burchak necha gradusga teng?",
                            diagram = "TYPE:GENERAL",
                            formulaUsed = "Qo'shni burchaklar yig'indisi: α + β = 180°",
                            correctValue = "$angleB°",
                            distractors = listOf("${angleB - 10}°", "${angleB + 10}°", "${90 - angleA}°"),
                            explanation = "Qo'shni burchaklarning yig'indisi doimo 180° ga teng bo'ladi.",
                            solutionSteps = "1. α + β = 180°\n2. β = 180° - $angleA° = $angleB°.",
                            estimatedTime = 40,
                            tags = "puza1,burchaklar,qoshni",
                            idIndex = idx++
                        )
                    )
                }
                1 -> {
                    // To'ldiruvchi burchaklar
                    val compA = 15 + (i * 3) % 60
                    val compB = 90 - compA
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 1",
                            topic = "Burchaklar",
                            subtopic = "To'ldiruvchi burchaklar",
                            difficulty = "EASY",
                            questionText = "O'zaro to'ldiruvchi burchaklardan biri $compA° ga teng bo'lsa, ikkinchi burchakni toping.",
                            diagram = "TYPE:GENERAL",
                            formulaUsed = "To'ldiruvchi burchaklar: α + β = 90°",
                            correctValue = "$compB°",
                            distractors = listOf("${compB + 10}°", "${compB - 5}°", "${180 - compA}°"),
                            explanation = "O'zaro to'ldiruvchi burchaklar yig'indisi 90° ni tashkil qiladi.",
                            solutionSteps = "1. α + β = 90°\n2. β = 90° - $compA° = $compB°.",
                            estimatedTime = 45,
                            tags = "puza1,burchaklar,toldiruvchi",
                            idIndex = idx++
                        )
                    )
                }
                2 -> {
                    // Parallel lines Z-rule
                    val zAngle = 35 + (i * 2) % 45
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 1",
                            topic = "Burchaklar",
                            subtopic = "Parallel to'g'ri chiziqlar",
                            difficulty = "EASY",
                            questionText = "Chizmada d₁ ∥ d₂ parallel to'g'ri chiziqlar berilgan. Ichki almashinuvchi burchaklardan biri $zAngle° bo'lsa, x burchakni toping.",
                            diagram = "TYPE:PARALLEL_Z;angle1=$zAngle°;angle2=x",
                            formulaUsed = "Z-qoidasi (ichki almashinuvchi): α = β",
                            correctValue = "$zAngle°",
                            distractors = listOf("${180 - zAngle}°", "${90 - zAngle}°", "${zAngle + 20}°"),
                            explanation = "Parallel to'g'ri chiziqlarni uchinchi to'g'ri chiziq kesib o'tganda hosil bo'lgan ichki almashinuvchi burchaklar tengdir.",
                            solutionSteps = "1. d₁ ∥ d₂ bo'lgani uchun Z-qoidasiga binoan x = $zAngle°.",
                            estimatedTime = 40,
                            tags = "puza1,parallel,z_qoida",
                            idIndex = idx++
                        )
                    )
                }
                else -> {
                    // Parallel lines M-rule
                    val aTop = 25 + (i * 2) % 30
                    val aBot = 30 + (i * 3) % 35
                    val aMid = aTop + aBot
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 1",
                            topic = "Burchaklar",
                            subtopic = "M-qoidasi",
                            difficulty = "EASY",
                            questionText = "Chizmada d₁ ∥ d₂ parallel chiziqlar orasida M-shaklidagi siniq chiziq berilgan. Burchaklar $aTop° va $aBot° bo'lsa, o'rtadagi x burchakni toping.",
                            diagram = "TYPE:PARALLEL_M;angleTop=$aTop°;angleBottom=$aBot°;angleMid=x",
                            formulaUsed = "M-qoidasi: x = α + β",
                            correctValue = "$aMid°",
                            distractors = listOf("${aMid - 10}°", "${180 - aMid}°", "${aMid + 15}°"),
                            explanation = "Puza Geometriya kitobidagi M-qoidasiga ko'ra chapga qaragan burchaklar yig'indisi o'ngga qaragan burchakka teng: x = α + β.",
                            solutionSteps = "1. M-qoidasiga ko'ra: x = $aTop° + $aBot° = $aMid°.",
                            estimatedTime = 50,
                            tags = "puza1,m_qoidasi,burchaklar",
                            idIndex = idx++
                        )
                    )
                }
            }
        }

        // Subtopic 1.2: Uchburchaklar (Triangles) - Ichki burchaklar, teng yonli, Pifagor (70 questions)
        for (i in 1..70) {
            when (i % 3) {
                0 -> {
                    // Uchburchak ichki burchaklari yig'indisi
                    val a1 = 40 + (i * 2) % 45
                    val a2 = 35 + (i * 3) % 45
                    val a3 = 180 - (a1 + a2)
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 1",
                            topic = "Uchburchaklar",
                            subtopic = "Burchaklar yig'indisi",
                            difficulty = "EASY",
                            questionText = "ABC uchburchakda ∠A = $a1°, ∠B = $a2° bo'lsa, uchinchi ∠C burchakni toping.",
                            diagram = "TYPE:TRIANGLE_GENERAL;a=b;c=x",
                            formulaUsed = "Uchburchak ichki burchaklari yig'indisi: ∠A + ∠B + ∠C = 180°",
                            correctValue = "$a3°",
                            distractors = listOf("${a3 + 10}°", "${a3 - 10}°", "${180 - a1}°"),
                            explanation = "Ixtiyoriy uchburchakning ichki burchaklari yig'indisi 180° ga teng.",
                            solutionSteps = "1. ∠C = 180° - ($a1° + $a2°) = 180° - ${a1 + a2}° = $a3°.",
                            estimatedTime = 45,
                            tags = "puza1,uchburchak,ichki_burchak",
                            idIndex = idx++
                        )
                    )
                }
                1 -> {
                    // To'g'ri burchakli uchburchak - Pifagor (3-4-5, 6-8-10, 5-12-13, 8-15-17)
                    val triples = listOf(
                        Triple(3, 4, 5),
                        Triple(6, 8, 10),
                        Triple(5, 12, 13),
                        Triple(9, 12, 15),
                        Triple(8, 15, 17),
                        Triple(12, 16, 20),
                        Triple(7, 24, 25)
                    )
                    val (a, b, c) = triples[i % triples.size]
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 1",
                            topic = "Uchburchaklar",
                            subtopic = "To'g'ri burchakli uchburchak va Pifagor",
                            difficulty = "EASY",
                            questionText = "To'g'ri burchakli uchburchakning katetlari a = $a va b = $b ga teng. Gipotenuzani (c) toping.",
                            diagram = "TYPE:TRIANGLE_RIGHT;a=$a;b=$b;c=x",
                            formulaUsed = "Pifagor teoremasi: c² = a² + b²",
                            correctValue = "$c",
                            distractors = listOf("${c + 1}", "${c - 2}", "${a + b}"),
                            explanation = "To'g'ri burchakli uchburchakda gipotenuza kvadrati katetlar kvadratlari yig'indisiga teng.",
                            solutionSteps = "1. c² = $a² + $b² = ${a * a} + ${b * b} = ${c * c}\n2. c = √${c * c} = $c.",
                            estimatedTime = 50,
                            tags = "puza1,pifagor,gipotenuza",
                            idIndex = idx++
                        )
                    )
                }
                else -> {
                    // Teng yonli uchburchak perimetri
                    val leg = 8 + (i % 12)
                    val base = 6 + (i % 8)
                    val p = 2 * leg + base
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 1",
                            topic = "Uchburchaklar",
                            subtopic = "Teng yonli uchburchak",
                            difficulty = "EASY",
                            questionText = "Teng yonli uchburchakning yon tomoni $leg ga, asosi esa $base ga teng. Uchburchakning perimetrini toping.",
                            diagram = "TYPE:TRIANGLE_ISOSCELES;base=$base;leg=$leg",
                            formulaUsed = "Perimetr: P = 2a + b",
                            correctValue = "$p",
                            distractors = listOf("${p + 2}", "${p - 4}", "${leg + base}"),
                            explanation = "Teng yonli uchburchakning ikkita yon tomoni o'zaro teng.",
                            solutionSteps = "1. P = 2 · $leg + $base = ${2 * leg} + $base = $p.",
                            estimatedTime = 40,
                            tags = "puza1,teng_yonli,perimetr",
                            idIndex = idx++
                        )
                    )
                }
            }
        }

        // Subtopic 1.3: Kvadrat va To'g'ri to'rtburchak (Puza 1) (40 questions)
        for (i in 1..40) {
            val a = 4 + (i % 10)
            val b = 6 + (i % 8)
            val s = a * b
            val p = 2 * (a + b)
            if (i % 2 == 0) {
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 1",
                        topic = "To‘rtburchaklar",
                        subtopic = "To'g'ri to'rtburchak yuzi",
                        difficulty = "EASY",
                        questionText = "To'g'ri to'rtburchakning tomonlari $a va $b ga teng. Uning yuzini toping.",
                        diagram = "TYPE:PARALLELOGRAM;a=$a;b=$b",
                        formulaUsed = "Yuz: S = a · b",
                        correctValue = "$s",
                        distractors = listOf("${s + 4}", "${s - 6}", "$p"),
                        explanation = "To'g'ri to'rtburchak yuzi uning eni va bo'yining ko'paytmasiga teng.",
                        solutionSteps = "1. S = a · b = $a · $b = $s.",
                        estimatedTime = 35,
                        tags = "puza1,tortburchak,yuza",
                        idIndex = idx++
                    )
                )
            } else {
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 1",
                        topic = "To‘rtburchaklar",
                        subtopic = "To'g'ri to'rtburchak perimetri",
                        difficulty = "EASY",
                        questionText = "Tomonlari $a va $b bo'lgan to'g'ri to'rtburchakning perimetrini hisoblang.",
                        diagram = "TYPE:PARALLELOGRAM;a=$a;b=$b",
                        formulaUsed = "Perimetr: P = 2(a + b)",
                        correctValue = "$p",
                        distractors = listOf("${p + 2}", "${p - 2}", "$s"),
                        explanation = "To'g'ri to'rtburchak perimetri: P = 2(a + b).",
                        solutionSteps = "1. P = 2 · ($a + $b) = 2 · ${a + b} = $p.",
                        estimatedTime = 35,
                        tags = "puza1,tortburchak,perimetr",
                        idIndex = idx++
                    )
                )
            }
        }

        // Subtopic 1.4: Puza Geometriya 2 - Parallelogram, Romb, Trapetsiya (50 questions)
        for (i in 1..50) {
            when (i % 3) {
                0 -> {
                    // Parallelogram burchaklari
                    val ang = 50 + (i * 2) % 55
                    val oppAng = ang
                    val suppAng = 180 - ang
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 2",
                            topic = "Parallelogram",
                            subtopic = "Burchaklari",
                            difficulty = "EASY",
                            questionText = "ABCD parallelogrammda burchak ∠A = $ang° bo'lsa, unga qo'shni ∠B burchakni toping.",
                            diagram = "TYPE:PARALLELOGRAM;a=8;b=5",
                            formulaUsed = "Parallelogram ketma-ket burchaklari yig'indisi: ∠A + ∠B = 180°",
                            correctValue = "$suppAng°",
                            distractors = listOf("$oppAng°", "${suppAng - 15}°", "${suppAng + 10}°"),
                            explanation = "Parallelogrammning bir tomoniga yopishgan burchaklarining yig'indisi 180° ga teng.",
                            solutionSteps = "1. ∠B = 180° - ∠A = 180° - $ang° = $suppAng°.",
                            estimatedTime = 40,
                            tags = "puza2,parallelogram,burchak",
                            idIndex = idx++
                        )
                    )
                }
                1 -> {
                    // Trapetsiya o'rta chizig'i
                    val base1 = 6 + (i * 2) % 14
                    val base2 = 14 + (i * 2) % 18
                    val midLine = (base1 + base2) / 2
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 2",
                            topic = "Trapetsiya",
                            subtopic = "O'rta chiziq",
                            difficulty = "EASY",
                            questionText = "Trapetsiyaning asoslari a = $base1 va b = $base2 ga teng. Uning o'rta chizig'i uzunligini toping.",
                            diagram = "TYPE:TRAPEZOID;a=$base2;b=$base1",
                            formulaUsed = "Trapetsiya o'rta chizig'i: m = (a + b) / 2",
                            correctValue = "$midLine",
                            distractors = listOf("${midLine + 1}", "${midLine - 2}", "${base2 - base1}"),
                            explanation = "Trapetsiyaning o'rta chizig'i asoslari yig'indisining yarmiga teng bo'ladi.",
                            solutionSteps = "1. m = ($base1 + $base2) / 2 = ${base1 + base2} / 2 = $midLine.",
                            estimatedTime = 40,
                            tags = "puza2,trapetsiya,orta_chiziq",
                            idIndex = idx++
                        )
                    )
                }
                else -> {
                    // Romb perimetri
                    val side = 5 + (i % 12)
                    val p = 4 * side
                    list.add(
                        createQuestion(
                            bookSource = "Puza Geometriya 2",
                            topic = "Romb",
                            subtopic = "Romb perimetri",
                            difficulty = "EASY",
                            questionText = "Tomoni $side ga teng bo'lgan rombning perimetrini toping.",
                            diagram = "TYPE:RHOMBUS;a=$side",
                            formulaUsed = "Romb perimetri: P = 4a",
                            correctValue = "$p",
                            distractors = listOf("${p - 4}", "${p + 4}", "${side * side}"),
                            explanation = "Rombning barcha 4 ta tomoni o'zaro teng.",
                            solutionSteps = "1. P = 4 · $side = $p.",
                            estimatedTime = 30,
                            tags = "puza2,romb,perimetr",
                            idIndex = idx++
                        )
                    )
                }
            }
        }

        // Subtopic 1.5: Aylana va Doira (Puza 2) (30 questions)
        for (i in 1..30) {
            val r = 2 + (i % 8)
            val len = 2 * r
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 2",
                    topic = "Aylana va doira",
                    subtopic = "Aylana uzunligi",
                    difficulty = "EASY",
                    questionText = "Radiusi R = $r bo'lgan aylananing uzunligini π orqali ifodalang.",
                    diagram = "TYPE:CIRCLE_INSCRIBED;r=$r",
                    formulaUsed = "Aylana uzunligi: C = 2πR",
                    correctValue = "${len}π",
                    distractors = listOf("${len + 2}π", "${r * r}π", "${r}π"),
                    explanation = "Aylana uzunligi C = 2πR formulasi bo'yicha hisoblanadi.",
                    solutionSteps = "1. C = 2 · π · $r = ${len}π.",
                    estimatedTime = 35,
                    tags = "puza2,aylana,uzunlik",
                    idIndex = idx++
                )
            )
        }

        return list.take(count)
    }

    // =========================================================================
    // 2. MEDIUM GEOMETRY QUESTIONS (250 items)
    // =========================================================================
    private fun generateMediumGeometryQuestions(count: Int): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)
        var idx = 250

        // Subtopic 2.1: 30-60-90 va 45-45-90 xossalari (50 questions)
        for (i in 1..50) {
            val a = 4 + (i % 10)
            val c = 2 * a
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 1",
                    topic = "Uchburchaklar",
                    subtopic = "30°-60°-90° xossasi",
                    difficulty = "MEDIUM",
                    questionText = "To'g'ri burchakli uchburchakning o'tkir burchaklaridan biri 30° ga teng. Agar 30° li burchak qarshisidagi katet $a ga teng bo'lsa, gipotenuzani toping.",
                    diagram = "TYPE:TRIANGLE_RIGHT;a=$a;c=x",
                    formulaUsed = "30° burchak qarshisidagi katet: a = c / 2 ⇒ c = 2a",
                    correctValue = "$c",
                    distractors = listOf("${c - 2}", "${a + 4}", "${c + 4}"),
                    explanation = "To'g'ri burchakli uchburchakda 30° li burchak qarshisidagi katet gipotenuzaning yarmiga teng.",
                    solutionSteps = "1. c = 2 · a = 2 · $a = $c.",
                    estimatedTime = 60,
                    tags = "puza1,uchburchak,30_daraja",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 2.2: Evklid teoremalari (Balandlik va katet proyeksiyalari) (50 questions)
        val euclidCases = listOf(
            Triple(4, 9, 6), // h^2 = 4*9 = 36 => h=6
            Triple(2, 8, 4), // h^2 = 16 => h=4
            Triple(3, 12, 6), // h^2 = 36 => h=6
            Triple(4, 16, 8), // h^2 = 64 => h=8
            Triple(9, 16, 12), // h^2 = 144 => h=12
            Triple(5, 20, 10), // h^2 = 100 => h=10
            Triple(1, 9, 3)
        )
        for (i in 1..50) {
            val (ac, bc, h) = euclidCases[i % euclidCases.size]
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 1",
                    topic = "Uchburchaklar",
                    subtopic = "Evklid teoremalari",
                    difficulty = "MEDIUM",
                    questionText = "To'g'ri burchakli uchburchakda to'g'ri burchak uchidan gipotenuzaga tushirilgan balandlik gipotenuzani $ac va $bc ga teng kesmalarga ajratadi. Ushbu balandlikni (h) toping.",
                    diagram = "TYPE:TRIANGLE_RIGHT;a=ac;b=bc;h=x",
                    formulaUsed = "Evklid balandlik teoremasi: h² = a_c · b_c",
                    correctValue = "$h",
                    distractors = listOf("${h + 1}", "${h - 2}", "${(ac + bc) / 2}"),
                    explanation = "To'g'ri burchakdan tushirilgan balandlik gipotenuza bo'laklarining o'rta geometrigiga teng: h = √(a_c · b_c).",
                    solutionSteps = "1. h² = a_c · b_c = $ac · $bc = ${ac * bc}\n2. h = √${ac * bc} = $h.",
                    estimatedTime = 70,
                    tags = "puza1,evklid,balandlik",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 2.3: Bissektrisa xossasi (40 questions)
        for (i in 1..40) {
            val k = 2 + (i % 3)
            val m = 3 + (i % 5)
            val n = 4 + (i % 4)
            val a = m * k
            val b = n * k
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 1",
                    topic = "Uchburchaklar",
                    subtopic = "Ichki bissektrisa teoremasi",
                    difficulty = "MEDIUM",
                    questionText = "ABC uchburchakda BD bissektrisa AC tomonni AD = $m va DC = $n kesmalarga bo'ladi. Agar AB = $a bo'lsa, BC tomonni toping.",
                    diagram = "TYPE:TRIANGLE_GENERAL;labels=A,D,C,B",
                    formulaUsed = "Ichki bissektrisa xossasi: AB / BC = AD / DC",
                    correctValue = "$b",
                    distractors = listOf("${b + 2}", "${b - 2}", "${a + n}"),
                    explanation = "Uchburchakning ichki bissektrisasi qarama-qarshi tomonni qolgan ikki tomonga mutanosib bo'laklarga ajratadi.",
                    solutionSteps = "1. $a / BC = $m / $n\n2. BC = ($a · $n) / $m = ${a * n} / $m = $b.",
                    estimatedTime = 75,
                    tags = "puza1,bissektrisa,nisbat",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 2.4: Puza 2 - Parallelogram va Romb yuzi (50 questions)
        for (i in 1..50) {
            if (i % 2 == 0) {
                // Parallelogram: S = a * ha
                val a = 6 + (i % 8)
                val ha = 4 + (i % 6)
                val s = a * ha
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 2",
                        topic = "Parallelogram",
                        subtopic = "Parallelogram yuzi",
                        difficulty = "MEDIUM",
                        questionText = "Parallelogrammning bir tomoni a = $a ga va unga tushirilgan balandlik h = $ha ga teng. Uning yuzini hisoblang.",
                        diagram = "TYPE:PARALLELOGRAM;a=$a;h=$ha",
                        formulaUsed = "Yuz formulasi: S = a · h_a",
                        correctValue = "$s",
                        distractors = listOf("${s + 6}", "${s - 4}", "${2 * (a + ha)}"),
                        explanation = "Parallelogramm yuzi asos va unga tushirilgan balandlik ko'paytmasiga teng.",
                        solutionSteps = "1. S = a · h_a = $a · $ha = $s.",
                        estimatedTime = 55,
                        tags = "puza2,parallelogram,yuza",
                        idIndex = idx++
                    )
                )
            } else {
                // Romb: S = (d1 * d2) / 2
                val d1 = 6 + (i % 6) * 2
                val d2 = 8 + (i % 5) * 2
                val s = (d1 * d2) / 2
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 2",
                        topic = "Romb",
                        subtopic = "Romb yuzi (diagonallar orqali)",
                        difficulty = "MEDIUM",
                        questionText = "Rombning diagonallari d₁ = $d1 va d₂ = $d2 ga teng. Rombning yuzini toping.",
                        diagram = "TYPE:RHOMBUS;d1=$d1;d2=$d2",
                        formulaUsed = "Romb yuzi: S = (d₁ · d₂) / 2",
                        correctValue = "$s",
                        distractors = listOf("${s * 2}", "${s + 5}", "${s - 6}"),
                        explanation = "Romb diagonallari o'zaro perpendikulyar bo'lgani uchun uning yuzi diagonallar ko'paytmasining yarmiga teng.",
                        solutionSteps = "1. S = ($d1 · $d2) / 2 = ${d1 * d2} / 2 = $s.",
                        estimatedTime = 60,
                        tags = "puza2,romb,diagonallar",
                        idIndex = idx++
                    )
                )
            }
        }

        // Subtopic 2.5: Doira sektori yuzi va Ichki chizilgan burchak (Puza 2) (60 questions)
        for (i in 1..60) {
            if (i % 2 == 0) {
                // Inscribed angle vs central angle
                val central = 40 + (i * 2) % 70
                val inscribed = central / 2
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 2",
                        topic = "Aylana va doira",
                        subtopic = "Ichki chizilgan burchak",
                        difficulty = "MEDIUM",
                        questionText = "Chizmada aylananing markaziy burchagi ∠AOB = ${inscribed * 2}° ga teng bo'lsa, xuddi shu yoyga tiralgan ichki chizilgan ∠ACB burchakni toping.",
                        diagram = "TYPE:CIRCLE_INSCRIBED;central=${inscribed * 2}°;inscribed=x",
                        formulaUsed = "Ichki chizilgan burchak: ∠ACB = ∠AOB / 2",
                        correctValue = "$inscribed°",
                        distractors = listOf("${inscribed * 2}°", "${inscribed + 10}°", "${90 - inscribed}°"),
                        explanation = "Bir xil yoyga tiralgan ichki chizilgan burchak markaziy burchakning yarmiga teng bo'ladi.",
                        solutionSteps = "1. ∠ACB = ${inscribed * 2}° / 2 = $inscribed°.",
                        estimatedTime = 50,
                        tags = "puza2,aylana,ichki_burchak",
                        idIndex = idx++
                    )
                )
            } else {
                // Sector area: S = (pi * R^2 * alpha) / 360
                val r = 6
                val alpha = 60
                val sCoeff = (r * r * alpha) / 360 // (36 * 60) / 360 = 6
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 2",
                        topic = "Yuzalar va Bo'yalgan sohalar",
                        subtopic = "Doira sektori yuzi",
                        difficulty = "MEDIUM",
                        questionText = "Radiusi R = $r bo'lgan doirada markaziy burchagi α = $alpha° bo'lgan bo'yalgan sektorning yuzini π orqali ifodalang.",
                        diagram = "TYPE:CIRCLE_SECTOR;r=$r;alpha=$alpha°",
                        formulaUsed = "Sektor yuzi: S = (π · R² · α) / 360°",
                        correctValue = "${sCoeff}π",
                        distractors = listOf("${sCoeff * 2}π", "${sCoeff + 3}π", "${r}π"),
                        explanation = "Doira sektori yuzi butun doira yuzining markaziy burchakka to'g'ri keluvchi ulushiga teng.",
                        solutionSteps = "1. S = (π · $r² · $alpha°) / 360° = (π · 36 · $alpha) / 360 = ${sCoeff}π.",
                        estimatedTime = 65,
                        tags = "puza2,sektor,boyalgan_yuza",
                        idIndex = idx++
                    )
                )
            }
        }

        return list.take(count)
    }

    // =========================================================================
    // 3. HARD GEOMETRY QUESTIONS (250 items)
    // =========================================================================
    private fun generateHardGeometryQuestions(count: Int): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)
        var idx = 500

        // Subtopic 3.1: Mediana va Sentroid (Og'irlik markazi G) (50 questions)
        for (i in 1..50) {
            val gd = 3 + (i % 6)
            val ag = 2 * gd
            val ad = ag + gd
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 1",
                    topic = "Uchburchaklar",
                    subtopic = "Uchburchak og'irlik markazi (Sentroid)",
                    difficulty = "HARD",
                    questionText = "ABC uchburchakning medianalari G nuqtada kesishadi. Agar AD mediana bo'lib, GD = $gd bo'lsa, butun AD mediananing uzunligini toping.",
                    diagram = "TYPE:TRIANGLE_GENERAL;labels=A,G,D,B,C",
                    formulaUsed = "Sentroid xossasi: AG : GD = 2 : 1 ⇒ AD = 3 · GD",
                    correctValue = "$ad",
                    distractors = listOf("$ag", "${ad + 2}", "${ad - 2}"),
                    explanation = "Uchburchak medianalari kesishish nuqtasida uchidan boshlab hisoblaganda 2:1 nisbatda bo'linadi.",
                    solutionSteps = "1. AG = 2 · GD = 2 · $gd = $ag\n2. AD = AG + GD = $ag + $gd = $ad.",
                    estimatedTime = 90,
                    tags = "puza1,mediana,sentroid",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 3.2: Teng yonli trapetsiya xossalari va balandligi (50 questions)
        for (i in 1..50) {
            // Isosceles trapezoid with bases a, b and leg c
            // Projection x = (b - a) / 2. If x=3, h=4, c=5 => a=10, b=16
            val x = 3 + (i % 4)
            val h = 4 + (i % 3)
            val cSquared = x * x + h * h
            val a = 8 + (i % 6)
            val b = a + 2 * x
            val s = ((a + b) * h) / 2
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 2",
                    topic = "Trapetsiya",
                    subtopic = "Teng yonli trapetsiya yuzi",
                    difficulty = "HARD",
                    questionText = "Teng yonli trapetsiyaning asoslari $a va $b ga, balandligi esa $h ga teng. Ushbu trapetsiyaning yuzini hisoblang.",
                    diagram = "TYPE:TRAPEZOID_ISOSCELES;a=$b;b=$a;h=$h",
                    formulaUsed = "Trapetsiya yuzi: S = ((a + b) / 2) · h",
                    correctValue = "$s",
                    distractors = listOf("${s + 8}", "${s - 6}", "${(a + b) * h}"),
                    explanation = "Teng yonli trapetsiyaning yuzi uning o'rta chizig'i va balandligi ko'paytmasiga teng.",
                    solutionSteps = "1. O'rta chiziq m = ($a + $b) / 2 = ${a + b} / 2 = ${(a + b) / 2.0}\n2. S = m · h = ${(a + b) / 2.0} · $h = $s.",
                    estimatedTime = 100,
                    tags = "puza2,teng_yonli_trapetsiya,yuza",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 3.3: Kesishuvchi vatarlar va urinmalar teoremalari (50 questions)
        for (i in 1..50) {
            // Tangent-secant theorem: PT^2 = PA * PB
            val pa = 4
            val pb = 9 + (i % 8) * 4 // 9, 13, etc. => e.g. PA=4, PB=9 => PT^2 = 36 => PT=6
            val pt = 6
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 2",
                    topic = "Aylana va doira",
                    subtopic = "Urinma va kesuvchi teoremasi",
                    difficulty = "HARD",
                    questionText = "P nuqtadan aylanaga PT urinma va aylana bilan A va B nuqtalarda kesishuvchi kesuvchi o'tkazilgan. Agar PA = 4 va PB = 9 bo'lsa, urinma kesmasi PT ni toping.",
                    diagram = "TYPE:CIRCLE_INSCRIBED;labels=P,T,A,B",
                    formulaUsed = "Urinma va kesuvchi teoremasi: PT² = PA · PB",
                    correctValue = "6",
                    distractors = listOf("5", "7", "8"),
                    explanation = "Bir nuqtadan o'tkazilgan urinma kesmasining kvadrati tashqi nuqtadan kesuvchining nuqtalarigacha bo'lgan masofalar ko'paytmasiga teng.",
                    solutionSteps = "1. PT² = PA · PB = 4 · 9 = 36\n2. PT = √36 = 6.",
                    estimatedTime = 110,
                    tags = "puza2,urinma,kesuvchi",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 3.4: Bo'yalgan halqa va murakkab soha yuzasi (Puza 2) (50 questions)
        for (i in 1..50) {
            // Ring area: S = pi * (R^2 - r^2)
            val r = 3 + (i % 4)
            val bigR = r + 2
            val ringDiff = bigR * bigR - r * r
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 2",
                    topic = "Yuzalar va Bo'yalgan sohalar",
                    subtopic = "Konsentrik doiralar halqasi yuzi",
                    difficulty = "HARD",
                    questionText = "Markazlari umumiy bo'lgan ikkita konsentrik aylananing radiuslari r = $r va R = $bigR ga teng. Ushbu aylanalar orasida hosil bo'lgan bo'yalgan halqaning yuzini π orqali hisoblang.",
                    diagram = "TYPE:CIRCLE_RING;R=$bigR;r=$r",
                    formulaUsed = "Doiraviy halqa yuzi: S = π · (R² - r²)",
                    correctValue = "${ringDiff}π",
                    distractors = listOf("${ringDiff + 4}π", "${ringDiff - 4}π", "${(bigR - r) * (bigR - r)}π"),
                    explanation = "Halqa yuzi katta doira yuzidan kichik doira yuzining ayirmasiga teng: S = π(R² - r²).",
                    solutionSteps = "1. S = π · ($bigR² - $r²) = π · (${bigR * bigR} - ${r * r}) = ${ringDiff}π.",
                    estimatedTime = 95,
                    tags = "puza2,halqa,boyalgan_yuza",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 3.5: Fazoviy geometriya - Silindr va Konus hajmi (Puza 2) (50 questions)
        for (i in 1..50) {
            if (i % 2 == 0) {
                // Silindr hajmi: V = pi * R^2 * H
                val r = 3 + (i % 4)
                val h = 5 + (i % 5)
                val vCoeff = r * r * h
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 2",
                        topic = "Fazoviy geometriya (Stereometriya)",
                        subtopic = "Silindr hajmi",
                        difficulty = "HARD",
                        questionText = "Silindr asosining radiusi R = $r ga, balandligi esa H = $h ga teng. Silindrning hajmini π orqali ifodalang.",
                        diagram = "TYPE:SOLID_CYLINDER;r=$r;h=$h",
                        formulaUsed = "Silindr hajmi: V = π · R² · H",
                        correctValue = "${vCoeff}π",
                        distractors = listOf("${vCoeff + 10}π", "${vCoeff - 12}π", "${2 * r * h}π"),
                        explanation = "Silindrning hajmi asos yuzi va balandlikning ko'paytmasiga teng.",
                        solutionSteps = "1. V = π · R² · H = π · $r² · $h = π · ${r * r} · $h = ${vCoeff}π.",
                        estimatedTime = 90,
                        tags = "puza2,silindr,hajm",
                        idIndex = idx++
                    )
                )
            } else {
                // Konus hajmi: V = (1/3) * pi * R^2 * H
                val r = 3
                val h = 6 + (i % 4) * 3
                val vCoeff = (r * r * h) / 3
                list.add(
                    createQuestion(
                        bookSource = "Puza Geometriya 2",
                        topic = "Fazoviy geometriya (Stereometriya)",
                        subtopic = "Konus hajmi",
                        difficulty = "HARD",
                        questionText = "Konus asosining radiusi R = $r va balandligi H = $h ga teng. Uning hajmini π orqali ifodalang.",
                        diagram = "TYPE:SOLID_CONE;r=$r;h=$h",
                        formulaUsed = "Konus hajmi: V = (1/3) · π · R² · H",
                        correctValue = "${vCoeff}π",
                        distractors = listOf("${vCoeff * 3}π", "${vCoeff + 6}π", "${vCoeff - 6}π"),
                        explanation = "Konusning hajmi mos silindr hajmining uchdan biriga teng: V = (1/3)πR²H.",
                        solutionSteps = "1. V = (1/3) · π · $r² · $h = (1/3) · π · 9 · $h = ${vCoeff}π.",
                        estimatedTime = 95,
                        tags = "puza2,konus,hajm",
                        idIndex = idx++
                    )
                )
            }
        }

        return list.take(count)
    }

    // =========================================================================
    // 4. VERY HARD GEOMETRY QUESTIONS (250 items)
    // =========================================================================
    private fun generateVeryHardGeometryQuestions(count: Int): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)
        var idx = 750

        // Subtopic 4.1: Geron formulasi va Ichki/Tashqi chizilgan aylanalar (50 questions)
        for (i in 1..50) {
            // Clean integer area triangle: 13, 14, 15 => p=21 => S = sqrt(21 * 8 * 7 * 6) = 84
            // Inradius r = S / p = 84 / 21 = 4
            // Circumradius R = abc / (4S) = (13*14*15) / (4*84) = 2730 / 336 = 8.125
            val scale = 1 + (i % 2)
            val a = 13 * scale
            val b = 14 * scale
            val c = 15 * scale
            val s = 84 * scale * scale
            val p = 21 * scale
            val r = 4 * scale
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 1 + Puza Geometriya 2",
                    topic = "Uchburchaklar",
                    subtopic = "Ichki chizilgan aylana radiusi (Geron)",
                    difficulty = "VERY_HARD",
                    questionText = "Tomonlari $a, $b va $c bo'lgan uchburchakka ichki chizilgan aylananing radiusi r ni toping.",
                    diagram = "TYPE:TRIANGLE_GENERAL;a=$a;b=$b;c=$c",
                    formulaUsed = "Ichki radius: r = S / p; Geron: S = √(p(p-a)(p-b)(p-c))",
                    correctValue = "$r",
                    distractors = listOf("${r + 1}", "${r - 1}", "${r * 2}"),
                    explanation = "Geron formulasi yordamida yuzani topamiz, so'ngra r = S / p formuladan foydalanamiz.",
                    solutionSteps = "1. Yarim perimetr: p = ($a + $b + $c) / 2 = $p\n2. S = √($p · ${p-a} · ${p-b} · ${p-c}) = $s\n3. r = S / p = $s / $p = $r.",
                    estimatedTime = 140,
                    tags = "puza1,puza2,geron,ichki_aylana",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 4.2: Muntazam oltiburchak va diagonallari (Puza 2) (50 questions)
        for (i in 1..50) {
            val a = 4 + (i % 6)
            val bigDiag = 2 * a
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 2",
                    topic = "Ko'pburchaklar",
                    subtopic = "Muntazam oltiburchakning katta diagonali",
                    difficulty = "VERY_HARD",
                    questionText = "Tomoni a = $a ga teng bo'lgan muntazam oltiburchakning eng katta diagonalining uzunligini toping.",
                    diagram = "TYPE:POLYGON_HEXAGON;a=$a",
                    formulaUsed = "Muntazam oltiburchak katta diagonali: D = 2a",
                    correctValue = "$bigDiag",
                    distractors = listOf("${bigDiag - 2}", "${a * 3}", "$a√3"),
                    explanation = "Muntazam oltiburchak markazdan o'tuvchi 3 ta katta diagonal bilan 6 ta teng tomonli uchburchakka ajraladi. Shuning uchun D = 2a bo'ladi.",
                    solutionSteps = "1. Katta diagonal formulasi: D = 2 · a\n2. D = 2 · $a = $bigDiag.",
                    estimatedTime = 110,
                    tags = "puza2,oltiburchak,katta_diagonal",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 4.3: Shar va sfera sirt yuzi hamda hajmi (Puza 2) (50 questions)
        for (i in 1..50) {
            // Sphere surface: S = 4 * pi * R^2
            val r = 3 + (i % 4)
            val sCoeff = 4 * r * r
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 2",
                    topic = "Fazoviy geometriya (Stereometriya)",
                    subtopic = "Shar sirtining yuzi",
                    difficulty = "VERY_HARD",
                    questionText = "Radiusi R = $r bo'lgan sharning sirt yuzini π orqali hisoblang.",
                    diagram = "TYPE:SOLID_SPHERE;r=$r",
                    formulaUsed = "Sfera sirt yuzi: S = 4πR²",
                    correctValue = "${sCoeff}π",
                    distractors = listOf("${sCoeff / 2}π", "${sCoeff + 12}π", "${r * r}π"),
                    explanation = "Shar sferik sirtining to'la yuzi 4πR² ga teng.",
                    solutionSteps = "1. S = 4 · π · R² = 4 · π · $r² = 4 · π · ${r * r} = ${sCoeff}π.",
                    estimatedTime = 120,
                    tags = "puza2,shar,sirt_yuzi",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 4.4: 15°-75°-90° maxsus to'g'ri burchakli uchburchak xossasi (50 questions)
        for (i in 1..50) {
            val h = 3 + (i % 5)
            val c = 4 * h
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 1",
                    topic = "Uchburchaklar",
                    subtopic = "15°-75°-90° uchburchak xossasi",
                    difficulty = "VERY_HARD",
                    questionText = "Burchaklari 15°, 75° va 90° bo'lgan to'g'ri burchakli uchburchakda to'g'ri burchak uchidan gipotenuzaga tushirilgan balandlik h = $h ga teng. Gipotenuza c ning uzunligini toping.",
                    diagram = "TYPE:TRIANGLE_RIGHT;h=$h;c=x",
                    formulaUsed = "15°-75°-90° xossasi: c = 4h",
                    correctValue = "$c",
                    distractors = listOf("${c - 2}", "${c + 4}", "${2 * h}"),
                    explanation = "Puza Geometriya kitobidagi muhim qoidaga ko'ra, 15°-75°-90° to'g'ri burchakli uchburchakda gipotenuzaga tushirilgan balandlik gipotenuzaning to'rtdan biriga teng: h = c / 4.",
                    solutionSteps = "1. Qoidaga ko'ra: c = 4 · h\n2. c = 4 · $h = $c.",
                    estimatedTime = 120,
                    tags = "puza1,15_75_90,maxsus_burchak",
                    idIndex = idx++
                )
            )
        }

        // Subtopic 4.5: Piramida va Tetraedr (Puza 2) (50 questions)
        for (i in 1..50) {
            // Muntazam to'rtburchakli piramida: V = (1/3) * a^2 * H
            val a = 6
            val h = 3 + (i % 5) * 3
            val v = (a * a * h) / 3
            list.add(
                createQuestion(
                    bookSource = "Puza Geometriya 2",
                    topic = "Fazoviy geometriya (Stereometriya)",
                    subtopic = "Muntazam piramida hajmi",
                    difficulty = "VERY_HARD",
                    questionText = "Muntazam to'rtburchakli piramidaning asosidagi tomoni a = $a ga, balandligi esa H = $h ga teng. Ushbu piramidaning hajmini toping.",
                    diagram = "TYPE:GENERAL",
                    formulaUsed = "Piramida hajmi: V = (1/3) · S_asos · H = (1/3) · a² · H",
                    correctValue = "$v",
                    distractors = listOf("${v * 3}", "${v + 12}", "${v - 12}"),
                    explanation = "Piramida hajmi asos yuzi va balandlik ko'paytmasining uchdan biriga teng.",
                    solutionSteps = "1. S_asos = a² = $a² = ${a * a}\n2. V = (1/3) · S_asos · H = (1/3) · ${a * a} · $h = $v.",
                    estimatedTime = 130,
                    tags = "puza2,piramida,hajm",
                    idIndex = idx++
                )
            )
        }

        return list.take(count)
    }
}
