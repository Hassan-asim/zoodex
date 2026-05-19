package com.Sufi.zoodex.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.data.SupabaseService
import kotlinx.coroutines.launch
import android.util.Log
import com.Sufi.zoodex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstTimeSetupScreen(onSetupComplete: (String, String) -> Unit) {
    var callsign by remember { mutableStateOf("") }
    var selectedFaction by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("🦊") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDeploying by remember { mutableStateOf(false) }

    val factions = listOf(
        Triple("NEON_SYNDICATE", NeonCyan, "Cybernetic bio-hackers controlling digital forest areas."),
        Triple("VOID_RUNNERS", NeonViolet, "Interdimensional outlaws utilizing dark void anomalies."),
        Triple("IRON_VANGUARD", NeonRed, "Heavy tactical legion focused on volcanic slag core power.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Welcome Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "OPERATIVE PROTOCOL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AppleBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Register Callsign",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Claim your faction to synchronize core database.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Onboarding Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Apple Input Field
            OutlinedTextField(
                value = callsign,
                onValueChange = { if (it.length <= 15) callsign = it.uppercase() },
                placeholder = { Text("OPERATIVE CALLSIGN", color = TextTertiary, fontWeight = FontWeight.SemiBold) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleBlue,
                    unfocusedBorderColor = Color.White.copy(0.08f),
                    focusedContainerColor = GlassSurface,
                    unfocusedContainerColor = GlassSurface,
                    cursorColor = AppleBlue
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SELECT OPERATIVE AVATAR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            val avatars = listOf("🦊", "🐯", "🦅", "🐉", "🐺", "🦁", "🐼", "🦄")
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                avatars.forEach { avatar ->
                    val isSelected = selectedAvatar == avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AppleBlue.copy(0.15f) else GlassSurface)
                            .border(
                                1.5.dp, 
                                if (isSelected) AppleBlue else Color.White.copy(0.08f), 
                                CircleShape
                            )
                            .clickable { selectedAvatar = avatar },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatar, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SELECT FACTION DIVISION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            // Factions Glass Cards selection
            factions.forEach { (name, color, desc) ->
                val isSelected = selectedFaction == name
                val animatedBorderColor by animateColorAsState(
                    targetValue = if (isSelected) color else Color.White.copy(0.08f),
                    animationSpec = tween(300),
                    label = "border"
                )
                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) color.copy(alpha = 0.08f) else GlassSurface,
                    animationSpec = tween(300),
                    label = "bg"
                )

                Surface(
                    onClick = { selectedFaction = name },
                    shape = RoundedCornerShape(18.dp),
                    color = animatedBgColor,
                    border = BorderStroke(1.2.dp, animatedBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored Faction Tag indicator
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = name.replace("_", " "),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) color else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Deploy Button
        val isReady = callsign.isNotBlank() && selectedFaction.isNotBlank() && !isDeploying
        Button(
            onClick = {
                if (callsign.isNotBlank() && selectedFaction.isNotBlank() && !isDeploying) {
                    isDeploying = true
                    scope.launch {
                        try {
                            SupabaseService.initializeUserProfile(callsign, selectedFaction)
                        } catch (e: Exception) {
                            Log.e("FirstTimeSetup", "Failed to register: ${e.message}")
                        }
                        GameState.init(context)
                        GameState.setProfile(context, callsign, selectedFaction, selectedAvatar)
                        isDeploying = false
                        onSetupComplete(callsign, selectedFaction)
                    }
                }
            },
            enabled = isReady,
            shape = RoundedCornerShape(28.dp), // Premium capsule style
            colors = ButtonDefaults.buttonColors(
                containerColor = AppleBlue,
                contentColor = ObsidianBlack,
                disabledContainerColor = GlassSurface,
                disabledContentColor = TextSecondary.copy(alpha = 0.4f)
            ),
            border = if (isReady) null else BorderStroke(1.dp, Color.White.copy(0.06f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = if (isDeploying) "Deploying..." else "Deploy Operative",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isReady) ObsidianBlack else TextSecondary.copy(alpha = 0.4f)
            )
        }
    }
}
