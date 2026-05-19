package com.Sufi.zoodex.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Sufi.zoodex.ui.theme.*

/**
 * Reusable top bar header used across screens - Redesigned with premium Apple-style aesthetics.
 */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), // Immersive top status padding
        color = ObsidianBlack,
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(ObsidianBlack)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Back Button
                TextButton(
                    onClick = onBack,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppleBlue
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(Color.White.copy(0.04f), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "← Back", 
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppleBlue
                    )
                }

                // Screen Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Sleek bottom border line
            Divider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 0.5.dp
            )
        }
    }
}

/**
 * Premium Apple-style Glassmorphic Card Container.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    } else {
        modifier
            .clip(RoundedCornerShape(18.dp))
    }

    Surface(
        modifier = cardModifier,
        color = GlassSurface,
        shape = RoundedCornerShape(18.dp),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}
