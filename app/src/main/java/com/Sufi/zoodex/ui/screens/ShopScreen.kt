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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.ui.theme.*

@Composable
fun ShopScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var goldBalance by remember { mutableIntStateOf(GameState.playerGold) }
    var boostersCount by remember { mutableIntStateOf(GameState.xpBoostersOwned) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessAlert by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        GameState.init(context)
        goldBalance = GameState.playerGold
        boostersCount = GameState.xpBoostersOwned
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Core Shared Premium Header
        ScreenHeader(title = "FACTION OUTPOST SHOP", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gold currency and inventory balance cards
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DIVISION CAPITAL BALANCE",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🪙 $goldBalance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppleOrange,
                                fontSize = 26.sp
                            )
                            Text(
                                text = " COINS",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp).padding(vertical = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(AppleBlue.copy(0.12f), RoundedCornerShape(12.dp))
                            .border(1.dp, AppleBlue.copy(0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚡ XP POTIONS", style = MaterialTheme.typography.labelMedium, fontSize = 8.sp, color = AppleBlue, fontWeight = FontWeight.Bold)
                            Text("$boostersCount OWNED", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Action feedback banner if trigger occurs
            if (alertMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSuccessAlert) AppleGreen.copy(0.12f) else AppleRed.copy(0.12f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isSuccessAlert) AppleGreen.copy(0.4f) else AppleRed.copy(0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alertMessage!!,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccessAlert) AppleGreen else AppleRed,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSecondary,
                            modifier = Modifier
                                .clickable { alertMessage = null }
                                .padding(start = 12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            Text(
                text = "AVAILABLE OUTPOST TACTICAL SUITE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Shop Product Card 1: XP Booster Potion
            ProductItemCard(
                title = "2X LEVEL XP POTION",
                emoji = "⚡",
                cost = 200,
                description = "Injects +150 XP direct database payload parameters to level up your division instantly!",
                canUse = boostersCount > 0,
                onBuyClick = {
                    val ok = GameState.buyItem(context, "XP_BOOSTER", 200)
                    if (ok) {
                        goldBalance = GameState.playerGold
                        boostersCount = GameState.xpBoostersOwned
                        isSuccessAlert = true
                        alertMessage = "✔ Purchase confirmed: +1 Level XP Potion Matrix acquired!"
                    } else {
                        isSuccessAlert = false
                        alertMessage = "✕ Insufficient capital funds to acquire item."
                    }
                },
                onUseClick = {
                    val ok = GameState.useXPBooster(context)
                    if (ok) {
                        goldBalance = GameState.playerGold
                        boostersCount = GameState.xpBoostersOwned
                        isSuccessAlert = true
                        alertMessage = "⚡ Core booster activated: Injected +150 Division XP!"
                    }
                }
            )

            Spacer(Modifier.height(14.dp))

            // Shop Product Card 2: Nano HP Repair Restore
            ProductItemCard(
                title = "NANO HEALTH REPAIR CORE",
                emoji = "🛠",
                cost = 150,
                description = "Runs automated nano restoration procedures to instantly recover fainted frontline squad lineups back to 100% HP!",
                canUse = false,
                onBuyClick = {
                    val ok = GameState.buyItem(context, "HP_REP", 150)
                    if (ok) {
                        goldBalance = GameState.playerGold
                        isSuccessAlert = true
                        alertMessage = "✔ Nano repairs complete! All fainted squad units fully fuzed to max HP!"
                    } else {
                        isSuccessAlert = false
                        alertMessage = "✕ Insufficient capital funds to initiate repairs."
                    }
                },
                onUseClick = {}
            )

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProductItemCard(
    title: String,
    emoji: String,
    cost: Int,
    description: String,
    canUse: Boolean,
    onBuyClick: () -> Unit,
    onUseClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 34.sp, modifier = Modifier.padding(end = 12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "🪙 COST: $cost COINS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppleOrange,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Buy button
                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppleOrange,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        "ACQUIRE CORE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianBlack
                    )
                }

                // Optional Use button
                if (title.contains("POTION")) {
                    Button(
                        onClick = onUseClick,
                        enabled = canUse,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassSurface,
                            contentColor = AppleBlue,
                            disabledContainerColor = GlassSurface.copy(0.4f),
                            disabledContentColor = TextSecondary.copy(0.3f)
                        ),
                        border = BorderStroke(1.dp, if (canUse) AppleBlue.copy(0.5f) else Color.White.copy(0.04f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            "USE INVENTORY",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
