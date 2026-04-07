package com.takipsanplus.rfidtablet.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Layered ambient background for executive-facing tablet screens.
 */
@Composable
fun PremiumScreenBackdrop(modifier: Modifier = Modifier) {
    val drift = rememberInfiniteTransition(label = "backdropDrift")
    val shift by drift.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    val topLeft = Color(0xFFE8F2FF)
    val mid = Color(0xFFF5F9FF)
    val bottom = Color(0xFFEEF4FB)
    val accent = Color(0xFF7DB8FF).copy(alpha = 0.12f + shift * 0.06f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(topLeft, mid, bottom),
                    start = Offset(0f, 0f),
                    end = Offset(1200f * (0.85f + shift * 0.15f), 900f)
                )
            )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent, Color.Transparent),
                        center = Offset(280f + shift * 180f, 120f + shift * 60f),
                        radius = 520f
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF4A90D9).copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(900f - shift * 100f, 500f),
                        radius = 640f
                    )
                )
        )
        // Soft vignette — depth without darkening the whole UI
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color(0x00000000),
                            0.55f to Color(0x00000000),
                            1f to Color(0x080A1628)
                        )
                    )
                )
        )
    }
}
