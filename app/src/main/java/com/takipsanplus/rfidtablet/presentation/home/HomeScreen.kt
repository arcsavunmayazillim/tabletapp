package com.takipsanplus.rfidtablet.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.PremiumScreenBackdrop
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.theme.AccentCyan
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveInk
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveMuted
import com.takipsanplus.rfidtablet.presentation.theme.GlassStroke
import com.takipsanplus.rfidtablet.presentation.theme.GlassWhite
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue
import kotlinx.coroutines.delay

private val ModuleCardHeight = 188.dp

@Composable
fun HomeScreen(
    language: AppLanguage,
    selectedDeviceName: String,
    onCountingClick: () -> Unit,
    onShipmentClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var entranceStep by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(40)
        entranceStep = 1
        delay(100)
        entranceStep = 2
        delay(100)
        entranceStep = 3
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        PremiumScreenBackdrop(Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = entranceStep >= 1,
                enter = fadeIn(tween(400)) + slideInVertically(
                    tween(450),
                    initialOffsetY = { -it / 8 }
                )
            ) {
                HomeHeroHeader(language = language, selectedDeviceName = selectedDeviceName)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val gradients = listOf(
                listOf(Color(0xFF1557C8), Color(0xFF3B82F6), Color(0xFF60A5FA)),
                listOf(Color(0xFF0F766E), Color(0xFF14B8A6), Color(0xFF5EEAD4)),
                listOf(Color(0xFFB45309), Color(0xFFF97316), Color(0xFFFBBF24))
            )

            val modules = listOf(
                Triple(
                    Icons.Default.Inventory2,
                    localizedString(R.string.module_counting, language),
                    localizedString(R.string.module_counting_subtitle, language)
                ) to onCountingClick,
                Triple(
                    Icons.Default.LocalShipping,
                    localizedString(R.string.module_shipment, language),
                    localizedString(R.string.module_shipment_subtitle, language)
                ) to onShipmentClick,
                Triple(
                    Icons.Default.Settings,
                    localizedString(R.string.module_settings, language),
                    localizedString(R.string.module_settings_subtitle, language)
                ) to onSettingsClick
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                modules.forEachIndexed { index, (triple, onClick) ->
                    val (icon, title, subtitle) = triple
                    val visible = entranceStep > index
                    androidx.compose.animation.AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(380, delayMillis = index * 80)) +
                            scaleIn(
                                initialScale = 0.88f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) +
                            slideInVertically(
                                tween(380, delayMillis = index * 80),
                                initialOffsetY = { it / 12 }
                            ),
                        modifier = Modifier.weight(1f)
                    ) {
                        PremiumModuleCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ModuleCardHeight),
                            indexLabel = String.format("%02d", index + 1),
                            icon = icon,
                            title = title,
                            subtitle = subtitle,
                            gradientColors = gradients[index],
                            onClick = onClick,
                            shimmerPhase = index * 0.33f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeHeroHeader(
    language: AppLanguage,
    selectedDeviceName: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(listOf(PrimaryBlue, AccentCyan))
                    )
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = localizedString(R.string.home_title, language),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    lineHeight = 32.sp
                ),
                color = ExecutiveInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .background(GlassWhite)
                .border(1.dp, GlassStroke, RoundedCornerShape(100.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(AccentCyan)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "${localizedString(R.string.selected_device_label, language)} · ",
                    style = MaterialTheme.typography.bodySmall,
                    color = ExecutiveMuted
                )
                Text(
                    text = selectedDeviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PremiumModuleCard(
    modifier: Modifier,
    indexLabel: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    shimmerPhase: Float
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "press"
    )

    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val sheen by shimmer.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    val base = gradientColors[0]
    val mid = gradientColors.getOrElse(1) { gradientColors[0] }
    val light = gradientColors.getOrElse(2) { Color.White }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (pressed) 6.dp else 14.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = base.copy(alpha = 0.55f),
                ambientColor = Color(0x40000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            base,
                            mid,
                            light.copy(alpha = 0.95f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(420f, 380f)
                    )
                )
        )

        // Glossy top highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.White.copy(alpha = 0.28f),
                            0.35f to Color.White.copy(alpha = 0.06f),
                            1f to Color.Transparent
                        )
                    )
                )
        )

        // Moving sheen (subtle)
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val w = size.width
                    val travel = (sheen + shimmerPhase) % 1f
                    val x0 = w * (travel * 1.4f - 0.35f)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            start = Offset(x0, 0f),
                            end = Offset(x0 + w * 0.35f, size.height)
                        ),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                }
        )

        // Inner border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = indexLabel,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light),
                    color = Color.White.copy(alpha = 0.45f)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, CircleShape, ambientColor = Color(0x33000000))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.White.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
