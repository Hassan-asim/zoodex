package com.Sufi.zoodex.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.Beast
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTeamIndex by remember { mutableIntStateOf(0) }
    
    // Trigger dynamic recomposition on team configuration updates
    var updateTrigger by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        GameState.init(context)
    }

    val activeFactionColor = CyberBlueStart

    val currentTeamIds = remember(selectedTeamIndex, updateTrigger) {
        GameState.playerTeams.getOrNull(selectedTeamIndex) ?: listOf()
    }

    val currentTeamBeasts = remember(currentTeamIds, GameState.capturedBeasts.size) {
        currentTeamIds.mapNotNull { id -> GameState.capturedBeasts.find { it.id == id } }
    }

    val allUnlockedBeasts = remember(GameState.capturedBeasts.size) {
        GameState.capturedBeasts.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Core Screen Header
        ScreenHeader(title = "SQUAD TEAMS DIVISION", onBack = onBack)

        // Apple-style horizontal scrolling team pill selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .horizontalScroll(rememberScrollState())
                .background(GlassSurface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (1..5).forEach { i ->
                val index = i - 1
                val isSelected = selectedTeamIndex == index
                val beastsCount = GameState.playerTeams.getOrNull(index)?.size ?: 0

                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Color.White.copy(0.08f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) Color.White.copy(0.06f) else Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { selectedTeamIndex = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TEAM $i",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "$beastsCount/3 units",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) activeFactionColor else TextTertiary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Team Deployment Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(GlassSurface, RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(0.04f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "ACTIVE SQUAD LINEUP (MAX 3)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (slot in 0 until 3) {
                        val beast = currentTeamBeasts.getOrNull(slot)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.9f)
                                .background(Color.White.copy(0.02f), RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (beast != null) activeFactionColor.copy(0.3f) else Color.White.copy(0.04f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (beast != null) {
                                TeamSlotCard(
                                    beast = beast,
                                    factionColor = activeFactionColor,
                                    onRemove = {
                                        val updatedIds = currentTeamIds.toMutableList()
                                        updatedIds.remove(beast.id)
                                        GameState.saveTeam(context, selectedTeamIndex, updatedIds)
                                        updateTrigger++
                                    }
                                )
                            } else {
                                Text(
                                    text = "EMPTY SLOT",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextTertiary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Specimen Reserve list header
        Text(
            text = "AVAILABLE SPECIMENS IN DICTION",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )

        // Grid of Unlocked Beasts
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allUnlockedBeasts) { beast ->
                val isInTeam = currentTeamIds.contains(beast.id)
                val elementColor = when (beast.elementType.uppercase()) {
                    "ELECTR" -> Color(0xFFFFDD00)
                    "VOID" -> CyberBlueEnd
                    "FIRE" -> Color(0xFFFF5522)
                    "CYBER" -> Color(0xFF30D158)
                    else -> CyberBlueStart
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(0.9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isInTeam) Color.White.copy(0.05f) else GlassSurface)
                        .border(
                            1.dp,
                            if (isInTeam) activeFactionColor else Color.White.copy(0.04f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            val updatedIds = currentTeamIds.toMutableList()
                            if (isInTeam) {
                                updatedIds.remove(beast.id)
                            } else {
                                if (updatedIds.size < 3) {
                                    updatedIds.add(beast.id)
                                }
                            }
                            GameState.saveTeam(context, selectedTeamIndex, updatedIds)
                            updateTrigger++
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ElementVectorGraphic(
                            elementType = beast.elementType,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = beast.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isInTeam) activeFactionColor else TextPrimary,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "LVL ${beast.level} • ${beast.elementType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = elementColor,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamSlotCard(
    beast: Beast,
    factionColor: Color,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onRemove() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ElementVectorGraphic(
                elementType = beast.elementType,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = beast.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppleRed.copy(0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "REMOVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleRed,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
