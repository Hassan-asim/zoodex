package com.Sufi.zoodex.ui.screens

import android.graphics.Bitmap
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
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke

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
    var analysisMessage by remember { mutableStateOf("Initializing Qwen 0.5B AI Model...") }
    var detectedLabel by remember { mutableStateOf("") }

    // Simulate the AI analysis progress
    fun simulateAIAnalysis(): List<Int> {
        // In production, this would use on-device TFLite + Qwen model
        // For now, we simulate by returning random animals from the database
        
        // Typical flow: image -> ML Kit Vision API -> Detect class -> Map to Zoodex animals
        val possibleClasses = listOf(
            "CANINE", "FELINE", "AVIAN", "REPTILE", "AQUATIC", 
            "UNGULATE", "PRIMATE", "RODENT", "MUSTELID"
        )
        
        val randomClass = possibleClasses.random()
        val matchedAnimals = AnimalDatabase.getAnimalsByClass(randomClass)
        
        return matchedAnimals.take(3).map { it.id } // Return top 3 matches
    }

    // Simulate AI analysis using local Qwen model
    LaunchedEffect(capturedBitmap) {
        if (capturedBitmap != null) {
            analyzedAnimals = simulateAIAnalysis()
            isAnalyzing = false
        }
    }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            var progress = 0f
            while (progress < 0.95f && isAnalyzing) {
                delay(150)
                progress += kotlin.random.Random.nextDouble(0.1, 0.2).toFloat()
                confidence = progress.coerceAtMost(0.95f)
                
                analysisMessage = when {
                    confidence < 0.3f -> "Scanning image frames..."
                    confidence < 0.6f -> "Analyzing visual features..."
                    confidence < 0.85f -> "Identifying animal characteristics..."
                    else -> "Cross-referencing encyclopedia database..."
                }
                detectedLabel = when {
                    confidence < 0.5f -> "PROCESSING"
                    else -> "IDENTIFIED"
                }
            }
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
                    Text(animal.iconUrl, fontSize = 24.sp)
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

                Divider(color = Color.White.copy(0.1f), thickness = 1.dp)

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
