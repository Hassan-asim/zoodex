package com.Sufi.zoodex.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CommandHubScreen(
    callsign: String,
    faction: String,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var localDarkTheme by remember { mutableStateOf(GameState.isDarkTheme) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Initialize/sync persistent game state
    LaunchedEffect(Unit) {
        GameState.init(context)
    }

    val activeFaction = GameState.faction.ifBlank { faction }
    val activeCallsign = GameState.callsign.ifBlank { callsign }

    val factionColor = when (activeFaction) {
        "NEON_SYNDICATE" -> NeonCyan
        "VOID_RUNNERS" -> NeonViolet
        else -> NeonRed
    }

    val totalBeasts = GameState.capturedBeasts.size
    val activeTeamSize = GameState.capturedBeasts.count { it.inActiveTeam }
    val conqueredSectors = GameState.sectors.count { it.faction == activeFaction }
    
    // Level XP progress calculation
    val xpRequired = GameState.playerLevel * 120
    val xpProgress = GameState.playerXP.toFloat() / xpRequired.toFloat()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ObsidianBlack.copy(0.98f),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .border(1.dp, Color.White.copy(0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Profile Header Card (Clickable to Edit Faction/Avatar!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(factionColor.copy(0.08f), RoundedCornerShape(16.dp))
                            .border(1.dp, factionColor.copy(0.2f), RoundedCornerShape(16.dp))
                            .clickable { showEditProfileDialog = true }
                            .padding(20.dp)
                    ) {
                        Column {
                            // Avatar Placeholder/Icon
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(factionColor.copy(0.15f), CircleShape)
                                    .border(2.dp, factionColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = GameState.playerAvatar,
                                    fontSize = 32.sp
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = activeCallsign.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeFaction.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = factionColor
                                )
                                Text(
                                    text = "⚙️ EDIT PROFILE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppleBlue,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Player Stats List
                    Text(
                        text = "OPERATIVE METADATA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    val stats = listOf(
                        "Rank Level" to "LEVEL ${GameState.playerLevel}",
                        "Active Faction Gold" to "${GameState.playerGold} COINS",
                        "Secured Specimen Size" to "$totalBeasts / 17 SPECIES",
                        "Conquered Outposts" to "$conqueredSectors SEC"
                    )

                    stats.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Theme Configurer Row
                    HorizontalDivider(color = Color.White.copy(0.08f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.03f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DARK THEME MODE",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (localDarkTheme) "Energy Efficient" else "High Contrast Light",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = localDarkTheme,
                            onCheckedChange = { isChecked ->
                                GameState.isDarkTheme = isChecked
                                localDarkTheme = isChecked
                                GameState.save(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = factionColor,
                                checkedTrackColor = factionColor.copy(0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Gray.copy(0.3f)
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(0.05f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CLOSE RADAR SYSTEM")
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Menu Hamburger Icon Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(0.04f))
                            .clickable { scope.launch { drawerState.open() } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☰",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ZOODEX",
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Operative: $activeCallsign",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

            // Faction Pill Badge
            Box(
                modifier = Modifier
                    .background(factionColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, factionColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = activeFaction.replace("_", " "),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = factionColor
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Level Progression Glass Card
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "OPERATIVE PROGRESSION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Level ${GameState.playerLevel}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                // XP Display
                Text(
                    text = "${GameState.playerXP} / ${xpRequired} XP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppleBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(xpProgress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(
                            brush = CyberGradient,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Status Stats Grid Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gold
            StatCard(
                label = "CAPITAL COINS",
                value = "${GameState.playerGold}",
                accentColor = CyberBlueStart,
                modifier = Modifier.weight(1f)
            )

            // Active Team
            StatCard(
                label = "ACTIVE TEAM",
                value = "$activeTeamSize / 3",
                accentColor = CyberBlueStart,
                modifier = Modifier.weight(1f)
            )

            // Beast Count
            StatCard(
                label = "BEAST DICTION",
                value = "$totalBeasts",
                accentColor = CyberBlueStart,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "FIELD OPERATIONS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        // Navigation Menu
        val operations = listOf(
            Triple("SCAN AREA", "AR Capture view & real camera lens", CyberBlueStart) to "scanner",
            Triple("TERRITORY CONQUEST", "Claim sectors for $faction", CyberBlueStart) to "map",
            Triple("FACTION SHOP", "Strategic core purchase outpost", CyberBlueStart) to "shop",
            Triple("ENCYCLOPEDIA", "Beast dossier & stat allocation", CyberBlueStart) to "encyclopedia",
            Triple("SQUAD TEAMS", "Manage up to 5 tactical strike teams", CyberBlueStart) to "teams",
            Triple("COMBAT ARENA", "Active team turn-based boss battles", CyberBlueStart) to "arena",
            Triple("COMMS CHANNEL", "Encrypted global & alliance chat", CyberBlueStart) to "comms"
        )

        operations.forEach { (info, route) ->
            val (title, subtitle, color) = info
            NavCard(
                title = title,
                subtitle = subtitle,
                color = color,
                onClick = { onNavigate(route) }
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(32.dp))
    }
}

    if (showEditProfileDialog) {
        var tempAvatar by remember { mutableStateOf(GameState.playerAvatar) }
        var tempFaction by remember { mutableStateOf(activeFaction) }

        val factions = listOf(
            Triple("NEON_SYNDICATE", NeonCyan, "Cybernetic Forest Division"),
            Triple("VOID_RUNNERS", NeonViolet, "Void Slag Anomaly Division"),
            Triple("IRON_VANGUARD", NeonRed, "Volcanic Slag Power Division")
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "RE-CONFIGURE PROFILE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "CALLSIGN: ${activeCallsign.uppercase()} [READ-ONLY]",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Column {
                        Text(
                            text = "SELECT OPERATIVE AVATAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val avatars = listOf("🦊", "🐯", "🦅", "🐉", "🐺", "🦁", "🐼", "🦄")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            avatars.forEach { avatar ->
                                val isSelected = tempAvatar == avatar
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) AppleBlue.copy(0.15f) else GlassSurface)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) AppleBlue else Color.White.copy(0.08f),
                                            CircleShape
                                        )
                                        .clickable { tempAvatar = avatar },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(avatar, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "SELECT DIVISION ALLIANCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        factions.forEach { (name, color, label) ->
                            val isSelected = tempFaction == name
                            Surface(
                                onClick = { tempFaction = name },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color.copy(0.08f) else GlassSurface,
                                border = BorderStroke(1.dp, if (isSelected) color else Color.White.copy(0.06f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = name.replace("_", " "),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) color else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        GameState.setProfile(context, activeCallsign, tempFaction, tempAvatar)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = ObsidianBlack),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditProfileDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CANCEL")
                }
            },
            containerColor = ObsidianBlack,
            shape = RoundedCornerShape(16.dp)
        )
    }
}


@Composable
fun StatCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = GlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun NavCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = GlassSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Premium iOS style arrow indicator
            Text(
                text = "→",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
