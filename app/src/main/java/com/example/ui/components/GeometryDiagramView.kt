package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue700
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders authentic, crisp vector geometry diagrams for Puza Geometriya problems.
 */
@Composable
fun GeometryDiagramView(
    diagramSpec: String,
    modifier: Modifier = Modifier
) {
    if (diagramSpec.isBlank()) return

    val params = parseDiagramSpec(diagramSpec)
    val type = params["TYPE"] ?: "GENERAL"
    val textMeasurer = rememberTextMeasurer()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 3f
                val primaryColor = Navy900
                val accentColor = Blue700
                val helperColor = Rose600
                val labelStyle = TextStyle(
                    color = Slate800,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                val valueStyle = TextStyle(
                    color = Blue700,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val w = size.width
                val h = size.height

                when (type) {
                    "TRIANGLE_RIGHT" -> {
                        // Right-angled triangle ABC with right angle at C
                        val pA = Offset(w * 0.15f, h * 0.82f)
                        val pC = Offset(w * 0.80f, h * 0.82f)
                        val pB = Offset(w * 0.80f, h * 0.18f)

                        // Draw triangle path
                        val path = Path().apply {
                            moveTo(pA.x, pA.y)
                            lineTo(pC.x, pC.y)
                            lineTo(pB.x, pB.y)
                            close()
                        }
                        // Shading
                        drawPath(path, color = Color(0x123B82F6), style = Fill)
                        drawPath(
                            path,
                            color = primaryColor,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Right angle indicator at C
                        val rSize = 18f
                        drawPath(
                            Path().apply {
                                moveTo(pC.x - rSize, pC.y)
                                lineTo(pC.x - rSize, pC.y - rSize)
                                lineTo(pC.x, pC.y - rSize)
                            },
                            color = helperColor,
                            style = Stroke(width = 2f)
                        )
                        drawCircle(helperColor, radius = 2.5f, center = Offset(pC.x - rSize / 2, pC.y - rSize / 2))

                        // Labels
                        drawText(textMeasurer, "A", Offset(pA.x - 22f, pA.y - 10f), style = labelStyle)
                        drawText(textMeasurer, "C", Offset(pC.x + 8f, pC.y - 6f), style = labelStyle)
                        drawText(textMeasurer, "B", Offset(pB.x + 8f, pB.y - 12f), style = labelStyle)

                        // Side value labels
                        val legA = params["a"] ?: "a"
                        val legB = params["b"] ?: "b"
                        val hypC = params["c"] ?: "c"
                        drawText(textMeasurer, legA, Offset((pA.x + pC.x) / 2 - 8f, pC.y + 6f), style = valueStyle)
                        drawText(textMeasurer, legB, Offset(pC.x + 10f, (pC.y + pB.y) / 2 - 8f), style = valueStyle)
                        drawText(textMeasurer, hypC, Offset((pA.x + pB.x) / 2 - 24f, (pA.y + pB.y) / 2 - 14f), style = TextStyle(color = Rose600, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    }

                    "TRIANGLE_ISOSCELES", "TRIANGLE_GENERAL" -> {
                        val pA = Offset(w * 0.15f, h * 0.82f)
                        val pB = Offset(w * 0.85f, h * 0.82f)
                        val pC = Offset(w * 0.50f, h * 0.18f)
                        val pH = Offset(w * 0.50f, h * 0.82f)

                        val path = Path().apply {
                            moveTo(pA.x, pA.y)
                            lineTo(pB.x, pB.y)
                            lineTo(pC.x, pC.y)
                            close()
                        }
                        drawPath(path, color = Color(0x103B82F6), style = Fill)
                        drawPath(path, color = primaryColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // Altitude h (dashed)
                        val dashed = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        drawLine(helperColor, pC, pH, strokeWidth = 2.5f, pathEffect = dashed)
                        // Right angle at H
                        val rSize = 14f
                        drawPath(
                            Path().apply {
                                moveTo(pH.x + rSize, pH.y)
                                lineTo(pH.x + rSize, pH.y - rSize)
                                lineTo(pH.x, pH.y - rSize)
                            },
                            color = helperColor,
                            style = Stroke(width = 2f)
                        )

                        drawText(textMeasurer, "A", Offset(pA.x - 20f, pA.y - 6f), style = labelStyle)
                        drawText(textMeasurer, "B", Offset(pB.x + 8f, pB.y - 6f), style = labelStyle)
                        drawText(textMeasurer, "C", Offset(pC.x - 6f, pC.y - 22f), style = labelStyle)
                        drawText(textMeasurer, "H", Offset(pH.x - 6f, pH.y + 6f), style = labelStyle)

                        val hVal = params["h"]
                        if (hVal != null) {
                            drawText(textMeasurer, "h = $hVal", Offset(pH.x + 8f, (pC.y + pH.y) / 2), style = TextStyle(color = Rose600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                        }
                        val baseVal = params["base"] ?: params["a"]
                        if (baseVal != null) {
                            drawText(textMeasurer, baseVal, Offset(w * 0.5f - 12f, pA.y + 8f), style = valueStyle)
                        }
                    }

                    "PARALLEL_LINES_Z", "PARALLEL_Z" -> {
                        // Two parallel lines with transversal
                        val y1 = h * 0.30f
                        val y2 = h * 0.70f
                        drawLine(primaryColor, Offset(w * 0.08f, y1), Offset(w * 0.92f, y1), strokeWidth = strokeWidth)
                        drawLine(primaryColor, Offset(w * 0.08f, y2), Offset(w * 0.92f, y2), strokeWidth = strokeWidth)

                        // Transversal
                        val tTop = Offset(w * 0.65f, h * 0.18f)
                        val tBot = Offset(w * 0.35f, h * 0.82f)
                        drawLine(accentColor, tTop, tBot, strokeWidth = strokeWidth)

                        // Parallel line arrows
                        drawParallelArrow(this, Offset(w * 0.85f, y1), strokeWidth)
                        drawParallelArrow(this, Offset(w * 0.85f, y2), strokeWidth)

                        // Labels
                        drawText(textMeasurer, "d₁", Offset(w * 0.92f, y1 - 18f), style = labelStyle)
                        drawText(textMeasurer, "d₂", Offset(w * 0.92f, y2 - 18f), style = labelStyle)

                        val a1 = params["angle1"] ?: "α"
                        val a2 = params["angle2"] ?: "β"
                        drawText(textMeasurer, a1, Offset(w * 0.44f, y1 + 8f), style = TextStyle(color = Amber600, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                        drawText(textMeasurer, a2, Offset(w * 0.46f, y2 - 24f), style = TextStyle(color = Amber600, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    }

                    "PARALLEL_LINES_M", "PARALLEL_M" -> {
                        val y1 = h * 0.25f
                        val y2 = h * 0.75f
                        drawLine(primaryColor, Offset(w * 0.08f, y1), Offset(w * 0.92f, y1), strokeWidth = strokeWidth)
                        drawLine(primaryColor, Offset(w * 0.08f, y2), Offset(w * 0.92f, y2), strokeWidth = strokeWidth)

                        // M-shape zigzag lines
                        val pTop = Offset(w * 0.30f, y1)
                        val pMid = Offset(w * 0.65f, h * 0.50f)
                        val pBot = Offset(w * 0.35f, y2)

                        val mPath = Path().apply {
                            moveTo(pTop.x, pTop.y)
                            lineTo(pMid.x, pMid.y)
                            lineTo(pBot.x, pBot.y)
                        }
                        drawPath(mPath, color = accentColor, style = Stroke(width = strokeWidth + 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        drawText(textMeasurer, "d₁", Offset(w * 0.92f, y1 - 16f), style = labelStyle)
                        drawText(textMeasurer, "d₂", Offset(w * 0.92f, y2 - 16f), style = labelStyle)

                        val aTop = params["angleTop"] ?: "a°"
                        val aMid = params["angleMid"] ?: "x"
                        val aBot = params["angleBottom"] ?: "b°"
                        drawText(textMeasurer, aTop, Offset(pTop.x + 8f, y1 + 6f), style = TextStyle(color = Amber600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                        drawText(textMeasurer, aMid, Offset(pMid.x - 30f, pMid.y - 8f), style = TextStyle(color = Rose600, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                        drawText(textMeasurer, aBot, Offset(pBot.x + 8f, y2 - 20f), style = TextStyle(color = Amber600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    }

                    "TRAPEZOID", "TRAPEZOID_ISOSCELES" -> {
                        val pA = Offset(w * 0.12f, h * 0.82f)
                        val pB = Offset(w * 0.88f, h * 0.82f)
                        val pC = Offset(w * 0.72f, h * 0.22f)
                        val pD = Offset(w * 0.28f, h * 0.22f)
                        val pH = Offset(w * 0.28f, h * 0.82f)

                        val path = Path().apply {
                            moveTo(pA.x, pA.y)
                            lineTo(pB.x, pB.y)
                            lineTo(pC.x, pC.y)
                            lineTo(pD.x, pD.y)
                            close()
                        }
                        drawPath(path, color = Color(0x103B82F6), style = Fill)
                        drawPath(path, color = primaryColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // Height line
                        val dashed = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        drawLine(helperColor, pD, pH, strokeWidth = 2f, pathEffect = dashed)

                        drawText(textMeasurer, "A", Offset(pA.x - 18f, pA.y - 4f), style = labelStyle)
                        drawText(textMeasurer, "B", Offset(pB.x + 6f, pB.y - 4f), style = labelStyle)
                        drawText(textMeasurer, "C", Offset(pC.x + 6f, pC.y - 18f), style = labelStyle)
                        drawText(textMeasurer, "D", Offset(pD.x - 18f, pD.y - 18f), style = labelStyle)

                        val baseA = params["a"]
                        val baseB = params["b"]
                        val hVal = params["h"]
                        if (baseA != null) drawText(textMeasurer, "a = $baseA", Offset(w * 0.5f - 20f, pA.y + 6f), style = valueStyle)
                        if (baseB != null) drawText(textMeasurer, "b = $baseB", Offset(w * 0.5f - 20f, pD.y - 20f), style = valueStyle)
                        if (hVal != null) drawText(textMeasurer, "h = $hVal", Offset(pD.x + 6f, (pD.y + pH.y) / 2 - 8f), style = TextStyle(color = Rose600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    }

                    "PARALLELOGRAM", "RHOMBUS" -> {
                        val pA = Offset(w * 0.15f, h * 0.78f)
                        val pB = Offset(w * 0.72f, h * 0.78f)
                        val pC = Offset(w * 0.85f, h * 0.22f)
                        val pD = Offset(w * 0.28f, h * 0.22f)

                        val path = Path().apply {
                            moveTo(pA.x, pA.y)
                            lineTo(pB.x, pB.y)
                            lineTo(pC.x, pC.y)
                            lineTo(pD.x, pD.y)
                            close()
                        }
                        drawPath(path, color = Color(0x103B82F6), style = Fill)
                        drawPath(path, color = primaryColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // Diagonals AC and BD
                        val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        drawLine(accentColor, pA, pC, strokeWidth = 1.8f, pathEffect = dashed)
                        drawLine(accentColor, pB, pD, strokeWidth = 1.8f, pathEffect = dashed)

                        drawText(textMeasurer, "A", Offset(pA.x - 16f, pA.y - 4f), style = labelStyle)
                        drawText(textMeasurer, "B", Offset(pB.x + 6f, pB.y - 4f), style = labelStyle)
                        drawText(textMeasurer, "C", Offset(pC.x + 6f, pC.y - 18f), style = labelStyle)
                        drawText(textMeasurer, "D", Offset(pD.x - 16f, pD.y - 18f), style = labelStyle)

                        val sideA = params["a"]
                        val sideB = params["b"]
                        if (sideA != null) drawText(textMeasurer, sideA, Offset((pA.x + pB.x) / 2, pA.y + 6f), style = valueStyle)
                        if (sideB != null) drawText(textMeasurer, sideB, Offset(pA.x - 8f, (pA.y + pD.y) / 2), style = valueStyle)
                    }

                    "CIRCLE_INSCRIBED", "CIRCLE_CENTRAL", "CIRCLE_ANGLE" -> {
                        val center = Offset(w * 0.50f, h * 0.50f)
                        val radius = minOf(w, h) * 0.38f

                        // Circle outline
                        drawCircle(primaryColor, radius = radius, center = center, style = Stroke(width = strokeWidth))
                        // Center point O
                        drawCircle(accentColor, radius = 3.5f, center = center)
                        drawText(textMeasurer, "O", Offset(center.x + 6f, center.y - 6f), style = labelStyle)

                        // Inscribed angle and central angle
                        val radA = -PI.toFloat() * 0.75f
                        val radB = PI.toFloat() * 0.15f
                        val radC = PI.toFloat() * 0.70f

                        val pA = Offset(center.x + radius * cos(radA), center.y + radius * sin(radA))
                        val pB = Offset(center.x + radius * cos(radB), center.y + radius * sin(radB))
                        val pC = Offset(center.x + radius * cos(radC), center.y + radius * sin(radC))

                        // Central angle lines: OA, OB
                        drawLine(helperColor, center, pA, strokeWidth = 2f)
                        drawLine(helperColor, center, pB, strokeWidth = 2f)

                        // Inscribed angle lines: CA, CB
                        drawLine(accentColor, pC, pA, strokeWidth = 2f)
                        drawLine(accentColor, pC, pB, strokeWidth = 2f)

                        drawText(textMeasurer, "A", Offset(pA.x - 14f, pA.y - 14f), style = labelStyle)
                        drawText(textMeasurer, "B", Offset(pB.x + 6f, pB.y - 6f), style = labelStyle)
                        drawText(textMeasurer, "C", Offset(pC.x - 14f, pC.y + 4f), style = labelStyle)

                        val centralVal = params["central"]
                        val inscribedVal = params["inscribed"]
                        if (centralVal != null) drawText(textMeasurer, centralVal, Offset(center.x + 6f, center.y - 20f), style = TextStyle(color = helperColor, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                        if (inscribedVal != null) drawText(textMeasurer, inscribedVal, Offset(pC.x + 10f, pC.y - 14f), style = TextStyle(color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    }

                    "CIRCLE_SECTOR" -> {
                        val center = Offset(w * 0.50f, h * 0.52f)
                        val radius = minOf(w, h) * 0.38f
                        val startAngle = -60f
                        val sweepAngle = 75f

                        // Draw full circle light guide
                        drawCircle(Color(0xFFE2E8F0), radius = radius, center = center, style = Stroke(width = 1.5f))

                        // Draw shaded sector
                        drawArc(
                            color = Color(0x303B82F6),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Fill
                        )
                        drawArc(
                            color = accentColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth)
                        )

                        // Center point
                        drawCircle(Navy900, radius = 3.5f, center = center)
                        drawText(textMeasurer, "O", Offset(center.x - 14f, center.y + 4f), style = labelStyle)

                        val rVal = params["r"] ?: "R"
                        val aVal = params["alpha"] ?: "α"
                        drawText(textMeasurer, "r = $rVal", Offset(center.x + radius * 0.4f, center.y - 24f), style = valueStyle)
                        drawText(textMeasurer, aVal, Offset(center.x + 14f, center.y - 12f), style = TextStyle(color = Rose600, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    }

                    "CIRCLE_RING", "CONCENTRIC_CIRCLES" -> {
                        val center = Offset(w * 0.50f, h * 0.50f)
                        val rOuter = minOf(w, h) * 0.40f
                        val rInner = minOf(w, h) * 0.22f

                        // Shaded ring
                        drawCircle(Color(0x353B82F6), radius = rOuter, center = center, style = Fill)
                        drawCircle(Color(0xFFF8FAFC), radius = rInner, center = center, style = Fill)

                        drawCircle(primaryColor, radius = rOuter, center = center, style = Stroke(width = strokeWidth))
                        drawCircle(primaryColor, radius = rInner, center = center, style = Stroke(width = strokeWidth))

                        // Radius lines
                        drawLine(Rose600, center, Offset(center.x + rInner, center.y), strokeWidth = 2f)
                        drawLine(Blue700, center, Offset(center.x, center.y - rOuter), strokeWidth = 2f)

                        drawCircle(Navy900, radius = 3f, center = center)
                        drawText(textMeasurer, "O", Offset(center.x - 14f, center.y + 2f), style = labelStyle)

                        val rVal = params["r"] ?: "r"
                        val bigRVal = params["R"] ?: "R"
                        drawText(textMeasurer, "r = $rVal", Offset(center.x + rInner * 0.3f, center.y + 4f), style = TextStyle(color = Rose600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                        drawText(textMeasurer, "R = $bigRVal", Offset(center.x + 6f, center.y - rOuter * 0.6f), style = TextStyle(color = Blue700, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    }

                    "SOLID_CYLINDER" -> {
                        val cx = w * 0.50f
                        val topY = h * 0.25f
                        val botY = h * 0.75f
                        val rx = w * 0.28f
                        val ry = h * 0.10f

                        // Side lines
                        drawLine(primaryColor, Offset(cx - rx, topY), Offset(cx - rx, botY), strokeWidth = strokeWidth)
                        drawLine(primaryColor, Offset(cx + rx, topY), Offset(cx + rx, botY), strokeWidth = strokeWidth)

                        // Top ellipse (full)
                        drawOval(primaryColor, Offset(cx - rx, topY - ry), Size(rx * 2, ry * 2), style = Stroke(width = strokeWidth))
                        // Bottom ellipse: front half solid, back half dashed
                        drawArc(primaryColor, 0f, 180f, false, Offset(cx - rx, botY - ry), Size(rx * 2, ry * 2), style = Stroke(width = strokeWidth))
                        val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        drawArc(primaryColor, 180f, 180f, false, Offset(cx - rx, botY - ry), Size(rx * 2, ry * 2), style = Stroke(width = 1.5f, pathEffect = dashed))

                        // Axis height line H
                        drawLine(helperColor, Offset(cx, topY), Offset(cx, botY), strokeWidth = 2f, pathEffect = dashed)
                        drawLine(Blue700, Offset(cx, topY), Offset(cx + rx, topY), strokeWidth = 2f)

                        val rVal = params["r"] ?: "R"
                        val hVal = params["h"] ?: "H"
                        drawText(textMeasurer, "R = $rVal", Offset(cx + rx * 0.3f, topY - 18f), style = valueStyle)
                        drawText(textMeasurer, "H = $hVal", Offset(cx + 8f, (topY + botY) / 2 - 8f), style = TextStyle(color = Rose600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    }

                    "SOLID_CONE" -> {
                        val cx = w * 0.50f
                        val topY = h * 0.18f
                        val botY = h * 0.78f
                        val rx = w * 0.30f
                        val ry = h * 0.10f

                        // Slant lines
                        drawLine(primaryColor, Offset(cx, topY), Offset(cx - rx, botY), strokeWidth = strokeWidth)
                        drawLine(primaryColor, Offset(cx, topY), Offset(cx + rx, botY), strokeWidth = strokeWidth)

                        // Base ellipse
                        drawArc(primaryColor, 0f, 180f, false, Offset(cx - rx, botY - ry), Size(rx * 2, ry * 2), style = Stroke(width = strokeWidth))
                        val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        drawArc(primaryColor, 180f, 180f, false, Offset(cx - rx, botY - ry), Size(rx * 2, ry * 2), style = Stroke(width = 1.5f, pathEffect = dashed))

                        // Height and radius
                        drawLine(helperColor, Offset(cx, topY), Offset(cx, botY), strokeWidth = 2f, pathEffect = dashed)
                        drawLine(Blue700, Offset(cx, botY), Offset(cx + rx, botY), strokeWidth = 2f, pathEffect = dashed)

                        val rVal = params["r"] ?: "R"
                        val hVal = params["h"] ?: "H"
                        val lVal = params["l"]
                        drawText(textMeasurer, "R = $rVal", Offset(cx + rx * 0.35f, botY + 4f), style = valueStyle)
                        drawText(textMeasurer, "H = $hVal", Offset(cx - 36f, (topY + botY) / 2), style = TextStyle(color = Rose600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                        if (lVal != null) {
                            drawText(textMeasurer, "L = $lVal", Offset(cx + rx * 0.6f, (topY + botY) / 2 - 10f), style = TextStyle(color = Amber600, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                        }
                    }

                    "SOLID_SPHERE" -> {
                        val center = Offset(w * 0.50f, h * 0.50f)
                        val radius = minOf(w, h) * 0.38f

                        // Circle silhouette
                        drawCircle(primaryColor, radius = radius, center = center, style = Stroke(width = strokeWidth))
                        // Equator ellipse
                        val ry = radius * 0.32f
                        drawArc(primaryColor, 0f, 180f, false, Offset(center.x - radius, center.y - ry), Size(radius * 2, ry * 2), style = Stroke(width = strokeWidth - 0.5f))
                        val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        drawArc(primaryColor, 180f, 180f, false, Offset(center.x - radius, center.y - ry), Size(radius * 2, ry * 2), style = Stroke(width = 1.5f, pathEffect = dashed))

                        // Radius vector
                        drawLine(Rose600, center, Offset(center.x + radius, center.y), strokeWidth = 2.5f)
                        drawCircle(Navy900, radius = 3.5f, center = center)
                        drawText(textMeasurer, "O", Offset(center.x - 14f, center.y + 4f), style = labelStyle)

                        val rVal = params["r"] ?: "R"
                        drawText(textMeasurer, "R = $rVal", Offset(center.x + radius * 0.4f, center.y - 18f), style = TextStyle(color = Rose600, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    }

                    else -> {
                        // Default general polygon/geometric shape
                        val p1 = Offset(w * 0.20f, h * 0.80f)
                        val p2 = Offset(w * 0.80f, h * 0.80f)
                        val p3 = Offset(w * 0.65f, h * 0.20f)
                        val p4 = Offset(w * 0.35f, h * 0.20f)

                        val path = Path().apply {
                            moveTo(p1.x, p1.y)
                            lineTo(p2.x, p2.y)
                            lineTo(p3.x, p3.y)
                            lineTo(p4.x, p4.y)
                            close()
                        }
                        drawPath(path, color = Color(0x103B82F6), style = Fill)
                        drawPath(path, color = primaryColor, style = Stroke(width = strokeWidth))
                    }
                }
            }
        }
    }
}

private fun drawParallelArrow(scope: DrawScope, point: Offset, strokeWidth: Float) {
    val arrowSize = 10f
    val path = Path().apply {
        moveTo(point.x - arrowSize, point.y - arrowSize * 0.6f)
        lineTo(point.x, point.y)
        lineTo(point.x - arrowSize, point.y + arrowSize * 0.6f)
    }
    scope.drawPath(path, color = Amber600, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun parseDiagramSpec(spec: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val parts = spec.split(";")
    for (part in parts) {
        val kv = part.split("=", limit = 2)
        if (kv.size == 2) {
            map[kv[0].trim()] = kv[1].trim()
        } else if (kv.size == 1 && kv[0].startsWith("TYPE:")) {
            map["TYPE"] = kv[0].substringAfter("TYPE:").trim()
        }
    }
    return map
}
