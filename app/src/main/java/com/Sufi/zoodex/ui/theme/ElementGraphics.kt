package com.Sufi.zoodex.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ElementVectorGraphic(
    elementType: String,
    modifier: Modifier = Modifier,
    glowColor: Color? = null
) {
    val activeGlowColor = glowColor ?: CyberBlueStart

    Canvas(
        modifier = modifier.size(64.dp)
    ) {
        val w = size.width
        val h = size.height

        when (elementType.uppercase()) {
            "ELECTR" -> {
                // High-fidelity sharp high-tech lightning bolt polygonal vector path
                val boltPath = Path().apply {
                    moveTo(w * 0.55f, h * 0.05f)
                    lineTo(w * 0.22f, h * 0.55f)
                    lineTo(w * 0.48f, h * 0.55f)
                    lineTo(w * 0.42f, h * 0.95f)
                    lineTo(w * 0.78f, h * 0.45f)
                    lineTo(w * 0.52f, h * 0.45f)
                    close()
                }

                // Inner core highlights
                val corePath = Path().apply {
                    moveTo(w * 0.53f, h * 0.15f)
                    lineTo(w * 0.32f, h * 0.52f)
                    lineTo(w * 0.48f, h * 0.52f)
                    lineTo(w * 0.45f, h * 0.82f)
                    lineTo(w * 0.68f, h * 0.48f)
                    lineTo(w * 0.52f, h * 0.48f)
                    close()
                }

                // Draw outer glowing halo path
                drawPath(
                    path = boltPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(activeGlowColor, CyberBlueEnd)
                    )
                )

                // Draw inner hot lightning core path
                drawPath(
                    path = corePath,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            "FIRE" -> {
                // High-fidelity twin-flame geometric teardrop vector path
                val outerFlame = Path().apply {
                    moveTo(w * 0.5f, h * 0.05f)
                    quadraticBezierTo(w * 0.82f, h * 0.4f, w * 0.82f, h * 0.68f)
                    cubicTo(w * 0.82f, h * 0.95f, w * 0.18f, h * 0.95f, w * 0.18f, h * 0.68f)
                    quadraticBezierTo(w * 0.18f, h * 0.4f, w * 0.5f, h * 0.05f)
                    close()
                }

                val innerFlame = Path().apply {
                    moveTo(w * 0.5f, h * 0.32f)
                    quadraticBezierTo(w * 0.7f, h * 0.55f, w * 0.7f, h * 0.72f)
                    cubicTo(w * 0.7f, h * 0.88f, w * 0.3f, h * 0.88f, w * 0.3f, h * 0.72f)
                    quadraticBezierTo(w * 0.3f, h * 0.55f, w * 0.5f, h * 0.32f)
                    close()
                }

                // Draw outer blazing flame
                drawPath(
                    path = outerFlame,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF9F0A), Color(0xFFFF375F))
                    )
                )

                // Draw glowing hot inner core
                drawPath(
                    path = innerFlame,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFFF9F0A))
                    )
                )
            }

            "VOID" -> {
                // Concentric orbital space rings and core nucleus vector
                // Outer ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyberBlueEnd, CyberBlueStart, CyberBlueEnd)
                    ),
                    radius = w * 0.42f,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Diagonal orbit path
                val orbitPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.35f)
                    cubicTo(w * 0.3f, h * 0.15f, w * 0.7f, h * 0.85f, w * 0.85f, h * 0.65f)
                    cubicTo(w * 0.7f, h * 0.85f, w * 0.3f, h * 0.15f, w * 0.15f, h * 0.35f)
                    close()
                }

                drawPath(
                    path = orbitPath,
                    color = Color.White.copy(0.4f),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Inner core sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, CyberBlueEnd)
                    ),
                    radius = w * 0.2f
                )

                // Micro satellite node
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.28f)
                )
            }

            "CYBER" -> {
                // Hexagonal circuit matrix with grid lines
                val hexPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.08f)
                    lineTo(w * 0.86f, h * 0.28f)
                    lineTo(w * 0.86f, h * 0.72f)
                    lineTo(w * 0.5f, h * 0.92f)
                    lineTo(w * 0.14f, h * 0.72f)
                    lineTo(w * 0.14f, h * 0.28f)
                    close()
                }

                val innerHex = Path().apply {
                    moveTo(w * 0.5f, h * 0.22f)
                    lineTo(w * 0.75f, h * 0.36f)
                    lineTo(w * 0.75f, h * 0.64f)
                    lineTo(w * 0.5f, h * 0.78f)
                    lineTo(w * 0.25f, h * 0.64f)
                    lineTo(w * 0.25f, h * 0.36f)
                    close()
                }

                // Draw outer structural hexagon
                drawPath(
                    path = hexPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF30D158), Color(0xFF00A2FF))
                    ),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw inner solid glowing hexagonal core
                drawPath(
                    path = innerHex,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF30D158).copy(0.2f), Color(0xFF00A2FF).copy(0.4f))
                    )
                )

                // Digital grid crosswires lines
                drawLine(
                    color = Color.White.copy(0.3f),
                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.08f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.92f),
                    strokeWidth = 1.dp.toPx()
                )

                // Central node dot
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
                )
            }

            else -> {
                // Default specimen paw/diamond geometric vector path
                val diamondPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.1f)
                    lineTo(w * 0.85f, h * 0.5f)
                    lineTo(w * 0.5f, h * 0.9f)
                    lineTo(w * 0.15f, h * 0.5f)
                    close()
                }

                drawPath(
                    path = diamondPath,
                    brush = CyberGradient
                )
            }
        }
    }
}
