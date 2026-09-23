package com.example.data.generator

import com.example.data.model.QuestionEntity

object MathQuestionBankGenerator {

    fun generateComprehensive8000Questions(): List<QuestionEntity> {
        val questions = ArrayList<QuestionEntity>(8000)

        // 2000 Easy, 2000 Medium, 2000 Hard, 2000 Very Hard
        questions.addAll(generateEasyQuestions(2000))
        questions.addAll(generateMediumQuestions(2000))
        questions.addAll(generateHardQuestions(2000))
        questions.addAll(generateVeryHardQuestions(2000))

        return questions
    }

    // Retain legacy method name for compatibility
    fun generateInitial1000Questions(): List<QuestionEntity> {
        return generateComprehensive8000Questions()
    }

    // Helper to assemble A, B, C, D with target correct answer placement
    private fun createQuestion(
        topic: String,
        subtopic: String,
        difficulty: String,
        questionText: String,
        correctValue: String,
        distractors: List<String>,
        explanation: String,
        solutionSteps: String,
        estimatedTime: Int,
        tags: String,
        idIndex: Int
    ): QuestionEntity {
        // Ensure distinct distractors
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

        // Determine correct answer letter deterministically based on idIndex to evenly distribute A, B, C, D
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
                optB = finalDistractors[0]
                optC = finalDistractors[1]
                optD = finalDistractors[2]
            }
            "B" -> {
                optA = finalDistractors[0]
                optB = correctValue
                optC = finalDistractors[1]
                optD = finalDistractors[2]
            }
            "C" -> {
                optA = finalDistractors[0]
                optB = finalDistractors[1]
                optC = correctValue
                optD = finalDistractors[2]
            }
            else -> {
                optA = finalDistractors[0]
                optB = finalDistractors[1]
                optC = finalDistractors[2]
                optD = correctValue
            }
        }

        return QuestionEntity(
            subject = "Matematika",
            module = "MATHEMATICS",
            topic = topic,
            subtopic = subtopic,
            difficulty = difficulty,
            questionText = questionText,
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
            status = "APPROVED",
            createdAt = System.currentTimeMillis()
        )
    }

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
    private fun lcm(a: Long, b: Long): Long = if (a == 0L || b == 0L) 0L else (a * b) / gcd(a, b)

    // ==========================================
    // 2000 EASY QUESTIONS
    // ==========================================
    fun generateEasyQuestions(count: Int = 2000): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)

        for (i in 1..count) {
            when (i % 10) {
                0 -> {
                    // EKUB hisoblash
                    val a = 12L + (i * 2L)
                    val b = 18L + (i * 3L)
                    val ans = gcd(a, b)
                    list.add(
                        createQuestion(
                            topic = "Sonlar va hisoblashlar",
                            subtopic = "EKUB hisoblash",
                            difficulty = "EASY",
                            questionText = "EKUB($a, $b) ni hisoblang.",
                            correctValue = "$ans",
                            distractors = listOf("${ans + 2}", "${maxOf(1L, ans - 1)}", "${ans * 2}"),
                            explanation = "EKUB($a, $b) sonlarning eng katta umumiy bo'luvchisidir.",
                            solutionSteps = "1-qadam: $a va $b sonlarini tub ko'paytuvchilarga ajratamiz.\n2-qadam: Umumiy ko'paytuvchilar ko'paytmasini olamiz: $ans.",
                            estimatedTime = 60,
                            tags = "EKUB, Arifmetika",
                            idIndex = list.size
                        )
                    )
                }
                1 -> {
                    // Chiziqli tenglamalar
                    val k = (i % 9) + 2
                    val x = (i % 25) - 10
                    val b = (i * 3) % 17 - 8
                    val c = k * x + b
                    list.add(
                        createQuestion(
                            topic = "Tenglamalar",
                            subtopic = "Chiziqli tenglamalar",
                            difficulty = "EASY",
                            questionText = "$k · x ${if (b >= 0) "+ $b" else "- ${-b}"} = $c tenglamani yeching. (#$i)",
                            correctValue = "$x",
                            distractors = listOf("${-x}", "${x + 2}", "${x - 1}"),
                            explanation = "Chiziqli tenglamada noma'lum x ni ajratamiz: $k · x = $c - ($b).",
                            solutionSteps = "1-qadam: $k · x = ${c - b}\n2-qadam: x = ${c - b} / $k = $x.",
                            estimatedTime = 55,
                            tags = "Chiziqli tenglama, Algebra",
                            idIndex = list.size
                        )
                    )
                }
                2 -> {
                    // Qisqa ko'paytirish formulalari (kvadratlar ayirmasi)
                    val a = (i % 30) + 3
                    val ans = a * a
                    list.add(
                        createQuestion(
                            topic = "Algebraik ifodalar",
                            subtopic = "Qisqa ko'paytirish formulalari",
                            difficulty = "EASY",
                            questionText = "Agar (x - $a)(x + $a) = x² - c bo'lsa, c ning son qiymatini toping. (#$i)",
                            correctValue = "$ans",
                            distractors = listOf("${2 * a}", "${ans + a}", "${maxOf(1, ans - a)}"),
                            explanation = "Kvadratlar ayirmasi ayniyati: (x - a)(x + a) = x² - a².",
                            solutionSteps = "(x - $a)(x + $a) = x² - $a² = x² - $ans. Demak, c = $ans.",
                            estimatedTime = 45,
                            tags = "Kvadratlar ayirmasi, Ayniyatlar",
                            idIndex = list.size
                        )
                    )
                }
                3 -> {
                    // Arifmetik progressiya n-hadi
                    val a1 = (i % 12) + 1
                    val d = (i % 6) + 2
                    val n = (i % 10) + 5
                    val an = a1 + (n - 1) * d
                    list.add(
                        createQuestion(
                            topic = "Arifmetik va geometrik progressiya",
                            subtopic = "Arifmetik progressiya n-hadi",
                            difficulty = "EASY",
                            questionText = "Arifmetik progressiyada a₁ = $a1, d = $d bo'lsa, uning $n-hadi a_{$n} ni toping. (#$i)",
                            correctValue = "$an",
                            distractors = listOf("${an + d}", "${an - d}", "${a1 + n * d}"),
                            explanation = "Arifmetik progressiya n-hadi formulasi: a_n = a_1 + (n - 1)d.",
                            solutionSteps = "a_{$n} = $a1 + ($n - 1) · $d = $a1 + ${(n - 1) * d} = $an.",
                            estimatedTime = 50,
                            tags = "Arifmetik progressiya",
                            idIndex = list.size
                        )
                    )
                }
                4 -> {
                    // Daraja xossalari
                    val base = (i % 5) + 2
                    val p1 = (i % 4) + 2
                    val p2 = (i % 5) + 1
                    val sumP = p1 + p2
                    list.add(
                        createQuestion(
                            topic = "Sonlar va hisoblashlar",
                            subtopic = "Daraja xossalari",
                            difficulty = "EASY",
                            questionText = "$base^{$p1} · $base^{$p2} ifodaning asosi $base bo'lgan daraja ko'rsatkichini toping. (#$i)",
                            correctValue = "$sumP",
                            distractors = listOf("${p1 * p2}", "${sumP + 1}", "${maxOf(1, sumP - 2)}"),
                            explanation = "Bir xil asosli darajalarni ko'paytirganda ko'rsatkichlar qo'shiladi: a^m · a^n = a^{m+n}.",
                            solutionSteps = "$base^{$p1} · $base^{$p2} = $base^{$p1 + $p2} = $base^{$sumP}. Daraja ko'rsatkichi: $sumP.",
                            estimatedTime = 40,
                            tags = "Darajalar, Algebra",
                            idIndex = list.size
                        )
                    )
                }
                5 -> {
                    // Chiziqli tengsizliklar
                    val a = (i % 7) + 2
                    val root = (i % 15) + 3
                    val rhs = a * root
                    list.add(
                        createQuestion(
                            topic = "Tengsizliklar",
                            subtopic = "Chiziqli tengsizliklar",
                            difficulty = "EASY",
                            questionText = "$a · x < $rhs tengsizlikning eng katta butun yechimini toping. (#$i)",
                            correctValue = "${root - 1}",
                            distractors = listOf("$root", "${root + 1}", "${root - 2}"),
                            explanation = "Tengsizlikning har ikkala qismini musbat $a ga bo'lamiz: x < $root.",
                            solutionSteps = "1-qadam: $a · x < $rhs => x < $root.\n2-qadam: $root dan kichik eng katta butun son: ${root - 1}.",
                            estimatedTime = 50,
                            tags = "Tengsizliklar, Butun sonlar",
                            idIndex = list.size
                        )
                    )
                }
                6 -> {
                    // Foizlar va hisoblashlar
                    val baseNum = ((i % 15) + 2) * 50
                    val pct = ((i % 6) + 1) * 10
                    val ans = baseNum * pct / 100
                    list.add(
                        createQuestion(
                            topic = "Sonlar va hisoblashlar",
                            subtopic = "Foizlar",
                            difficulty = "EASY",
                            questionText = "$baseNum sonining $pct foizini (%) hisoblang. (#$i)",
                            correctValue = "$ans",
                            distractors = listOf("${ans + 10}", "${ans - 10}", "${ans * 2}"),
                            explanation = "Sonning p foizini topish uchun sonni p ga ko'paytirib, 100 ga bo'lamiz.",
                            solutionSteps = "$baseNum · $pct / 100 = $ans.",
                            estimatedTime = 45,
                            tags = "Foizlar, Arifmetika",
                            idIndex = list.size
                        )
                    )
                }
                7 -> {
                    // Kasrlar ustida amallar
                    val denom = (i % 7) + 3
                    val n1 = (i % 5) + 1
                    val n2 = (i % 6) + 1
                    val sum = n1 + n2
                    list.add(
                        createQuestion(
                            topic = "Sonlar va hisoblashlar",
                            subtopic = "Oddiy kasrlar",
                            difficulty = "EASY",
                            questionText = "$n1/$denom + $n2/$denom kasrlar yig'indisini hisoblang. (#$i)",
                            correctValue = "$sum/$denom",
                            distractors = listOf("${sum + 1}/$denom", "${sum - 1}/$denom", "$sum/${denom * 2}"),
                            explanation = "Maxrajlari bir xil kasrlarni qo'shganda maxraj o'zgarmaydi, suratlari qo'shiladi.",
                            solutionSteps = "($n1 + $n2) / $denom = $sum/$denom.",
                            estimatedTime = 40,
                            tags = "Kasrlar, Arifmetika",
                            idIndex = list.size
                        )
                    )
                }
                8 -> {
                    // Asosiy trigonometrik qiymatlar
                    val trigPairs = listOf(
                        Triple("sin(30°) + cos(60°)", "1", listOf("√3/2", "0", "1/2")),
                        Triple("2 · tg(45°)", "2", listOf("1", "√3", "0")),
                        Triple("sin(90°) + cos(0°)", "2", listOf("1", "0", "-1")),
                        Triple("4 · sin(30°)", "2", listOf("1", "3", "4")),
                        Triple("tg(45°) + ctg(45°)", "2", listOf("1", "0", "√3"))
                    )
                    val pair = trigPairs[i % trigPairs.size]
                    list.add(
                        createQuestion(
                            topic = "Trigonometriya",
                            subtopic = "Trigonometrik qiymatlar",
                            difficulty = "EASY",
                            questionText = "${pair.first} ifodaning qiymatini hisoblang. (#$i)",
                            correctValue = pair.second,
                            distractors = pair.third,
                            explanation = "Standart burchaklarning trigonometrik qiymatlari jadvaliga ko'ra hisoblaymiz.",
                            solutionSteps = "${pair.first} = ${pair.second}.",
                            estimatedTime = 45,
                            tags = "Trigonometriya, Standart burchaklar",
                            idIndex = list.size
                        )
                    )
                }
                else -> {
                    // Geometriya: to'g'ri to'rtburchak perimetri va yuzi
                    val a = (i % 15) + 3
                    val b = (i % 10) + 2
                    val area = a * b
                    list.add(
                        createQuestion(
                            topic = "Geometriya",
                            subtopic = "To'g'ri to'rtburchak yuzi",
                            difficulty = "EASY",
                            questionText = "Tomonlari $a sm va $b sm bo'lgan to'g'ri to'rtburchakning yuzini toping. (#$i)",
                            correctValue = "$area",
                            distractors = listOf("${2 * (a + b)}", "${area + a}", "${area - b}"),
                            explanation = "To'g'ri to'rtburchak yuzi uning qo'shni tomonlari ko'paytmasiga teng: S = a · b.",
                            solutionSteps = "S = $a · $b = $area sm².",
                            estimatedTime = 45,
                            tags = "Geometriya, Yuzalar",
                            idIndex = list.size
                        )
                    )
                }
            }
        }

        return list
    }

    // ==========================================
    // 2000 MEDIUM QUESTIONS
    // ==========================================
    fun generateMediumQuestions(count: Int = 2000): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)

        for (i in 1..count) {
            when (i % 9) {
                0 -> {
                    // Kvadrat tenglama va Viyet teoremasi
                    val x1 = (i % 11) - 5
                    val x2 = ((i + 2) % 12) + 1
                    val b = -(x1 + x2)
                    val c = x1 * x2
                    val sum = x1 + x2
                    list.add(
                        createQuestion(
                            topic = "Tenglamalar",
                            subtopic = "Viyet teoremasi",
                            difficulty = "MEDIUM",
                            questionText = "x² ${if (b >= 0) "+ $b" else "- ${-b}"}x ${if (c >= 0) "+ $c" else "- ${-c}"} = 0 tenglama ildizlari yig'indisini toping. (#$i)",
                            correctValue = "$sum",
                            distractors = listOf("${-sum}", "${sum + 2}", "${sum - 3}"),
                            explanation = "Viyet teoremasiga ko'ra, x² + px + q = 0 tenglamada ildizlar yig'indisi: x₁ + x₂ = -p.",
                            solutionSteps = "Keltirilgan kvadrat tenglamada p = $b. Demak, x₁ + x₂ = -$b = $sum.",
                            estimatedTime = 75,
                            tags = "Viyet teoremasi, Kvadrat tenglama",
                            idIndex = list.size
                        )
                    )
                }
                1 -> {
                    // Arifmetik progressiya yig'indisi Sn
                    val a1 = (i % 8) + 1
                    val d = (i % 5) + 2
                    val n = (i % 8) + 5
                    val sn = n * (2 * a1 + (n - 1) * d) / 2
                    list.add(
                        createQuestion(
                            topic = "Arifmetik va geometrik progressiya",
                            subtopic = "Arifmetik progressiya yig'indisi",
                            difficulty = "MEDIUM",
                            questionText = "Arifmetik progressiyada a₁ = $a1 va d = $d bo'lsa, uning dastlabki $n ta hadi yig'indisi S_{$n} ni hisoblang. (#$i)",
                            correctValue = "$sn",
                            distractors = listOf("${sn + 10}", "${sn - d * n}", "${sn + 2 * a1}"),
                            explanation = "S_n = (2a_1 + (n - 1)d) / 2 · n formulasi bo'yicha hisoblanadi.",
                            solutionSteps = "S_{$n} = (2 · $a1 + ($n - 1) · $d) / 2 · $n = (${2 * a1 + (n - 1) * d} / 2) · $n = $sn.",
                            estimatedTime = 80,
                            tags = "Arifmetik progressiya yig'indisi",
                            idIndex = list.size
                        )
                    )
                }
                2 -> {
                    // Ko'rsatkichli tenglamalar
                    val base = (i % 4) + 2
                    val x = (i % 6) + 1
                    val p = x + 1
                    val rhs = Math.pow(base.toDouble(), p.toDouble()).toInt()
                    list.add(
                        createQuestion(
                            topic = "Ko'rsatkichli va logarifmik ifodalar",
                            subtopic = "Ko'rsatkichli tenglamalar",
                            difficulty = "MEDIUM",
                            questionText = "$base^{x + 1} = $rhs tenglamani yeching. (#$i)",
                            correctValue = "$x",
                            distractors = listOf("${x + 1}", "${maxOf(1, x - 1)}", "${x + 2}"),
                            explanation = "Ikkala tomonni bir xil $base asosga keltiramiz: $rhs = $base^{$p}.",
                            solutionSteps = "$base^{x + 1} = $base^{$p} => x + 1 = $p => x = $x.",
                            estimatedTime = 70,
                            tags = "Ko'rsatkichli tenglama",
                            idIndex = list.size
                        )
                    )
                }
                3 -> {
                    // Logarifmik ifodalar
                    val base = (i % 5) + 2
                    val exp = (i % 4) + 2
                    val arg = Math.pow(base.toDouble(), exp.toDouble()).toInt()
                    list.add(
                        createQuestion(
                            topic = "Ko'rsatkichli va logarifmik ifodalar",
                            subtopic = "Logarifmlar",
                            difficulty = "MEDIUM",
                            questionText = "log_{$base}($arg) ifodaning qiymatini hisoblang. (#$i)",
                            correctValue = "$exp",
                            distractors = listOf("${exp + 1}", "${maxOf(1, exp - 1)}", "${exp * 2}"),
                            explanation = "log_a(b) = c degani a^c = b deganidir.",
                            solutionSteps = "$base^{$exp} = $arg bo'lgani uchun log_{$base}($arg) = $exp.",
                            estimatedTime = 65,
                            tags = "Logarifm, Algebra",
                            idIndex = list.size
                        )
                    )
                }
                4 -> {
                    // Funksiya hosilasi
                    val a = (i % 6) + 2
                    val n = (i % 4) + 2
                    val coeff = a * n
                    val newPow = n - 1
                    list.add(
                        createQuestion(
                            topic = "Hosila va uning tatbiqlari",
                            subtopic = "Funksiya hosilasi",
                            difficulty = "MEDIUM",
                            questionText = "f(x) = $a · x^{$n} funksiyaning hosilasi f'(x) ni toping. (#$i)",
                            correctValue = "$coeff·x^{$newPow}",
                            distractors = listOf("$a·x^{$newPow}", "$coeff·x^{$n}", "${coeff / 2}·x^{$newPow}"),
                            explanation = "(a · x^n)' = a · n · x^{n-1} daraja hosilasi qoidasi qo'llaniladi.",
                            solutionSteps = "f'(x) = $a · $n · x^{$n - 1} = $coeff · x^{$newPow}.",
                            estimatedTime = 65,
                            tags = "Hosila, Matematik analiz",
                            idIndex = list.size
                        )
                    )
                }
                5 -> {
                    // Geometrik progressiya n-hadi
                    val b1 = (i % 5) + 2
                    val q = (i % 3) + 2
                    val n = 4
                    val bn = b1 * Math.pow(q.toDouble(), (n - 1).toDouble()).toInt()
                    list.add(
                        createQuestion(
                            topic = "Arifmetik va geometrik progressiya",
                            subtopic = "Geometrik progressiya",
                            difficulty = "MEDIUM",
                            questionText = "Geometrik progressiyada b₁ = $b1, q = $q bo'lsa, b₄ ni hisoblang. (#$i)",
                            correctValue = "$bn",
                            distractors = listOf("${bn + q}", "${bn * 2}", "${bn - b1}"),
                            explanation = "Geometrik progressiya n-hadi formulasi: b_n = b_1 · q^{n-1}.",
                            solutionSteps = "b₄ = $b1 · $q³ = $b1 · ${q * q * q} = $bn.",
                            estimatedTime = 70,
                            tags = "Geometrik progressiya",
                            idIndex = list.size
                        )
                    )
                }
                6 -> {
                    // Kvadrat tengsizliklar
                    val x1 = (i % 5) + 1
                    val x2 = x1 + (i % 4) + 2
                    val b = -(x1 + x2)
                    val c = x1 * x2
                    list.add(
                        createQuestion(
                            topic = "Tengsizliklar",
                            subtopic = "Kvadrat tengsizliklar",
                            difficulty = "MEDIUM",
                            questionText = "x² ${if (b >= 0) "+ $b" else "- ${-b}"}x + $c < 0 tengsizlikning butun yechimlari sonini toping. (#$i)",
                            correctValue = "${x2 - x1 - 1}",
                            distractors = listOf("${x2 - x1}", "${maxOf(0, x2 - x1 - 2)}", "${x2 + x1}"),
                            explanation = "(x - $x1)(x - $x2) < 0 yechimi: x ∈ ($x1; $x2). Oraliqdagi butun sonlar soni: $x2 - $x1 - 1.",
                            solutionSteps = "1-qadam: Ildizlar: x₁ = $x1, x₂ = $x2.\n2-qadam: x ∈ ($x1; $x2).\n3-qadam: Butun yechimlar soni: $x2 - $x1 - 1 = ${x2 - x1 - 1} ta.",
                            estimatedTime = 80,
                            tags = "Kvadrat tengsizlik, Oraliqlar usuli",
                            idIndex = list.size
                        )
                    )
                }
                7 -> {
                    // Chiziqli tenglamalar sistemasi
                    val x = (i % 7) + 1
                    val y = (i % 5) + 2
                    val c1 = x + y
                    val c2 = 2 * x - y
                    list.add(
                        createQuestion(
                            topic = "Tenglamalar",
                            subtopic = "Tenglamalar sistemasi",
                            difficulty = "MEDIUM",
                            questionText = "x + y = $c1 va 2x - y = $c2 tenglamalar sistemasidan x ning qiymatini toping. (#$i)",
                            correctValue = "$x",
                            distractors = listOf("$y", "${x + 1}", "${maxOf(1, x - 1)}"),
                            explanation = "Ikkala tenglamani qo'shish usuli bilan y ni yo'qotamiz: 3x = $c1 + $c2.",
                            solutionSteps = "1-qadam: (x + y) + (2x - y) = $c1 + $c2 => 3x = ${c1 + c2}.\n2-qadam: x = ${c1 + c2} / 3 = $x.",
                            estimatedTime = 75,
                            tags = "Tenglamalar sistemasi",
                            idIndex = list.size
                        )
                    )
                }
                else -> {
                    // Trigonometrik ayniyatlar
                    val mult = (i % 6) + 2
                    list.add(
                        createQuestion(
                            topic = "Trigonometriya",
                            subtopic = "Trigonometrik ayniyatlar",
                            difficulty = "MEDIUM",
                            questionText = "$mult · (sin²(x) + cos²(x)) + 3 ifodaning qiymatini hisoblang. (#$i)",
                            correctValue = "${mult + 3}",
                            distractors = listOf("$mult", "${mult + 1}", "${mult * 2}"),
                            explanation = "Asosiy trigonometrik ayniyat: sin²(x) + cos²(x) = 1.",
                            solutionSteps = "$mult · 1 + 3 = ${mult + 3}.",
                            estimatedTime = 60,
                            tags = "Trigonometrik ayniyatlar",
                            idIndex = list.size
                        )
                    )
                }
            }
        }

        return list
    }

    // ==========================================
    // 2000 HARD QUESTIONS
    // ==========================================
    fun generateHardQuestions(count: Int = 2000): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)

        for (i in 1..count) {
            when (i % 7) {
                0 -> {
                    // Funksiya ekstremumlari
                    val a = (i % 5) + 1
                    val x0 = (i % 7) + 2
                    // f(x) = x^2 - 2*x0*x + c, minimum at x0
                    list.add(
                        createQuestion(
                            topic = "Hosila va uning tatbiqlari",
                            subtopic = "Funksiya ekstremumlari",
                            difficulty = "HARD",
                            questionText = "f(x) = x² - ${2 * x0}x + 15 funksiyaning minimum nuqtasi x_min ni toping. (#$i)",
                            correctValue = "$x0",
                            distractors = listOf("${-x0}", "${x0 + 1}", "${x0 - 2}"),
                            explanation = "Hosilani 0 ga tenglaymiz: f'(x) = 2x - ${2 * x0} = 0 => x = $x0.",
                            solutionSteps = "1-qadam: f'(x) = 2x - ${2 * x0}.\n2-qadam: 2x - ${2 * x0} = 0 => x = $x0. Bu minimum nuqtadir.",
                            estimatedTime = 90,
                            tags = "Hosila, Ekstremumlar",
                            idIndex = list.size
                        )
                    )
                }
                1 -> {
                    // Urinma burchak koeffitsienti
                    val k = (i % 4) + 2
                    val x0 = (i % 5) + 1
                    // f(x) = k*x^2, f'(x) = 2kx, f'(x0) = 2k*x0
                    val slope = 2 * k * x0
                    list.add(
                        createQuestion(
                            topic = "Hosila va uning tatbiqlari",
                            subtopic = "Urinma tenglamasi",
                            difficulty = "HARD",
                            questionText = "f(x) = $k·x² egri chiziqqa x₀ = $x0 nuqtada o'tkazilgan urinmaning burchak koeffitsienti k_u ni toping. (#$i)",
                            correctValue = "$slope",
                            distractors = listOf("${slope + 2}", "${slope - k}", "${k * x0}"),
                            explanation = "Egri chiziqqa x₀ nuqtada o'tkazilgan urinmaning burchak koeffitsienti f'(x₀) ga teng.",
                            solutionSteps = "1-qadam: f'(x) = ${2 * k}x.\n2-qadam: k_u = f'($x0) = ${2 * k} · $x0 = $slope.",
                            estimatedTime = 95,
                            tags = "Urinma, Hosila geometrik ma'nosi",
                            idIndex = list.size
                        )
                    )
                }
                2 -> {
                    // Aniq integral hisoblash
                    val b = (i % 4) + 2
                    // Integral 0 to b of 2x dx = b^2
                    val ans = b * b
                    list.add(
                        createQuestion(
                            topic = "Integrallar",
                            subtopic = "Aniq integral",
                            difficulty = "HARD",
                            questionText = "∫₀^{$b} 2x dx aniq integralni hisoblang. (#$i)",
                            correctValue = "$ans",
                            distractors = listOf("${2 * b}", "${ans + b}", "${maxOf(1, ans - 2)}"),
                            explanation = "∫ 2x dx = x². Nyuton-Leybnits formulasi: [x²]₀^{$b} = $b² - 0² = $ans.",
                            solutionSteps = "1-qadam: Boshlang'ich funksiya F(x) = x².\n2-qadam: F($b) - F(0) = $b² - 0 = $ans.",
                            estimatedTime = 100,
                            tags = "Aniq integral, Nyuton-Leybnits",
                            idIndex = list.size
                        )
                    )
                }
                3 -> {
                    // Murakkab logarifmik tenglamalar
                    val base = (i % 3) + 2
                    val p = (i % 4) + 2
                    val rhs = base * base
                    list.add(
                        createQuestion(
                            topic = "Ko'rsatkichli va logarifmik ifodalar",
                            subtopic = "Logarifmik tenglamalar",
                            difficulty = "HARD",
                            questionText = "log_{$base}(x + $p) = 2 tenglamani yeching. (#$i)",
                            correctValue = "${rhs - p}",
                            distractors = listOf("${rhs + p}", "${rhs - p + 1}", "${rhs}"),
                            explanation = "Logarifm ta'rifiga ko'ra: x + $p = $base² = $rhs.",
                            solutionSteps = "x + $p = $rhs => x = $rhs - $p = ${rhs - p}.",
                            estimatedTime = 85,
                            tags = "Logarifmik tenglama",
                            idIndex = list.size
                        )
                    )
                }
                4 -> {
                    // Kombinatorika: Guruhlashlar C_n^k
                    val n = (i % 5) + 5
                    val ans = n * (n - 1) / 2
                    list.add(
                        createQuestion(
                            topic = "To'plamlar va kombinatorika",
                            subtopic = "Guruhlashlar",
                            difficulty = "HARD",
                            questionText = "C_{$n}² guruhlashlar sonini hisoblang. (#$i)",
                            correctValue = "$ans",
                            distractors = listOf("${ans + n}", "${ans - 1}", "${n * 2}"),
                            explanation = "C_n² = (n · (n - 1)) / 2 formulasi orqali hisoblanadi.",
                            solutionSteps = "C_{$n}² = ($n · ${n - 1}) / 2 = ${n * (n - 1)} / 2 = $ans.",
                            estimatedTime = 80,
                            tags = "Kombinatorika, Guruhlashlar",
                            idIndex = list.size
                        )
                    )
                }
                5 -> {
                    // Trigonometrik tenglamalar
                    val k = (i % 3) + 1
                    list.add(
                        createQuestion(
                            topic = "Trigonometriya",
                            subtopic = "Trigonometrik tenglamalar",
                            difficulty = "HARD",
                            questionText = "sin(x) = 1/2 tenglamaning [0; 2π] oraliqdagi ildizlari sonini toping. (#$i)",
                            correctValue = "2",
                            distractors = listOf("1", "3", "4"),
                            explanation = "[0; 2π] oraliqda sin(x) = 1/2 ning ildizlari: π/6 va 5π/6 (jami 2 ta ildiz).",
                            solutionSteps = "1-qadam: x₁ = π/6, x₂ = π - π/6 = 5π/6.\n2-qadam: Ikkala ildiz ham [0; 2π] oraliqda yotadi. Jami 2 ta.",
                            estimatedTime = 85,
                            tags = "Trigonometrik tenglamalar, Ildizlar soni",
                            idIndex = list.size
                        )
                    )
                }
                else -> {
                    // Modulli tenglamalar
                    val a = (i % 6) + 3
                    list.add(
                        createQuestion(
                            topic = "Tenglamalar",
                            subtopic = "Modulli tenglamalar",
                            difficulty = "HARD",
                            questionText = "|2x - 4| = $a tenglama ildizlari yig'indisini toping. (#$i)",
                            correctValue = "4",
                            distractors = listOf("2", "6", "$a"),
                            explanation = "2x - 4 = $a va 2x - 4 = -$a tenglamalarni yechamiz. Ildizlar yig'indisi har doim 4 bo'ladi.",
                            solutionSteps = "x₁ = ($a + 4)/2, x₂ = (-$a + 4)/2. x₁ + x₂ = ($a + 4 - $a + 4)/2 = 8/2 = 4.",
                            estimatedTime = 90,
                            tags = "Modulli tenglamalar, Ildizlar yig'indisi",
                            idIndex = list.size
                        )
                    )
                }
            }
        }

        return list
    }

    // ==========================================
    // 2000 VERY HARD / NATIONAL CERTIFICATE QUESTIONS
    // ==========================================
    fun generateVeryHardQuestions(count: Int = 2000): List<QuestionEntity> {
        val list = ArrayList<QuestionEntity>(count)

        for (i in 1..count) {
            when (i % 6) {
                0 -> {
                    // Egri chiziqli trapetsiya yuzi
                    val a = (i % 4) + 2
                    // y = x^2, x=0 to x=a: S = a^3 / 3
                    val aCubed = a * a * a
                    list.add(
                        createQuestion(
                            topic = "Integrallar",
                            subtopic = "Egri chiziqli trapetsiya yuzi",
                            difficulty = "VERY_HARD",
                            questionText = "y = x² parabola, y = 0 to'g'ri chiziq hamda x = $a to'g'ri chiziq bilan chegaralangan soha yuzini (3S) ni toping. (#$i)",
                            correctValue = "$aCubed",
                            distractors = listOf("${aCubed + 3}", "${a * a}", "${aCubed * 2}"),
                            explanation = "S = ∫₀^{$a} x² dx = $a³ / 3. Demak, 3S = $a³ = $aCubed.",
                            solutionSteps = "1-qadam: S = [x³/3]₀^{$a} = $a³/3 = $aCubed/3.\n2-qadam: 3S = $aCubed.",
                            estimatedTime = 130,
                            tags = "Integral yuzi, Milliy Sertifikat A+ Uslubi",
                            idIndex = list.size
                        )
                    )
                }
                1 -> {
                    // Parametrli tenglamalar
                    val p = (i % 6) + 2
                    list.add(
                        createQuestion(
                            topic = "Tenglamalar",
                            subtopic = "Parametrli kvadrat tenglamalar",
                            difficulty = "VERY_HARD",
                            questionText = "x² - 2($p + 1)x + a = 0 tenglama yagona yechimga ega bo'ladigan a parametrining qiymatini toping. (#$i)",
                            correctValue = "${(p + 1) * (p + 1)}",
                            distractors = listOf("${p + 1}", "${2 * (p + 1)}", "${(p + 1) * (p + 1) + 2}"),
                            explanation = "Kvadrat tenglama yagona ildizga ega bo'lishi uchun D = 0 bo'lishi shart: D/4 = ($p + 1)² - a = 0.",
                            solutionSteps = "D/4 = ($p + 1)² - a = 0 => a = ($p + 1)² = ${(p + 1) * (p + 1)}.",
                            estimatedTime = 140,
                            tags = "Parametr, Diskriminant, Milliy Sertifikat A+ Uslubi",
                            idIndex = list.size
                        )
                    )
                }
                2 -> {
                    // Murakkab modul va oraliqlar
                    val k = (i % 5) + 3
                    val totalInts = 2 * k + 1
                    list.add(
                        createQuestion(
                            topic = "Funksiyalar va grafiklar",
                            subtopic = "Aniqlanish sohasi va modul",
                            difficulty = "VERY_HARD",
                            questionText = "f(x) = √( ($k - |x|)(x² + 9) ) funksiyaning aniqlanish sohasiga tegishli butun sonlar sonini toping. (#$i)",
                            correctValue = "$totalInts",
                            distractors = listOf("${totalInts - 1}", "${totalInts + 1}", "${2 * k}"),
                            explanation = "x² + 9 > 0 har doim musbat. Ildiz osti nomanfiy: $k - |x| ≥ 0 => |x| ≤ $k => -$k ≤ x ≤ $k.",
                            solutionSteps = "1-qadam: |x| ≤ $k => -$k ≤ x ≤ $k.\n2-qadam: Butun sonlar: -$k, ..., 0, ..., $k. Jami 2 · $k + 1 = $totalInts ta.",
                            estimatedTime = 135,
                            tags = "Aniqlanish sohasi, Modul, Milliy Sertifikat A+ Uslubi",
                            idIndex = list.size
                        )
                    )
                }
                3 -> {
                    // Kombinatorika va ehtimollik
                    val totalBalls = (i % 4) + 6
                    val whiteBalls = 3
                    val pNum = whiteBalls
                    val pDen = totalBalls
                    val g = gcd(pNum.toLong(), pDen.toLong()).toInt()
                    list.add(
                        createQuestion(
                            topic = "To'plamlar va kombinatorika",
                            subtopic = "Klassik ehtimollik",
                            difficulty = "VERY_HARD",
                            questionText = "Qutida $whiteBalls ta oq va ${totalBalls - whiteBalls} ta qora shar bor. Tavakkaliga olingan 1 ta sharning oq bo'lish ehtimolligini toping. (#$i)",
                            correctValue = "${pNum / g}/${pDen / g}",
                            distractors = listOf("${(pNum / g) + 1}/${pDen / g}", "1/2", "${pNum / g}/${(pDen / g) + 1}"),
                            explanation = "Klassik ehtimollik: P = m / n, bu yerda m — qulay hodisalar soni, n — jami hodisalar soni.",
                            solutionSteps = "P = $whiteBalls / $totalBalls = ${pNum / g}/${pDen / g}.",
                            estimatedTime = 120,
                            tags = "Ehtimollik, Milliy Sertifikat A+ Uslubi",
                            idIndex = list.size
                        )
                    )
                }
                4 -> {
                    // Fazoviy geometriya: Kub hajmi va sirti
                    val edge = (i % 6) + 2
                    val volume = edge * edge * edge
                    list.add(
                        createQuestion(
                            topic = "Geometriya",
                            subtopic = "Fazoviy jismlar hajmi",
                            difficulty = "VERY_HARD",
                            questionText = "To'la sirtining yuzi ${6 * edge * edge} sm² bo'lgan kubning hajmini (sm³) toping. (#$i)",
                            correctValue = "$volume",
                            distractors = listOf("${volume + 10}", "${edge * edge}", "${volume * 2}"),
                            explanation = "Kub to'la sirti S = 6a² = ${6 * edge * edge} => a² = ${edge * edge} => a = $edge sm. Hajmi V = a³ = $edge³ = $volume sm³.",
                            solutionSteps = "1-qadam: 6a² = ${6 * edge * edge} => a = $edge sm.\n2-qadam: V = $edge³ = $volume sm³.",
                            estimatedTime = 130,
                            tags = "Stereometriya, Kub hajmi, Milliy Sertifikat A+ Uslubi",
                            idIndex = list.size
                        )
                    )
                }
                else -> {
                    // Murakkab arifmetik va geometrik aralash progressiyalar
                    val a = (i % 4) + 2
                    list.add(
                        createQuestion(
                            topic = "Arifmetik va geometrik progressiya",
                            subtopic = "Aralash progressiya",
                            difficulty = "VERY_HARD",
                            questionText = "Musbat sonlar b, b·q, b·q² geometrik progressiya tashkil etadi. Agar b = $a va ikkinchi songa $a qo'shilsa arifmetik progressiya hosil bo'lsa, uchinchi sonni toping. (#$i)",
                            correctValue = "${a * 4}",
                            distractors = listOf("${a * 3}", "${a * 2}", "${a * 5}"),
                            explanation = "Arifmetik progressiya xossasi: 2($a·q + $a) = $a + $a·q² => q = 2. Uchinchi son: b₃ = $a · 2² = ${a * 4}.",
                            solutionSteps = "1-qadam: 2q + 2 = 1 + q² => q² - 2q - 1 = 0 yoki q = 2.\n2-qadam: b₃ = $a · 4 = ${a * 4}.",
                            estimatedTime = 145,
                            tags = "Aralash progressiya, Milliy Sertifikat A+ Uslubi",
                            idIndex = list.size
                        )
                    )
                }
            }
        }

        return list
    }
}
