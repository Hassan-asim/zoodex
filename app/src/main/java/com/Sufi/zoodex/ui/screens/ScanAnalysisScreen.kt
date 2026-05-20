package com.Sufi.zoodex.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.data.AnimalDatabase
import com.Sufi.zoodex.data.AnimalScanDetector
import com.Sufi.zoodex.util.IconUtils
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close

@Composable
fun ScanAnalysisScreen(
    capturedBitmap: Bitmap?,
    onBack: () -> Unit,
    onAnimalsAnalyzed: (List<Int>) -> Unit
) {
    val context = LocalContext.current
    var analyzedAnimals by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(true) }
    var confidence by remember { mutableFloatStateOf(0f) }
    var analysisMessage by remember { mutableStateOf("Initializing on-device vision model...") }
    var detectedLabel by remember { mutableStateOf("") }
    val telemetryLogs = remember { mutableStateListOf<String>() }

    LaunchedEffect(capturedBitmap) {
        if (capturedBitmap == null) return@LaunchedEffect
        isAnalyzing = true
        telemetryLogs.clear()
        
        val logs = listOf(
            ">> INITIALIZING BIO-SIGNATURE AI NEURAL MATRIX...",
            ">> ML KIT OBJECT TRACKER ENGINE DEPLOYED...",
            ">> ISOLATING TARGET REGIONS WITHIN VIEWPORT...",
            ">> EXTRACTING DEEP CONVOLUTIONAL FEATURE TENSORS...",
            ">> VECTOR CONFIDENCE DISTRIBUTIONS LOADED...",
            ">> GENERATING AFFINITY VECTOR CORRELATION MATRIX...",
            ">> COMPARATIVE DNA ENCYCLOPEDIA REGISTRY ALIGNMENT...",
            ">> RESOLVING HIGH-DIMENSIONAL ELEMENT SPECTRUM AFFINITY..."
        )
        
        for (logLine in logs) {
            telemetryLogs.add(logLine)
            confidence = (telemetryLogs.size.toFloat() / (logs.size + 2).toFloat()) * 0.82f
            analysisMessage = logLine.substringAfter(">> ")
            delay(280)
        }
        
        try {
            val (ids, headline, isMatchFound) = withContext(Dispatchers.Default) {
                AnimalScanDetector.analyzeStrict(capturedBitmap)
            }
            analyzedAnimals = ids
            detectedLabel = headline
            confidence = if (isMatchFound) 0.98f else 0.65f
            
            if (isMatchFound) {
                telemetryLogs.add(">> CLASSIFICATION SUCCESS: BIO-MATCH DETECTED!")
                telemetryLogs.add(">> TOP SIGNAL: ${headline.uppercase()} — ELEMENT VECTORS CORRELATED.")
                analysisMessage = "Bio-match confirmed: $headline"
            } else {
                telemetryLogs.add(">> CLASSIFICATION REJECTED: UNKNOWN SPECIMEN.")
                telemetryLogs.add(">> SIGNAL DETECTED: ${headline.uppercase()} — NO ENCYCLOPEDIA ENTRY FOUND.")
                analysisMessage = "UNKNOWN BIO-SIGNAL: $headline"
            }
        } catch (e: Exception) {
            Log.e("ScanAnalysisScreen", "Real scan failed: ${e.message}", e)
            analyzedAnimals = emptyList()
            detectedLabel = "SCAN_ERROR"
            confidence = 0.2f
            telemetryLogs.add(">> ERROR: Bio-scan processing failure.")
            analysisMessage = "Bio-scan failed to initialize properly."
        } finally {
            isAnalyzing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassSurface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = AppleBlue)
                ) {
                    Text("< BACK", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "AI ANALYSIS", 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = TextPrimary, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.weight(1f))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Captured Image Preview
                if (capturedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(GlassSurface, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = capturedBitmap.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item {
                // Analysis Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = analysisMessage,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confidence Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color.White.copy(0.1f), RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(confidence.coerceIn(0f, 1f))
                                    .background(
                                        when {
                                            confidence < 0.5f -> AppleOrange
                                            confidence < 0.8f -> Color(0xFFFFD700)
                                            else -> Color(0xFF30D158)
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(confidence * 100).toInt()}% CONFIDENCE",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Premium Sci-Fi Cybernetic scrolling telemetry log console
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                telemetryLogs.forEach { log ->
                                    val isHighlight = log.contains("SUCCESS") || log.contains("TOP SIGNAL")
                                    val logCol = when {
                                        log.contains("SUCCESS") -> AppleGreen
                                        log.contains("ERROR") -> AppleRed
                                        isHighlight -> AppleBlue
                                        else -> TextSecondary
                                    }
                                    Text(
                                        text = log,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = logCol,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!isAnalyzing && analyzedAnimals.isNotEmpty()) {
                item {
                    Text(
                        text = "🎯 MATCHES FOUND IN ENCYCLOPEDIA",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(analyzedAnimals) { animalId ->
                    val animal = AnimalDatabase.getAnimalById(animalId)
                    if (animal != null) {
                        AnimalMatchCard(
                            animal = animal,
                            onCapture = {
                                GameState.init(context)
                                GameState.captureBeast(context, animal.name, animal.elementType)
                                onAnimalsAnalyzed(listOf(animalId))
                                onBack()
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onBack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "DECLINE & RETAKE",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (isAnalyzing) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = CyberLime,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun AnimalMatchCard(animal: com.Sufi.zoodex.data.AnimalData, onCapture: () -> Unit) {
    val elementColor = when (animal.elementType) {
        "ELECTR" -> Color(0xFFFFDD00)
        "VOID" -> Color(0xFFBF5AF2)
        "FIRE" -> Color(0xFFFF5522)
        "CYBER" -> Color(0xFF30D158)
        "WATER" -> Color(0xFF00B4D8)
        "EARTH" -> Color(0xFF8B4513)
        else -> AppleBlue
    }

    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassSurface, RoundedCornerShape(12.dp))
            .border(1.dp, elementColor.copy(0.3f), RoundedCornerShape(12.dp))
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Animal Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(elementColor.copy(0.2f), CircleShape)
                        .border(2.dp, elementColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = IconUtils.getAnimalIcon(animal.name),
                        fontSize = 28.sp
                    )
                }

                // Animal Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = animal.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${animal.encyclopediaClass} | ${animal.elementType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = elementColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                // Match Score Badge
                Box(
                    modifier = Modifier
                        .background(elementColor.copy(0.3f), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ MATCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = elementColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = HairlineDivider, thickness = 1.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = animal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox("HP", animal.baseHp, Modifier.weight(1f))
                    StatBox("ATK", animal.baseAttack, Modifier.weight(1f))
                    StatBox("DEF", animal.baseDefense, Modifier.weight(1f))
                    StatBox("SPD", animal.baseSpeed, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCapture,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = elementColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "✓ CAPTURE THIS ANIMAL",
                        color = ObsidianBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White.copy(0.05f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
