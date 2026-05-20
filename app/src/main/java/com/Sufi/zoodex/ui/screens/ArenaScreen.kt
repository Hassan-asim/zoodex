package com.Sufi.zoodex.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.Sufi.zoodex.data.OperativeProfile
import com.Sufi.zoodex.data.SupabaseService
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ArenaPhase { PRE_MATCH, SEARCHING, PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT }

private enum class OpponentMode { AI, FRIEND }

@Composable
fun ArenaScreen(
    navMode: String,
    navFriendEncoded: String,
    onBack: () -> Unit
) {
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
    var logList by remember { mutableStateOf(listOf(">> CONFIGURE HOSTILE ENCOUNTER AND SQUAD...")) }

    LaunchedEffect(Unit) {
        GameState.init(context)
    }

    var opponentMode by rememberSaveable { mutableStateOf(OpponentMode.AI) }
    var friendCallsign by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(navMode, navFriendEncoded) {
        opponentMode = if (navMode.equals("friend", ignoreCase = true)) OpponentMode.FRIEND else OpponentMode.AI
        friendCallsign = when {
            navFriendEncoded.isBlank() || navFriendEncoded == "NONE" -> ""
            else -> Uri.decode(navFriendEncoded)
        }
    }

    var friendsLoaded by remember { mutableStateOf<List<OperativeProfile>>(emptyList()) }
    LaunchedEffect(opponentMode, phase) {
        if (opponentMode == OpponentMode.FRIEND && phase == ArenaPhase.PRE_MATCH) {
            val mine = GameState.callsign.ifBlank {
                context.getSharedPreferences("zoodex_save", Context.MODE_PRIVATE).getString("callsign", "") ?: ""
            }
            if (mine.isNotBlank()) {
                friendsLoaded = withContext(Dispatchers.IO) {
                    try {
                        SupabaseService.fetchFriendsForCallsign(mine)
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            }
        }
    }

    val selectedTeamBeasts = remember { mutableStateListOf<Beast>() }
    val playerHpList = remember { mutableStateListOf<Int>() }
    val playerMaxHpList = remember { mutableStateListOf<Int>() }
    var activeIndex by remember { mutableIntStateOf(0) }

    val enemyTeamBeasts = remember { mutableStateListOf<Beast>() }
    val enemyHpList = remember { mutableStateListOf<Int>() }
    val enemyMaxHpList = remember { mutableStateListOf<Int>() }
    var activeEnemyIndex by remember { mutableIntStateOf(0) }

    val currentFightingPlayerBeast = remember {
        derivedStateOf {
            selectedTeamBeasts.getOrNull(activeIndex)
                ?: Beast(99, "VOLT HOUND", "VOLT HOUND", 1, 0, "ELECTR", 14, 8, 18, 120, 120, 100, 100, 0, true)
        }
    }

    val currentEnemyBeast = remember(activeEnemyIndex, enemyTeamBeasts.size) {
        derivedStateOf {
            enemyTeamBeasts.getOrNull(activeEnemyIndex)
                ?: Beast(998, "SCANNING", "SCANNING", 1, 0, "CYBER", 1, 1, 1, 1, 1, 100, 100, 0, false)
        }
    }

    val bossOptions = listOf(
        Triple("GLITCH SPECTER", "CYBER", "👾"),
        Triple("NEON TIGER", "CYBER", "🐅"),
        Triple("SOLAR HAWK", "FIRE", "🦅"),
        Triple("VOID FLYER", "VOID", "🦇")
    )
    val bossInfo = remember { bossOptions.random() }
    val isTerritoryBattle = remember { GameState.activeTerritoryBattle != null }

    val rewardGold = remember(enemyTeamBeasts.size, isTerritoryBattle) {
        val wave = enemyTeamBeasts.size.coerceAtLeast(1)
        if (isTerritoryBattle) 150 + wave * 20 else 100 + wave * 25 + GameState.playerLevel * 8
    }
    val rewardXP = remember(enemyTeamBeasts.size, isTerritoryBattle) {
        val wave = enemyTeamBeasts.size.coerceAtLeast(1)
        if (isTerritoryBattle) 250 + wave * 30 else 80 + wave * 20 + GameState.playerLevel * 6
    }

    val toneGen = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Throwable) {
            null
        }
    }
    DisposableEffect(Unit) {
        onDispose { toneGen?.release() }
    }

    var playerShakeX by remember { mutableStateOf(0f) }
    var enemyShakeX by remember { mutableStateOf(0f) }

    val floatAnim = rememberInfiniteTransition(label = "hover")
    val playerFloatY by floatAnim.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "p_float"
    )
    val enemyFloatY by floatAnim.animateFloat(
        initialValue = 4f, targetValue = -4f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "e_float"
    )

    fun buildEnemyTeam(): List<Beast> {
        val n = selectedTeamBeasts.size
        if (n == 0) return emptyList()
        val lvlBase = GameState.playerLevel + 1
        return if (opponentMode == OpponentMode.AI) {
            val pool = bossOptions.shuffled()
            List(n) { idx ->
                val pick = pool[idx % pool.size]
                val lvl = lvlBase + idx / 2
                val hp = 95 + lvl * 14
                val territory = GameState.activeTerritoryBattle
                val name = if (isTerritoryBattle && idx == 0 && territory != null) {
                    "RIVAL ${territory.callsign.uppercase()}"
                } else {
                    "${pick.first} ${idx + 1}"
                }
                Beast(
                    id = 900 + idx,
                    name = name,
                    nickname = name,
                    level = lvl,
                    xp = 0,
                    elementType = pick.second,
                    strength = 10 + lvl * 2,
                    defense = 6 + lvl,
                    agility = 8 + lvl,
                    maxHp = hp,
                    currentHp = hp
                )
            }
        } else {
            val tag = friendCallsign.ifBlank { "OPERATIVE" }.uppercase()
            val elements = listOf("CYBER", "VOID", "FIRE", "ELECTR")
            List(n) { idx ->
                val lvl = lvlBase + idx
                val hp = 100 + lvl * 12
                Beast(
                    id = 800 + idx,
                    name = "HOSTILE $tag-${idx + 1}",
                    nickname = "HOSTILE $tag-${idx + 1}",
                    level = lvl,
                    xp = 0,
                    elementType = elements[idx % elements.size],
                    strength = 11 + lvl * 2,
                    defense = 7 + lvl,
                    agility = 9 + lvl,
                    maxHp = hp,
                    currentHp = hp
                )
            }
        }
    }

    fun startSearchingMatch() {
        if (opponentMode == OpponentMode.FRIEND && friendCallsign.isBlank()) {
            logList = listOf(">> ENTER OR PICK A FRIEND CALLSIGN BEFORE DEPLOYING.")
            return
        }
        phase = ArenaPhase.SEARCHING
        logList = listOf(">> SYNCHRONIZING ARENA BEACON...")
        scope.launch {
            delay(1200)
            val roster = buildEnemyTeam()
            enemyTeamBeasts.clear()
            enemyTeamBeasts.addAll(roster)
            enemyHpList.clear()
            enemyMaxHpList.clear()
            roster.forEach { b ->
                enemyHpList.add(b.maxHp)
                enemyMaxHpList.add(b.maxHp)
            }
            activeEnemyIndex = 0
            val label = if (opponentMode == OpponentMode.AI) "AI WAVE" else "FRIEND SQUAD"
            logList = logList + ">> LINK ESTABLISHED ($label) — ${roster.size} HOSTILE UNITS"
            delay(900)
            logList = logList + ">> FIRST CONTACT: ${roster.firstOrNull()?.name ?: "UNKNOWN"}"
            delay(500)
            phase = ArenaPhase.PLAYER_TURN
        }
    }

    fun playerExecuteAttack(skillName: String) {
        if (phase != ArenaPhase.PLAYER_TURN) return
        val currentBeast = currentFightingPlayerBeast.value
        val enemy = enemyTeamBeasts.getOrNull(activeEnemyIndex) ?: return
        val eIdx = activeEnemyIndex
        val curEnemyHp = enemyHpList.getOrNull(eIdx) ?: return
        val maxEnemyHp = enemyMaxHpList.getOrNull(eIdx) ?: 1

        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 85)
        scope.launch {
            for (i in 0..5) {
                enemyShakeX = if (i % 2 == 0) 10f else -10f
                delay(40)
            }
            enemyShakeX = 0f
        }

        val baseDmg = 25 + (currentBeast.strength * 1.2).toInt()
        val dmg = (baseDmg - (enemy.defense / 2)).coerceAtLeast(12) + (1..10).random()
        val newEh = (curEnemyHp - dmg).coerceAtLeast(0)
        if (eIdx < enemyHpList.size) enemyHpList[eIdx] = newEh

        logList = logList + ">> [Slot #${activeIndex + 1}] ${currentBeast.name} uses $skillName! Deals $dmg to ${enemy.name}."

        if (newEh <= 0) {
            logList = logList + ">> ${enemy.name} neutralized."
            if (activeEnemyIndex + 1 < enemyTeamBeasts.size) {
                activeEnemyIndex++
                logList = logList + ">> NEXT HOSTILE: ${enemyTeamBeasts[activeEnemyIndex].name}"
                phase = ArenaPhase.PLAYER_TURN
            } else {
                phase = ArenaPhase.VICTORY
                logList = logList + ">> ALL HOSTILE UNITS ELIMINATED. VICTORY CONFIRMED."
            }
            return
        }
        phase = ArenaPhase.ENEMY_TURN
    }

    LaunchedEffect(phase) {
        if (phase == ArenaPhase.ENEMY_TURN) {
            delay(1200)
            val currentBeast = currentFightingPlayerBeast.value
            val enemy = enemyTeamBeasts.getOrNull(activeEnemyIndex) ?: run {
                phase = ArenaPhase.PLAYER_TURN
                return@LaunchedEffect
            }

            toneGen?.startTone(ToneGenerator.TONE_SUP_RADIO_ACK, 95)
            scope.launch {
                for (i in 0..5) {
                    playerShakeX = if (i % 2 == 0) 10f else -10f
                    delay(40)
                }
                playerShakeX = 0f
            }

            val baseDmg = 22 + (enemy.strength * 1.1).toInt()
            val dmg = (baseDmg - (currentBeast.defense / 2)).coerceAtLeast(10) + (1..8).random()

            val currentHp = playerHpList.getOrElse(activeIndex) { currentBeast.maxHp }
            val newHp = (currentHp - dmg).coerceAtLeast(0)

            if (activeIndex < playerHpList.size) {
                playerHpList[activeIndex] = newHp
            }

            logList = logList + ">> ${enemy.name} strikes ${currentBeast.name}! Deals $dmg damage."

            if (newHp <= 0) {
                logList = logList + ">> [Slot #${activeIndex + 1}] ${currentBeast.name} core fainted!"
                GameState.scheduleArenaRecovery(context, currentBeast.id, dmg, currentBeast.maxHp)

                if (activeIndex + 1 < selectedTeamBeasts.size) {
                    activeIndex++
                    val nextBeast = selectedTeamBeasts[activeIndex]
                    logList = logList + ">> Deploying [Slot #${activeIndex + 1}] ${nextBeast.name}!"
                    phase = ArenaPhase.PLAYER_TURN
                } else {
                    phase = ArenaPhase.DEFEAT
                    logList = logList + ">> All squad units down. DEFEAT RECORDED."
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
                        "OPPONENT TYPE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { opponentMode = OpponentMode.AI },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (opponentMode == OpponentMode.AI) AppleBlue else GlassSurface,
                                contentColor = if (opponentMode == OpponentMode.AI) ObsidianBlack else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("VS AI", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { opponentMode = OpponentMode.FRIEND },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (opponentMode == OpponentMode.FRIEND) AppleGreen else GlassSurface,
                                contentColor = if (opponentMode == OpponentMode.FRIEND) ObsidianBlack else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("VS FRIEND", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    if (opponentMode == OpponentMode.FRIEND) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = friendCallsign,
                            onValueChange = { input: String -> friendCallsign = input.uppercase().trim() },
                            label = { Text("Friend callsign", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AppleGreen,
                                unfocusedBorderColor = Color.White.copy(0.12f)
                            )
                        )
                        if (friendsLoaded.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text("Tap ally:", fontSize = 9.sp, color = TextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                friendsLoaded.take(8).forEach { f ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (friendCallsign.equals(f.callsign, true)) AppleGreen.copy(0.25f) else GlassSurface)
                                            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(8.dp))
                                            .clickable { friendCallsign = f.callsign.uppercase() }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            f.callsign,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
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
                            
                            val beasts = finalTeamIds.mapNotNull { id -> GameState.capturedBeasts.find { it.id == id } }
                            val recovering = beasts.any { GameState.isBeastRecovering(it) }
                            val friendOk = opponentMode == OpponentMode.AI || friendCallsign.isNotBlank()
                            val isTeamValid = beasts.isNotEmpty() && !recovering && friendOk

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
                    if (opponentMode == OpponentMode.FRIEND && friendCallsign.isBlank()) {
                        Text(
                            "Enter a friend callsign (or pick from list) to battle their simulated squad.",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppleOrange,
                            fontSize = 9.sp
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        "* Beasts in recovery (after arena faint) cannot deploy. Use Recovery Stim or Nano Repair in the shop.",
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
                                AnimatedHpRatioBar(ratio = ratio, fillColor = hpBarCol)
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
                        val eB = currentEnemyBeast.value
                        val eHp = enemyHpList.getOrNull(activeEnemyIndex) ?: 0
                        val eMax = enemyMaxHpList.getOrNull(activeEnemyIndex) ?: 1

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
                                elementType = eB.elementType,
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
                                        currentEnemyBeast.value.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "LVL ${currentEnemyBeast.value.level}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = AppleRed
                                    )
                                }

                                val ratio = (eHp.toFloat() / eMax.toFloat()).coerceIn(0f, 1f)
                                val hpBarCol = when {
                                    ratio > 0.5f -> AppleGreen
                                    ratio > 0.2f -> AppleOrange
                                    else -> AppleRed
                                }

                                Spacer(Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("HOSTILE ${activeEnemyIndex + 1}/${enemyTeamBeasts.size}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AppleRed)
                                    Text("$eHp/$eMax HP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = hpBarCol)
                                }
                                Spacer(Modifier.height(3.dp))
                                AnimatedHpRatioBar(ratio = ratio, fillColor = hpBarCol)
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

@Composable
private fun AnimatedHpRatioBar(
    ratio: Float,
    fillColor: Color,
    trackColor: Color = Color.White.copy(0.08f)
) {
    val animated by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "hpbar"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(trackColor, RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .background(fillColor, RoundedCornerShape(3.dp))
        )
    }
}
