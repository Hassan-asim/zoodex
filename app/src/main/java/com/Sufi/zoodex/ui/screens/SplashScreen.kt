package com.Sufi.zoodex.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    
    // Core Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "CorePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    // Smooth transition sequence
    LaunchedEffect(Unit) {
        delay(300)
        while (loadingProgress < 1f) {
            delay(35)
            loadingProgress += 0.015f
        }
        delay(600)
        onSplashComplete()
    }

    val loadingMessage = when {
        loadingProgress < 0.2f -> "SYNCHRONIZING FACTION CORE DATABASES..."
        loadingProgress < 0.45f -> "MAPPING REGIONAL COORDINATES & GPS BUFFERS..."
        loadingProgress < 0.7f -> "ENGAGING ON-DEVICE NEURAL RECOGNITION..."
        loadingProgress < 0.92f -> "DECRYPTING TAMED BEAST ENCRYPTIONS..."
        else -> "INTERFACE ACTIVE. WELCOME OPERATIVE."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Glowing Core Orb (Canvas-drawn Apple style logo)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Outer Ring
                    drawCircle(
                        color = AppleBlue.copy(alpha = 0.12f),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        color = AppleBlue.copy(alpha = 0.6f),
                        radius = size.minDimension / 2.3f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Inner glowing core
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AppleBlue, Color.Transparent),
                            radius = size.minDimension / 3.5f
                        ),
                        radius = size.minDimension / 3.5f
                    )
                    // Core target ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = size.minDimension / 8f,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Premium Sleek Title
            Text(
                text = "ZOODEX",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Advanced Operative Interface",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Thin Premium Apple-Style Loading Indicator
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(loadingProgress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AppleBlue, NeonViolet)
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "[ ${loadingMessage} ]",
                style = MaterialTheme.typography.labelMedium,
                fontSize = 9.sp,
                color = AppleBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }

        // All Rights Reserved & Version Footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "© 2026 ZOODEX INC. ALL RIGHTS RESERVED.",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "SYSTEM ENGINE v1.0.26 // CORE COMPILATION 2026 VERSION 1.0",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.sp,
                    color = TextSecondary.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
