package com.Sufi.zoodex.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.Context
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.Beast
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ArenaPhase { PRE_MATCH, SEARCHING, PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT }

@Composable
fun ArenaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Programmatically Lock Screen Orientation to Landscape Fixed
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    var phase by remember { mutableStateOf(ArenaPhase.PRE_MATCH) }
    var logList by remember { mutableStateOf(listOf(">> CHOOSE A SQUAD TO DEPLOY IN SECTOR...")) }

    // Dynamic Battle Setup based on persistent active roster
    LaunchedEffect(Unit) {
        GameState.init(context)
    }

    // Party-based health trackers
    val selectedTeamBeasts = remember { mutableStateListOf<Beast>() }
    val playerHpList = remember { mutableStateListOf<Int>() }
    val playerMaxHpList = remember { mutableStateListOf<Int>() }
    var activeIndex by remember { mutableIntStateOf(0) }

    // Get current fighting player beast
    val currentFightingPlayerBeast = remember {
        derivedStateOf { selectedTeamBeasts.getOrNull(activeIndex) ?: Beast(99, "VOLT HOUND", "VOLT HOUND", 1, 0, "ELECTR", 14, 8, 18, 120, 120, 100, 100, 0, true) }
    }

    // Dynamic Spawn Boss enemy based on player level
    val bossOptions = listOf(
        Triple("GLITCH SPECTER", "CYBER", "👾"),
        Triple("NEON TIGER", "CYBER", "🐅"),
        Triple("SOLAR HAWK", "FIRE", "🦅"),
        Triple("VOID FLYER", "VOID", "🦇")
    )
    val bossInfo = remember { bossOptions.random() }
    val isTerritoryBattle = remember { GameState.activeTerritoryBattle != null }
    val enemyBeast = remember {
        val bossLvl = GameState.playerLevel + 1
        val baseHp = 110 + (bossLvl * 15)
        val name = if (isTerritoryBattle) "RIVAL ${GameState.activeTerritoryBattle!!.callsign.uppercase()}" else bossInfo.first
        Beast(
            id = 999,
            name = name,
            nickname = name,
            level = bossLvl,
            xp = 0,
            elementType = bossInfo.second,
            strength = 12 + (bossLvl * 2),
            defense = 8 + bossLvl,
            agility = 10 + bossLvl,
            maxHp = baseHp,
            currentHp = baseHp
        )
    }

    var enemyHp by remember { mutableIntStateOf(enemyBeast.currentHp) }

    // Rewards bounds
    val rewardGold = remember { if (isTerritoryBattle) 150 else 100 + (enemyBeast.level * 10) }
    val rewardXP = remember { if (isTerritoryBattle) 250 else 80 + (enemyBeast.level * 8) }

    // Dynamic Shake/Flash hit offsets
    var playerShakeX by remember { mutableStateOf(0f) }
    var enemyShakeX by remember { mutableStateOf(0f) }

    // Idle hover floating animation loop
    val floatAnim = rememberInfiniteTransition(label = "hover")
    val playerFloatY by floatAnim.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "p_float"
    )
    val enemyFloatY by floatAnim.animateFloat(
        initialValue = 4f, targetValue = -4f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "e_float"
    )

    // Matchmaking sequence triggers after squad choice
    fun startSearchingMatch() {
        phase = ArenaPhase.SEARCHING
        logList = listOf(">> SYNCHRONIZING ARENA BEACON...")
        scope.launch {
            delay(1500)
            logList = logList + ">> SECTOR BEACON IDENTIFIED: RIVAL BOSS [${enemyBeast.name}]"
            delay(1000)
            logList = logList + ">> ENGAGING TARGET STAGES..."
            delay(800)
            phase = ArenaPhase.PLAYER_TURN
        }
    }

    // Player Turn Action
    fun playerExecuteAttack(skillName: String) {
        if (phase != ArenaPhase.PLAYER_TURN) return
        val currentBeast = currentFightingPlayerBeast.value

        // Trigger physical hit shake animation on target (enemy)
        scope.launch {
            for (i in 0..5) {
                enemyShakeX = if (i % 2 == 0) 10f else -10f
                delay(40)
            }
            enemyShakeX = 0f
        }

        // Damage formula scales on active beast's strength
        val baseDmg = 25 + (currentBeast.strength * 1.2).toInt()
        val dmg = (baseDmg - (enemyBeast.defense / 2)).coerceAtLeast(15) + (1..10).random()

        enemyHp = (enemyHp - dmg).coerceAtLeast(0)
        logList = logList + ">> [Slot #${activeIndex + 1}] ${currentBeast.name} uses $skillName! Deals $dmg damage."

        if (enemyHp <= 0) {
            phase = ArenaPhase.VICTORY
            logList = logList + ">> ${enemyBeast.name} core collapsed! VICTORY CONFIRMED."
            return
        }
        phase = ArenaPhase.ENEMY_TURN
    }

    // Turn Swap / Enemy Strike Execution
    LaunchedEffect(phase) {
        if (phase == ArenaPhase.ENEMY_TURN) {
            delay(1400)
            val currentBeast = currentFightingPlayerBeast.value

            // Trigger shake animation on target (player)
            scope.launch {
                for (i in 0..5) {
                    playerShakeX = if (i % 2 == 0) 10f else -10f
                    delay(40)
                }
                playerShakeX = 0f
            }

            val baseDmg = 22 + (enemyBeast.strength * 1.1).toInt()
            val dmg = (baseDmg - (currentBeast.defense / 2)).coerceAtLeast(12) + (1..8).random()

            // Apply damage to current active beast
            val currentHp = playerHpList.getOrElse(activeIndex) { currentBeast.maxHp }
            val newHp = (currentHp - dmg).coerceAtLeast(0)

            if (activeIndex < playerHpList.size) {
                playerHpList[activeIndex] = newHp
            }

            logList = logList + ">> ${enemyBeast.name} strikes ${currentBeast.name}! Deals $dmg damage."

            if (newHp <= 0) {
                logList = logList + ">> [Slot #${activeIndex + 1}] ${currentBeast.name} core fainted!"

                // Swap-in next squad member if available
                if (activeIndex + 1 < selectedTeamBeasts.size) {
                    activeIndex++
                    val nextBeast = selectedTeamBeasts[activeIndex]
                    logList = logList + ">> Swapping active lines! Deploying [Slot #${activeIndex + 1}] ${nextBeast.name}!"
                    phase = ArenaPhase.PLAYER_TURN
                } else {
                    phase = ArenaPhase.DEFEAT
                    logList = logList + ">> All active squad fainted. DEFEAT RECORDED."
                }
            } else {
                phase = ArenaPhase.PLAYER_TURN
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Landscape Fixed Top HUD Header Bar
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(0.4f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White.copy(0.04f), RoundedCornerShape(8.dp)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text("← Retreat", style = MaterialTheme.typography.labelMedium, color = AppleRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Text(
                "TACTICAL COMBAT ARENA",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )

            val statusColor = when (phase) {
                ArenaPhase.PLAYER_TURN -> AppleGreen
                ArenaPhase.ENEMY_TURN -> AppleRed
                ArenaPhase.VICTORY -> AppleGreen
                ArenaPhase.DEFEAT -> AppleRed
                else -> TextSecondary
            }

            Box(
                modifier = Modifier
                    .background(statusColor.copy(0.12f), RoundedCornerShape(6.dp))
                    .border(0.5.dp, statusColor.copy(0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = when (phase) {
                        ArenaPhase.PRE_MATCH -> "PRE-MATCH"
                        ArenaPhase.SEARCHING -> "CONNECTING"
                        ArenaPhase.PLAYER_TURN -> "YOUR TURN"
                        ArenaPhase.ENEMY_TURN -> "ENEMY TURN"
                        ArenaPhase.VICTORY -> "VICTORY"
                        ArenaPhase.DEFEAT -> "DEFEAT"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontSize = 9.sp
                )
            }
        }

        if (phase == ArenaPhase.PRE_MATCH) {
            // High-fidelity Squad Selection Dialog
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(420.dp)
                        .background(GlassSurface, RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        "CHOOSE A FIGHTING DEPLOYMENT TEAM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Pick from your 5 custom tactical squads configured in Teams",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (0..4).forEach { idx ->
                            val teamIds = GameState.playerTeams.getOrNull(idx) ?: listOf()
                            
                            // Fallback for Team 1: If empty, but the player has captured beasts, automatically default to the first 3!
                            val finalTeamIds = if (idx == 0 && teamIds.isEmpty() && GameState.capturedBeasts.isNotEmpty()) {
                                GameState.capturedBeasts.take(3).map { it.id }
                            } else {
                                teamIds
                            }
                            
                            val isTeamValid = finalTeamIds.isNotEmpty()

                            Button(
                                onClick = {
                                    if (isTeamValid) {
                                        val beasts = finalTeamIds.mapNotNull { id -> GameState.capturedBeasts.find { it.id == id } }
                                        selectedTeamBeasts.clear()
                                        selectedTeamBeasts.addAll(beasts)
                                        playerHpList.clear()
                                        playerMaxHpList.clear()
                                        beasts.forEach { beast ->
                                            playerHpList.add(beast.maxHp)
                                            playerMaxHpList.add(beast.maxHp)
                                        }
                                        activeIndex = 0
                                        startSearchingMatch()
                                    }
                                },
                                enabled = isTeamValid,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTeamValid) Color.White.copy(0.05f) else Color.White.copy(0.01f),
                                    contentColor = if (isTeamValid) AppleBlue else TextTertiary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .border(
                                        1.dp,
                                        if (isTeamValid) AppleBlue.copy(0.3f) else Color.White.copy(0.02f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "TEAM ${idx + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isTeamValid) TextPrimary else TextTertiary
                                    )
                                    Text(
                                        "${finalTeamIds.size} Units",
                                        fontSize = 8.sp,
                                        color = if (isTeamValid) AppleGreen else TextTertiary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "* Go configure your squads in the SQUAD TEAMS section of Command Hub.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 8.sp
                    )
                }
            }
        } else if (phase == ArenaPhase.SEARCHING) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppleBlue, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "ESTABLISHING SIGNAL COMBAT LINK...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppleBlue,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Split-Screen fixed Landscape battle layout
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Left Pod (Player Side)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer(
                                translationX = playerShakeX,
                                translationY = playerFloatY
                            )
                    ) {
                        val activeBeast = currentFightingPlayerBeast.value

                        // Glow base pedestal
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(CyberBlueStart.copy(0.2f), Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ElementVectorGraphic(
                                elementType = activeBeast.elementType,
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        // Detailed HP card Pokemon-style
                        GlassCard(
                            modifier = Modifier.width(180.dp),
                            border = BorderStroke(1.dp, AppleBlue.copy(0.2f))
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        activeBeast.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "LVL ${activeBeast.level}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = AppleBlue
                                    )
                                }

                                val hp = playerHpList.getOrNull(activeIndex) ?: 100
                                val max = playerMaxHpList.getOrNull(activeIndex) ?: 100
                                val ratio = (hp.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                                val hpBarCol = when {
                                    ratio > 0.5f -> AppleGreen
                                    ratio > 0.2f -> AppleOrange
                                    else -> AppleRed
                                }

                                Spacer(Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    // Pokemon-style squad dots
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        selectedTeamBeasts.forEachIndexed { i, _ ->
                                            val currentHp = playerHpList.getOrNull(i) ?: 0
                                            val dotCol = if (currentHp <= 0) AppleRed else AppleGreen
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(dotCol)
                                            )
                                        }
                                    }
                                    Text("$hp/$max HP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = hpBarCol)
                                }
                                Spacer(Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.White.copy(0.08f), RoundedCornerShape(2.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(ratio)
                                            .fillMaxHeight()
                                            .background(hpBarCol, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Right Pod (Enemy Side)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer(
                                translationX = enemyShakeX,
                                translationY = enemyFloatY
                            )
                    ) {
                        // Glow base pedestal
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(CyberBlueEnd.copy(0.2f), Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ElementVectorGraphic(
                                elementType = enemyBeast.elementType,
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        // Detailed HP Card
                        GlassCard(
                            modifier = Modifier.width(180.dp),
                            border = BorderStroke(1.dp, AppleRed.copy(0.2f))
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        enemyBeast.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "LVL ${enemyBeast.level}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = AppleRed
                                    )
                                }

                                val ratio = (enemyHp.toFloat() / enemyBeast.maxHp.toFloat()).coerceIn(0f, 1f)
                                val hpBarCol = when {
                                    ratio > 0.5f -> AppleGreen
                                    ratio > 0.2f -> AppleOrange
                                    else -> AppleRed
                                }

                                Spacer(Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("BOSS UNIT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AppleRed)
                                    Text("$enemyHp/${enemyBeast.maxHp} HP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = hpBarCol)
                                }
                                Spacer(Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.White.copy(0.08f), RoundedCornerShape(2.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(ratio)
                                            .fillMaxHeight()
                                            .background(hpBarCol, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Landscape Bottom Splitscreen Controller Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color.Black.copy(0.3f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Terminal scrolling combat logs
                GlassCard(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(4.dp)
                    ) {
                        logList.takeLast(5).forEach { line ->
                            val lineCol = when {
                                line.contains("VICTORY") -> AppleGreen
                                line.contains("DEFEAT") -> AppleRed
                                line.contains("Swapping") -> AppleBlue
                                line.contains("strikes") -> AppleOrange
                                else -> TextSecondary
                            }
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = lineCol,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }

                // Right Command menu inputs
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                ) {
                    if (phase == ArenaPhase.PLAYER_TURN) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val moves = listOf("CORE SLASH", "PLASMA BURST", "SHIELD SHELL", "TECH REBOOT")
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                moves.take(2).forEach { move ->
                                    Button(
                                        onClick = { playerExecuteAttack(move) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GlassSurface, contentColor = AppleBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, Color.White.copy(0.08f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text(move, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                moves.takeLast(2).forEach { move ->
                                    Button(
                                        onClick = { playerExecuteAttack(move) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GlassSurface, contentColor = AppleBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, Color.White.copy(0.08f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text(move, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    } else if (phase == ArenaPhase.VICTORY || phase == ArenaPhase.DEFEAT) {
                        val win = phase == ArenaPhase.VICTORY
                        Button(
                            onClick = {
                                if (win) {
                                    GameState.addXPAndGold(context, rewardXP, rewardGold)
                                    if (isTerritoryBattle) {
                                        val battle = GameState.activeTerritoryBattle
                                        if (battle != null) {
                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                try {
                                                    val myCall = GameState.callsign.ifBlank {
                                                        val prefs = context.getSharedPreferences("zoodex_save", Context.MODE_PRIVATE)
                                                        prefs.getString("callsign", "") ?: ""
                                                    }
                                                    com.Sufi.zoodex.data.SupabaseService.saveTerritoryClaim(
                                                        callsign = myCall,
                                                        lat = battle.lat,
                                                        lng = battle.lng,
                                                        radius = battle.radius,
                                                        faction = GameState.faction
                                                    )
                                                } catch (e: Exception) {
                                                    Log.e("ArenaScreen", "Error saving captured territory: ${e.message}")
                                                }
                                            }
                                        }
                                        GameState.activeTerritoryBattle = null
                                    }
                                }
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (win) AppleGreen else AppleRed, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(
                                text = if (win) "Claim Victory: +$rewardGold Coins 🪙" else "Retreat back to Hub",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ObsidianBlack,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GlassSurface, RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RIVAL ACTION INBOUND...",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppleRed,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
