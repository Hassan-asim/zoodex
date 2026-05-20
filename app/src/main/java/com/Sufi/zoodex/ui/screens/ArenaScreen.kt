package com.Sufi.zoodex.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.Beast
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.data.OperativeProfile
import com.Sufi.zoodex.data.SupabaseService
import com.Sufi.zoodex.util.IconUtils
import com.Sufi.zoodex.ui.theme.*
import androidx.compose.ui.res.painterResource
import com.Sufi.zoodex.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ArenaPhase { PRE_MATCH, SEARCHING, PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT }

private data class MoveSlot(val name: String, val icon: String, val desc: String, val color: Color)

private val arenaMoveSlots = listOf(
    MoveSlot("CORE SLASH",   "⚔️", "Physical Strike",       Color(0xFF0A84FF)),
    MoveSlot("PLASMA BURST", "💥", "High DMG + Recoil",    Color(0xFFFF9F0A)),
    MoveSlot("SHIELD SHELL", "🛡️", "Low DMG + Self Heal",  Color(0xFF30D158)),
    MoveSlot("TECH REBOOT",  "🔄", "Heal / Overclock",     Color(0xFFBF5AF2))
)

private enum class OpponentMode { AI, FRIEND }

@Composable
fun ArenaScreen(
    navMode: String,
    navFriendEncoded: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    var phase by remember { mutableStateOf(ArenaPhase.PRE_MATCH) }
    var logList by remember { mutableStateOf(listOf(">> INITIALIZING COMBAT INTERFACE...")) }

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
                    try { SupabaseService.fetchFriendsForCallsign(mine) } catch (_: Exception) { emptyList() }
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
    val rewardGold = remember(enemyTeamBeasts.size) { 100 + enemyTeamBeasts.size * 25 }
    val rewardXP = remember(enemyTeamBeasts.size) { 80 + enemyTeamBeasts.size * 20 }

    val toneGen = remember { try { ToneGenerator(AudioManager.STREAM_MUSIC, 85) } catch (_: Throwable) { null } }
    DisposableEffect(Unit) { onDispose { toneGen?.release() } }

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

    // Music simulation
    LaunchedEffect(phase) {
        if (phase != ArenaPhase.PRE_MATCH && phase != ArenaPhase.VICTORY && phase != ArenaPhase.DEFEAT) {
            while (true) {
                toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 40)
                delay(3000)
            }
        }
    }

    fun startSearchingMatch() {
        phase = ArenaPhase.SEARCHING
        logList = listOf(">> SCANNING FOR HOSTILE FREQUENCIES...")
        scope.launch {
            delay(1500)
            val roster = if (opponentMode == OpponentMode.AI) {
                val pool = bossOptions.shuffled()
                List(selectedTeamBeasts.size) { idx ->
                    val pick = pool[idx % pool.size]
                    val lvl = GameState.playerLevel + 1
                    val hp = (90..120).random() + lvl * 15
                    val str = (10..18).random() + lvl
                    val def = (6..14).random() + lvl
                    val spd = (8..16).random() + lvl
                    Beast(900+idx, pick.first, pick.first, lvl, 0, pick.second, str, def, spd, hp, hp)
                }
            } else {
                List(selectedTeamBeasts.size) { idx ->
                    val lvl = GameState.playerLevel + 1
                    val hp = (100..130).random() + lvl * 12
                    val str = (12..20).random() + lvl
                    val def = (8..16).random() + lvl
                    val spd = (10..18).random() + lvl
                    Beast(800+idx, "RIVAL ${friendCallsign.uppercase()}", "RIVAL", lvl, 0, "CYBER", str, def, spd, hp, hp)
                }
            }
            enemyTeamBeasts.clear()
            enemyTeamBeasts.addAll(roster)
            enemyHpList.clear()
            enemyMaxHpList.clear()
            roster.forEach { b -> enemyHpList.add(b.maxHp); enemyMaxHpList.add(b.maxHp) }
            activeEnemyIndex = 0
            logList = logList + ">> HOSTILE SQUAD DETECTED — ${roster.size} UNITS"
            delay(800)
            phase = ArenaPhase.PLAYER_TURN
        }
    }

    fun playerExecuteAttack(skillName: String) {
        if (phase != ArenaPhase.PLAYER_TURN) return
        val currentBeast = currentFightingPlayerBeast.value
        val enemy = enemyTeamBeasts.getOrNull(activeEnemyIndex) ?: return
        val curEnemyHp = enemyHpList[activeEnemyIndex]

        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        scope.launch {
            for (i in 0..5) { enemyShakeX = if (i % 2 == 0) 8f else -8f; delay(40) }
            enemyShakeX = 0f
        }

        val baseDmg = (20 + currentBeast.strength * 1.5).toInt()
        val mitigation = (enemy.defense * 0.9).toInt()
        val dmg = (baseDmg - mitigation).coerceAtLeast(1) + (1..10).random()
        val newEh = (curEnemyHp - dmg).coerceAtLeast(0)
        enemyHpList[activeEnemyIndex] = newEh
        logList = logList + ">> ${currentBeast.name} uses $skillName! Deals $dmg damage."

        if (newEh <= 0) {
            logList = logList + ">> ${enemy.name} eliminated."
            if (activeEnemyIndex + 1 < enemyTeamBeasts.size) {
                activeEnemyIndex++
                phase = ArenaPhase.PLAYER_TURN
            } else {
                phase = ArenaPhase.VICTORY
                logList = logList + ">> ARENA CLEARED. MISSION SUCCESS."
            }
        } else {
            phase = ArenaPhase.ENEMY_TURN
        }
    }

    LaunchedEffect(phase) {
        if (phase == ArenaPhase.ENEMY_TURN) {
            delay(1500)
            val currentBeast = currentFightingPlayerBeast.value
            val enemy = enemyTeamBeasts.getOrNull(activeEnemyIndex) ?: return@LaunchedEffect

            toneGen?.startTone(ToneGenerator.TONE_SUP_RADIO_ACK, 90)
            scope.launch {
                for (i in 0..5) { playerShakeX = if (i % 2 == 0) 8f else -8f; delay(40) }
                playerShakeX = 0f
            }

            val baseDmg = (15 + enemy.strength * 1.3).toInt()
            val mitigation = (currentBeast.defense * 0.9).toInt()
            val dmg = (baseDmg - mitigation).coerceAtLeast(1) + (1..8).random()
            val curPh = playerHpList[activeIndex]
            val newPh = (curPh - dmg).coerceAtLeast(0)
            playerHpList[activeIndex] = newPh
            logList = logList + ">> ${enemy.name} strikes! Deals $dmg damage to ${currentBeast.name}."

            if (newPh <= 0) {
                logList = logList + ">> ${currentBeast.name} offline."
                if (activeIndex + 1 < selectedTeamBeasts.size) {
                    activeIndex++
                    phase = ArenaPhase.PLAYER_TURN
                } else {
                    phase = ArenaPhase.DEFEAT
                    logList = logList + ">> SQUAD WIPED. RETREATING..."
                }
            } else {
                phase = ArenaPhase.PLAYER_TURN
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF05050A), Color(0xFF0F0F1A))))
                val space = 50.dp.toPx()
                for (x in 0..(size.width / space).toInt()) {
                    drawLine(Color.White.copy(0.02f), Offset(x * space, 0f), Offset(x * space, size.height), 1f)
                }
                for (y in 0..(size.height / space).toInt()) {
                    drawLine(Color.White.copy(0.02f), Offset(0f, y * space), Offset(size.width, y * space), 1f)
                }
                drawCircle(AppleBlue.copy(0.03f), size.minDimension / 2.5f, center, style = Stroke(2f))
            }
    ) {


        Column(Modifier.fillMaxSize()) {
            // HUD Header
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                TextButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) { 
                    Text("← ABORT", color = AppleRed, fontWeight = FontWeight.Bold) 
                }
            }

            if (phase == ArenaPhase.PRE_MATCH) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier
                            .width(400.dp)
                            .background(GlassSurface, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SELECT SQUAD DEPLOYMENT", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            (0..2).forEach { idx ->
                                val team = GameState.playerTeams.getOrNull(idx) ?: emptyList()
                                val isValid = team.isNotEmpty() || (idx == 0 && GameState.capturedBeasts.isNotEmpty())
                                Button(
                                    onClick = {
                                        val ids = if (idx == 0 && team.isEmpty()) GameState.capturedBeasts.take(3).map { it.id } else team
                                        val beasts = ids.mapNotNull { id -> GameState.capturedBeasts.find { it.id == id } }
                                        selectedTeamBeasts.clear()
                                        selectedTeamBeasts.addAll(beasts)
                                        playerHpList.clear()
                                        playerMaxHpList.clear()
                                        beasts.forEach { b -> playerHpList.add(b.maxHp); playerMaxHpList.add(b.maxHp) }
                                        activeIndex = 0
                                        startSearchingMatch()
                                    },
                                    enabled = isValid,
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) { Text("TEAM ${idx+1}") }
                            }
                        }
                    }
                }
            } else if (phase == ArenaPhase.SEARCHING) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppleBlue)
                }
            } else {
                // Battle View
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    // Player Side
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        val pB = currentFightingPlayerBeast.value
                        HpBar(pB.name, playerHpList.getOrNull(activeIndex) ?: 0, playerMaxHpList.getOrNull(activeIndex) ?: 1, AppleBlue)
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .size(90.dp)
                                .graphicsLayer(translationY = playerFloatY, translationX = playerShakeX),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(IconUtils.getAnimalIcon(pB.name), fontSize = 64.sp)
                        }
                    }

                    // Enemy Side
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        val eB = currentEnemyBeast.value
                        HpBar(eB.name, enemyHpList.getOrNull(activeEnemyIndex) ?: 0, enemyMaxHpList.getOrNull(activeEnemyIndex) ?: 1, AppleRed)
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .size(90.dp)
                                .graphicsLayer(translationY = enemyFloatY, translationX = enemyShakeX),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(IconUtils.getAnimalIcon(eB.name), fontSize = 64.sp)
                        }
                    }
                }

                // Bottom Console
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color.Black.copy(0.4f))
                        .padding(8.dp)
                ) {
                    if (phase == ArenaPhase.PLAYER_TURN) {
                        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            arenaMoveSlots.forEach { slot ->
                                MoveBtn(slot, Modifier.weight(1f)) { playerExecuteAttack(slot.name) }
                            }
                        }
                    } else if (phase == ArenaPhase.VICTORY || phase == ArenaPhase.DEFEAT) {
                        Button(
                            onClick = {
                                if (phase == ArenaPhase.VICTORY) GameState.addXPAndGold(context, rewardXP, rewardGold)
                                onBack()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(if (phase == ArenaPhase.VICTORY) "CLAIM REWARDS" else "RETREAT")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HpBar(name: String, hp: Int, max: Int, color: Color) {
    val fraction = if (max > 0) (hp.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
    Column(Modifier.width(160.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 10.sp)
            Text("$hp/$max", color = color, fontSize = 10.sp)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(0.1f), CircleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun RowScope.MoveBtn(move: MoveSlot, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        color = move.color.copy(0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, move.color.copy(0.4f))
    ) {
        Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(move.icon, fontSize = 16.sp)
            Text(move.name, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = move.color)
        }
    }
}
