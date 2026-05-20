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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class OperationItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

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
                    // Profile Header Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(factionColor.copy(0.08f), RoundedCornerShape(16.dp))
                            .border(1.dp, factionColor.copy(0.2f), RoundedCornerShape(16.dp))
                            .clickable { showEditProfileDialog = true }
                            .padding(20.dp)
                    ) {
                        Column {
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
                                    text = "⚙️ EDIT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppleBlue,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "METADATA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    val sidebarStats = listOf(
                        "Level" to "${GameState.playerLevel}",
                        "Gold" to "${GameState.playerGold}",
                        "Beasts" to "$totalBeasts / 17",
                        "Sectors" to "$conqueredSectors"
                    )

                    sidebarStats.forEach { (label, value) ->
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
                        Text(
                            text = "DARK MODE",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Switch(
                            checked = localDarkTheme,
                            onCheckedChange = { isChecked ->
                                GameState.isDarkTheme = isChecked
                                localDarkTheme = isChecked
                                GameState.save(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = factionColor,
                                checkedTrackColor = factionColor.copy(0.3f)
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
                        Text("BACK TO RADAR")
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(0.04f))
                            .clickable { scope.launch { drawerState.open() } },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ZOODEX",
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = activeCallsign,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

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

            // Progress Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RANK L${GameState.playerLevel}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${GameState.playerXP} / ${xpRequired} XP",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppleBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
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
                                .background(brush = CyberGradient, shape = RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("COINS", "${GameState.playerGold}", CyberBlueStart, Modifier.weight(1f))
                StatCard("TEAM", "$activeTeamSize / 3", CyberBlueStart, Modifier.weight(1f))
                StatCard("SPECIES", "$totalBeasts", CyberBlueStart, Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "OPERATIONS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            )

            val operations = listOf(
                OperationItem("SCAN", "AREA", Icons.Filled.QrCodeScanner, CyberBlueStart, "scanner"),
                OperationItem("MAP", "WORLD", Icons.Filled.Map, CyberBlueStart, "map"),
                OperationItem("SHOP", "FACTION", Icons.Filled.ShoppingCart, CyberBlueStart, "shop"),
                OperationItem("DEX", "BEASTS", Icons.Filled.Book, CyberBlueStart, "encyclopedia"),
                OperationItem("SQUAD", "TEAMS", Icons.Filled.Groups, CyberBlueStart, "teams"),
                OperationItem("ARENA", "COMBAT", Icons.Filled.SportsEsports, CyberBlueStart, "arena/ai/NONE"),
                OperationItem("COMMS", "LINK", Icons.Filled.Wifi, CyberBlueStart, "comms")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OperationCard(operations[0], Modifier.weight(1f)) { onNavigate(operations[0].route) }
                    OperationCard(operations[1], Modifier.weight(1f)) { onNavigate(operations[1].route) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OperationCard(operations[3], Modifier.weight(1f)) { onNavigate(operations[3].route) }
                    OperationCard(operations[5], Modifier.weight(1f)) { onNavigate(operations[5].route) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OperationCard(operations[2], Modifier.weight(1f)) { onNavigate(operations[2].route) }
                    OperationCard(operations[4], Modifier.weight(1f)) { onNavigate(operations[4].route) }
                    OperationCard(operations[6], Modifier.weight(1f)) { onNavigate(operations[6].route) }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showEditProfileDialog) {
        var tempAvatar by remember { mutableStateOf(GameState.playerAvatar) }
        var tempFaction by remember { mutableStateOf(activeFaction) }
        val factions = listOf(
            Triple("NEON_SYNDICATE", NeonCyan, "Cyber Forest"),
            Triple("VOID_RUNNERS", NeonViolet, "Void Slag"),
            Triple("IRON_VANGUARD", NeonRed, "Volcanic Slag")
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("RE-CONFIGURE", fontWeight = FontWeight.ExtraBold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val avatars = listOf("🦊", "🐯", "🦅", "🐉", "🐺", "🦁", "🐼", "🦄")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        avatars.forEach { avatar ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (tempAvatar == avatar) AppleBlue.copy(0.15f) else GlassSurface)
                                    .border(1.dp, if (tempAvatar == avatar) AppleBlue else Color.White.copy(0.08f), CircleShape)
                                    .clickable { tempAvatar = avatar },
                                contentAlignment = Alignment.Center
                            ) { Text(avatar, fontSize = 16.sp) }
                        }
                    }
                    Column {
                        factions.forEach { (name, color, label) ->
                            Surface(
                                onClick = { tempFaction = name },
                                shape = RoundedCornerShape(10.dp),
                                color = if (tempFaction == name) color.copy(0.08f) else GlassSurface,
                                border = BorderStroke(1.dp, if (tempFaction == name) color else Color.White.copy(0.06f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = name.replace("_", " "), fontWeight = FontWeight.Bold, color = if (tempFaction == name) color else TextPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    GameState.setProfile(context, activeCallsign, tempFaction, tempAvatar)
                    showEditProfileDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)) { Text("SAVE", fontWeight = FontWeight.Bold) }
            },
            containerColor = ObsidianBlack,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun StatCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
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
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = accentColor, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun OperationCard(item: OperationItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp).clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        color = GlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(36.dp).background(item.color.copy(0.1f), CircleShape).border(1.dp, item.color.copy(0.3f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = item.icon, contentDescription = item.title, tint = TextPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(text = item.title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = item.color, letterSpacing = 1.sp)
            Text(text = item.subtitle, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        }
    }
}
