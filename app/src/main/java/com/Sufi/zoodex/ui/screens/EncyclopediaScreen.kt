package com.Sufi.zoodex.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.Beast
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.data.AnimalDatabase
import com.Sufi.zoodex.ui.theme.*

private val elementColors = mapOf(
    "ELECTR" to Color(0xFFFFDD00),
    "VOID" to Color(0xFFBF5AF2),
    "FIRE" to Color(0xFFFF5522),
    "CYBER" to Color(0xFF30D158),
    "WATER" to Color(0xFF00B4D8),
    "EARTH" to Color(0xFF8B4513)
)

@Composable
fun EncyclopediaScreen(onBack: () -> Unit, onBeastDetail: (Int) -> Unit) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf("ALL") }
    var elementFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "CAPTURED", "LOCKED")
    val elementFilters = listOf("ALL", "ELECTR", "VOID", "FIRE", "CYBER", "WATER", "EARTH")

    // Sync GameState on load
    LaunchedEffect(Unit) {
        GameState.init(context)
    }

    // Use new comprehensive animal database
    val allAnimals = AnimalDatabase.allAnimals
    val capturedAnimalIds = GameState.capturedBeasts.map { it.id }

    // Match all animals to filter
    val displayedItems = remember(capturedAnimalIds.size, filter, elementFilter) {
        allAnimals.filter { animal ->
            val isCaptured = capturedAnimalIds.contains(animal.id)
            val elementMatch = elementFilter == "ALL" || animal.elementType == elementFilter
            
            elementMatch && when (filter) {
                "CAPTURED" -> isCaptured
                "LOCKED" -> !isCaptured
                else -> true
            }
        }
    }

    Column(Modifier.fillMaxSize().background(ObsidianBlack)) {
        // Header
        Box(
            Modifier
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "ENCYCLOPEDIA", 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = TextPrimary, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    val capturedCount = capturedAnimalIds.size
                    Text(
                        "UNLOCKED: $capturedCount/${allAnimals.size}", 
                        style = MaterialTheme.typography.bodyMedium, 
                        fontSize = 11.sp, 
                        color = CyberLime,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }

        // Filter tabs for capture status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF07070B))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { f ->
                val selected = filter == f
                Box(
                    modifier = Modifier
                        .background(if (selected) AppleBlue else GlassSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, if (selected) AppleBlue else Color.White.copy(0.08f), RoundedCornerShape(8.dp))
                        .clickable { filter = f }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = f, 
                        style = MaterialTheme.typography.bodyMedium, 
                        fontSize = 11.sp,
                        color = if (selected) ObsidianBlack else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Element filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF07070B))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            elementFilters.forEach { elem ->
                val selected = elementFilter == elem
                val elemColor = elementColors[elem] ?: TextSecondary
                Box(
                    modifier = Modifier
                        .background(if (selected) elemColor.copy(0.2f) else Color.White.copy(0.04f), RoundedCornerShape(6.dp))
                        .border(1.dp, if (selected) elemColor else Color.White.copy(0.08f), RoundedCornerShape(6.dp))
                        .clickable { elementFilter = elem }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = elem, 
                        style = MaterialTheme.typography.bodySmall, 
                        fontSize = 9.sp,
                        color = if (selected) elemColor else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Grid of all animals
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(displayedItems) { animal ->
                val isCaptured = capturedAnimalIds.contains(animal.id)
                val elemColor = elementColors[animal.elementType] ?: AppleBlue
                
                AnimalEncyclopediaCard(
                    animal = animal,
                    isCaptured = isCaptured,
                    elemColor = elemColor,
                    onClick = { onBeastDetail(animal.id) }
                )
            }
        }
    }
}

@Composable
fun AnimalEncyclopediaCard(
    animal: com.Sufi.zoodex.data.AnimalData,
    isCaptured: Boolean,
    elemColor: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardscale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(0.85f)
                .background(GlassSurface, RoundedCornerShape(12.dp))
                .border(
                    1.dp, 
                    if (isCaptured) elemColor.copy(alpha = 0.6f) else Color.White.copy(0.08f), 
                    RoundedCornerShape(12.dp)
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                // Animal Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(elemColor.copy(0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, elemColor.copy(0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(animal.iconUrl, fontSize = 20.sp)
                }

                Spacer(Modifier.height(6.dp))

                // Animal Name
                Text(
                    text = animal.name, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontSize = 9.sp,
                    color = if (isCaptured) elemColor else TextSecondary, 
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 10.sp
                )

                Spacer(Modifier.height(4.dp))

                // Stats preview
                if (isCaptured) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(elemColor.copy(0.1f), RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatMini("HP", animal.baseHp)
                        VerticalDivider(
                            modifier = Modifier.height(12.dp),
                            thickness = 0.5.dp,
                            color = elemColor.copy(0.3f)
                        )
                        StatMini("ATK", animal.baseAttack)
                        VerticalDivider(
                            modifier = Modifier.height(12.dp),
                            thickness = 0.5.dp,
                            color = elemColor.copy(0.3f)
                        )
                        StatMini("DEF", animal.baseDefense)
                    }
                } else {
                    Text(
                        "🔒 LOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Element Type Badge
                Box(
                    modifier = Modifier
                        .background(elemColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, elemColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = animal.elementType, 
                        fontSize = 7.sp, 
                        color = elemColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatMini(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 6.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.toString(),
            fontSize = 7.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedCardWrapper(
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
    ) {
        content()
    }
}
