package com.Sufi.zoodex.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.data.Sector
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedSector by remember { mutableStateOf<Sector?>(null) }
    var conquestState by remember { mutableStateOf("IDLE") } // IDLE, CONQUERING, SUCCESS
    var conquestProgress by remember { mutableFloatStateOf(0f) }

    val nodeOffsets = listOf(
        Pair(45.dp, 80.dp),   // Sector 1
        Pair(220.dp, 120.dp), // Sector 2
        Pair(100.dp, 220.dp), // Sector 3
        Pair(240.dp, 300.dp), // Sector 4
        Pair(60.dp, 360.dp)   // Sector 5
    )

    val walkedPoints = remember { mutableStateListOf<androidx.compose.ui.geometry.Offset>() }
    var isWalkingSimulation by remember { mutableStateOf(false) }
    var perimeterSecured by remember { mutableStateOf(false) }

    // Simulated walk tracker launched loop
    LaunchedEffect(isWalkingSimulation) {
        if (isWalkingSimulation && selectedSector != null) {
            walkedPoints.clear()
            perimeterSecured = false
            val index = GameState.sectors.indexOfFirst { it.id == selectedSector!!.id }
            val (sectorX, sectorY) = nodeOffsets.getOrElse(index) { Pair(100.dp, 100.dp) }
            
            // Scaled central point of simulated sector outpost
            val center = androidx.compose.ui.geometry.Offset(
                x = sectorX.value * 2.5f + 40f,
                y = sectorY.value * 2.5f + 40f
            )
            val radius = 110f
            val stepsCount = 14
            
            for (step in 0..stepsCount) {
                delay(320)
                val angle = (step.toFloat() / stepsCount.toFloat()) * (2f * Math.PI.toFloat())
                val nextPoint = androidx.compose.ui.geometry.Offset(
                    x = center.x + radius * kotlin.math.cos(angle),
                    y = center.y + radius * kotlin.math.sin(angle)
                )
                walkedPoints.add(nextPoint)
            }
            delay(500)
            perimeterSecured = true
            isWalkingSimulation = false
            conquestState = "SUCCESS"
            
            // Execute conquest in dynamic persistence!
            GameState.conquestSector(context, selectedSector!!.id)
            delay(1200)
            
            conquestState = "IDLE"
            selectedSector = GameState.sectors.find { it.id == selectedSector!!.id }
        }
    }

    // Sync GameState
    LaunchedEffect(Unit) {
        GameState.init(context)
    }

    // Conquest simulation loop
    LaunchedEffect(conquestState) {
        if (conquestState == "CONQUERING" && selectedSector != null) {
            while (conquestProgress < 1.0f) {
                delay(40)
                conquestProgress += 0.05f
            }
            conquestState = "SUCCESS"
            delay(1200)
            
            // Execute conquest in dynamic persistence!
            GameState.conquestSector(context, selectedSector!!.id)
            
            // Reset state
            conquestState = "IDLE"
            conquestProgress = 0f
            // Force re-draw by cloning current selection reference
            selectedSector = GameState.sectors.find { it.id == selectedSector!!.id }
        }
    }

    val mapAnim = rememberInfiniteTransition(label = "map_ping")
    val pulseScale by mapAnim.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val pulseAlpha by mapAnim.animateFloat(
        initialValue = 0.9f, targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Shared Screen Header
        ScreenHeader(title = "TERRITORY CONQUEST", onBack = onBack)

        // Subheader showing total owned status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "GPS SECTORS ACTIVE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            
            val ownedSectors = GameState.sectors.count { it.faction == GameState.faction }
            Text(
                text = "$ownedSectors / ${GameState.sectors.size} CONQUERED",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppleBlue
            )
        }

        // Virtual Radar Map Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF07070B))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
        ) {
            
            // Map Grid Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 45.dp.toPx()
                val lineAlpha = 0.03f
                // Vertical lines
                var x = 0f
                while (x < size.width) {
                    drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, size.height), alpha = lineAlpha)
                    x += step
                }
                // Horizontal lines
                var y = 0f
                while (y < size.height) {
                    drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), alpha = lineAlpha)
                    y += step
                }

                // Radar ping ring
                drawCircle(
                    color = AppleBlue.copy(alpha = 0.05f),
                    radius = size.minDimension / 3.5f
                )
                drawCircle(
                    color = AppleBlue.copy(alpha = 0.03f),
                    radius = size.minDimension / 1.8f
                )

                // Draw Walked Perimeter Polyline
                if (walkedPoints.isNotEmpty()) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(walkedPoints.first().x, walkedPoints.first().y)
                        for (i in 1 until walkedPoints.size) {
                            lineTo(walkedPoints[i].x, walkedPoints[i].y)
                        }
                        if (perimeterSecured) {
                            close()
                        }
                    }
                    // Draw filled territory if secured
                    if (perimeterSecured) {
                        drawPath(
                            path = path,
                            color = NeonCyan.copy(0.12f)
                        )
                    }
                    // Draw outer border path
                    drawPath(
                        path = path,
                        color = NeonCyan,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx()
                        )
                    )
                }
            }

            // Radar Scan sweep overlay
            val scanAngle by mapAnim.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
                label = "scan_angle"
            )
            // Visual simulated sweeps inside Canvas
            Box(
                Modifier
                    .size(240.dp)
                    .align(Alignment.Center)
                    .alpha(0.06f)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(Color.Transparent, AppleBlue, Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            GameState.sectors.forEachIndexed { index, sector ->
                val (offsetX, offsetY) = nodeOffsets.getOrElse(index) { Pair(100.dp, 100.dp) }
                val isSelected = selectedSector?.id == sector.id
                
                val nodeColor = when (sector.faction) {
                    "UNCLAIMED" -> TextSecondary
                    "NEON_SYNDICATE" -> NeonCyan
                    "VOID_RUNNERS" -> NeonViolet
                    else -> NeonRed
                }

                // Map Dot Node Interactive Component
                Box(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .clickable {
                            selectedSector = sector
                            conquestState = "IDLE"
                            conquestProgress = 0f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing select indicator ring
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .scale(pulseScale)
                                .alpha(pulseAlpha)
                                .background(nodeColor.copy(0.4f), CircleShape)
                        )
                    }

                    // Inner main solid dot
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(nodeColor, CircleShape)
                            .border(2.5.dp, Color.White, CircleShape)
                    )

                    // Floating name banner above node
                    Box(
                        modifier = Modifier
                            .offset(y = (-20).dp)
                            .background(ObsidianBlack.copy(0.7f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sector.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = nodeColor
                        )
                    }
                }
            }

            // Radar static alert text
            Text(
                text = "✦ AR RADAR ACTIVE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AppleBlue.copy(0.5f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Selected Sector detailed drawer cards panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (selectedSector != null) {
                val sector = selectedSector!!
                val isMyFaction = sector.faction == GameState.faction
                
                val factionColor = when (sector.faction) {
                    "UNCLAIMED" -> TextSecondary
                    "NEON_SYNDICATE" -> NeonCyan
                    "VOID_RUNNERS" -> NeonViolet
                    else -> NeonRed
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (conquestState == "CONQUERING") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = conquestProgress,
                                color = AppleBlue,
                                strokeWidth = 5.dp,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "OVERWRITING TERRITORY CORES...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppleBlue
                            )
                        }
                    } else if (conquestState == "SUCCESS") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "🏆 SECTOR ACQUIRED",
                                fontSize = 34.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "DEployed Faction Node Complete!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppleGreen
                            )
                        }
                    } else {
                        // Standard details state
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = sector.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Threat Rating: ${"✦".repeat(sector.threatLevel)}${"✧".repeat(5 - sector.threatLevel)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleOrange
                                )
                            }

                            // Current Owner Badge
                            Box(
                                modifier = Modifier
                                    .background(factionColor.copy(0.12f), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, factionColor.copy(0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sector.faction.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = factionColor
                                )
                            }
                        }

                        Divider(color = Color.White.copy(0.06f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                        // Rewards row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("XP REWARD", style = MaterialTheme.typography.labelMedium, fontSize = 8.sp, color = TextSecondary)
                                Text("+${sector.rewardXP} XP", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AppleBlue)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("COIN REWARD", style = MaterialTheme.typography.labelMedium, fontSize = 8.sp, color = TextSecondary)
                                Text("+${sector.rewardGold} COINS 🪙", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AppleOrange)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (isMyFaction) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppleGreen.copy(0.12f), RoundedCornerShape(24.dp))
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✔ CONQUERED BY YOUR ALLIANCE DIVISION",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleGreen
                                )
                            }
                        } else {
                            if (isWalkingSimulation) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AppleOrange.copy(0.12f), RoundedCornerShape(24.dp))
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🛰️ SIMULATING GPS WALK PERIMETER [${walkedPoints.size}/15]...",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleOrange
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { isWalkingSimulation = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonCyan,
                                            contentColor = ObsidianBlack
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = "🛰️ GPS WALK",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = ObsidianBlack
                                        )
                                    }

                                    Button(
                                        onClick = { conquestState = "CONQUERING" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppleBlue,
                                            contentColor = ObsidianBlack
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = "⚡ INSTANT",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = ObsidianBlack
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Initial prompt state card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SELECT MAP SECTOR RADAR NODE TO BEGIN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}
