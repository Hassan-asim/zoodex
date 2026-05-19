package com.Sufi.zoodex.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.Beast
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.ui.theme.*

private val elementMetaData = mapOf(
    "ELECTR" to Pair("⚡", Color(0xFFFFDD00)),
    "VOID" to Pair("🔮", Color(0xFFBF5AF2)),
    "FIRE" to Pair("🔥", Color(0xFFFF5522)),
    "CYBER" to Pair("💾", Color(0xFF30D158))
)

@Composable
fun BeastDetailScreen(beastId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    
    // Find the beast in persistent state
    val beastState = remember(GameState.capturedBeasts.size) {
        derivedStateOf { GameState.capturedBeasts.find { it.id == beastId } }
    }

    val beast = beastState.value ?: return

    val (emoji, elementColor) = elementMetaData[beast.elementType] ?: Pair("❓", TextSecondary)
    
    // Local dynamic trigger to refresh Compose layouts on stat spend
    var updateTrigger by remember { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
    ) {
        // Core Shared Premium Header
        ScreenHeader(title = "BEAST DOSSIER", onBack = onBack)

        Column(Modifier.padding(20.dp)) {
            
            // Ultra-Premium Beast Avatar Glass Block
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.2.dp, elementColor.copy(0.3f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Big Animated Avatar emoji
                    Text(
                        text = emoji,
                        fontSize = 72.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Beast Custom Nickname / Name
                    Text(
                        text = beast.name,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Element badge
                        Box(
                            modifier = Modifier
                                .background(elementColor.copy(0.12f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, elementColor.copy(0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${beast.elementType} TYPE",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = elementColor
                            )
                        }

                        // Level badge
                        Box(
                            modifier = Modifier
                                .background(AppleOrange.copy(0.12f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, AppleOrange.copy(0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LEVEL ${beast.level}",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleOrange
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Stat Allocation Notification Card
            if (GameState.statPointsAvailable > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NeonViolet.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ ${GameState.statPointsAvailable} UNALLOCATED STAT POINTS AVAILABLE\nTap + next to base parameters to reinforce this specimen.",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonViolet,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Base Parameters list title
            Text(
                text = "SPECIMEN BASE PARAMETERS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic upgrade stat grid rows
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                val hasAvailable = GameState.statPointsAvailable > 0
                
                StatUpgradeRow(
                    label = "MAX HEALTH (HP)",
                    value = beast.maxHp,
                    maxValue = 400,
                    fillColor = AppleGreen,
                    canUpgrade = hasAvailable,
                    onUpgrade = {
                        GameState.allocateStat(context, beast.id, "HP")
                        updateTrigger++
                    }
                )
                
                Divider(color = Color.White.copy(0.06f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                StatUpgradeRow(
                    label = "ATTACK / STRENGTH",
                    value = beast.strength,
                    maxValue = 100,
                    fillColor = AppleRed,
                    canUpgrade = hasAvailable,
                    onUpgrade = {
                        GameState.allocateStat(context, beast.id, "STRENGTH")
                        updateTrigger++
                    }
                )

                Divider(color = Color.White.copy(0.06f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                StatUpgradeRow(
                    label = "DEFENSE / BARRIER",
                    value = beast.defense,
                    maxValue = 100,
                    fillColor = AppleBlue,
                    canUpgrade = hasAvailable,
                    onUpgrade = {
                        GameState.allocateStat(context, beast.id, "DEFENSE")
                        updateTrigger++
                    }
                )

                Divider(color = Color.White.copy(0.06f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                StatUpgradeRow(
                    label = "AGILITY / SPEED",
                    value = beast.agility,
                    maxValue = 100,
                    fillColor = AppleOrange,
                    canUpgrade = hasAvailable,
                    onUpgrade = {
                        GameState.allocateStat(context, beast.id, "AGILITY")
                        updateTrigger++
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            // Roster Management Section
            Text(
                text = "COMBAT DIVISION SYSTEM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val activeTeamCount = GameState.capturedBeasts.count { it.inActiveTeam }
            val inActiveTeam = beast.inActiveTeam

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        GameState.toggleRoster(context, beast.id)
                        updateTrigger++
                    },
                color = if (inActiveTeam) AppleGreen.copy(0.08f) else GlassSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    1.dp,
                    if (inActiveTeam) AppleGreen.copy(0.4f) else Color.White.copy(0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (inActiveTeam) "★ REMOVE FROM BATTLE TEAM" else "✚ ASSIGN TO BATTLE TEAM",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (inActiveTeam) AppleGreen else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (inActiveTeam) "Specimen is deployed in combat zones." else "Deploy specimen to fighting rosters ($activeTeamCount/3 filled).",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = inActiveTeam,
                        onCheckedChange = {
                            GameState.toggleRoster(context, beast.id)
                            updateTrigger++
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = AppleGreen,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color.White.copy(0.08f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatUpgradeRow(
    label: String,
    value: Int,
    maxValue: Int,
    fillColor: Color,
    canUpgrade: Boolean,
    onUpgrade: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Proportional horizontal linear slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(fillColor, RoundedCornerShape(3.dp))
                    )
                }
            }

            // Stat Value number
            Text(
                text = "$value",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = fillColor,
                modifier = Modifier.width(36.dp)
            )

            // Upgrade action button
            if (canUpgrade) {
                Surface(
                    onClick = onUpgrade,
                    shape = RoundedCornerShape(8.dp),
                    color = fillColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, fillColor.copy(alpha = 0.4f)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+", 
                            color = fillColor, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 15.sp,
                            modifier = Modifier.offset(y = (-1).dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(28.dp))
            }
        }
    }
}
