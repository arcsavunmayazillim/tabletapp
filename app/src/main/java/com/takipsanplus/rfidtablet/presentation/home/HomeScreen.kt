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
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalConfiguration
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
import java.util.Locale

private val ModuleCardHeight = 188.dp

@Composable
fun HomeScreen(
    language: AppLanguage,
    selectedDeviceName: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onCountingClick: () -> Unit,
    onShipmentClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val isWide = LocalConfiguration.current.smallestScreenWidthDp >= 600
    var entranceStep by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(40)
        entranceStep = 1
        delay(100)
        entranceStep = 2
        delay(100)
        entranceStep = 3
    }

    val gradients = remember {
        listOf(
            listOf(Color(0xFF1557C8), Color(0xFF3B82F6), Color(0xFF60A5FA)),
            listOf(Color(0xFF0F766E), Color(0xFF14B8A6), Color(0xFF5EEAD4)),
            listOf(Color(0xFFB45309), Color(0xFFF97316), Color(0xFFFBBF24))
        )
    }

    val countingTitle = localizedString(R.string.module_counting, language)
    val countingSubtitle = localizedString(R.string.module_counting_subtitle, language)
    val shipmentTitle = localizedString(R.string.module_shipment, language)
    val shipmentSubtitle = localizedString(R.string.module_shipment_subtitle, language)
    val settingsTitle = localizedString(R.string.module_settings, language)
    val settingsSubtitle = localizedString(R.string.module_settings_subtitle, language)

    val modules = remember(
        language,
        countingTitle,
        countingSubtitle,
        shipmentTitle,
        shipmentSubtitle,
        settingsTitle,
        settingsSubtitle
    ) {
        listOf(
            Triple(Icons.Default.Inventory2, countingTitle, countingSubtitle) to onCountingClick,
            Triple(Icons.Default.LocalShipping, shipmentTitle, shipmentSubtitle) to onShipmentClick,
            Triple(Icons.Default.Settings, settingsTitle, settingsSubtitle) to onSettingsClick
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PremiumScreenBackdrop(Modifier.fillMaxSize())

        // Background Pattern
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                id = if (isWide) R.drawable.bg_pat2 else R.drawable.bg_pat
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = if (isWide) 0.35f else 0.45f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isWide) 18.dp else 12.dp,
                    vertical = 12.dp
                ),
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(
                visible = entranceStep >= 1,
                enter = fadeIn(tween(400)) + slideInVertically(
                    tween(450),
                    initialOffsetY = { -it / 8 }
                )
            ) {
                HomeHeroHeader(
                    language = language,
                    selectedDeviceName = selectedDeviceName,
                    onBack = onBack,
                    onLogout = onLogout
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    modules.forEachIndexed { index, (triple, onClick) ->
                        val (icon, title, subtitle) = triple
                        val visible = entranceStep > index
                        AnimatedVisibility(
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
                                indexLabel = String.format(Locale.getDefault(), "%02d", index + 1),
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
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    modules.forEachIndexed { index, (triple, onClick) ->
                        val (icon, title, subtitle) = triple
                        val visible = entranceStep > index
                        AnimatedVisibility(
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
                                )
                        ) {
                            PremiumModuleCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                indexLabel = String.format(Locale.getDefault(), "%02d", index + 1),
                                isWide = false,
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
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeHeroHeader(
    language: AppLanguage,
    selectedDeviceName: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ExecutiveInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
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

            IconButton(
                onClick = onLogout,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                    fontWeight = FontWeight.Bold,
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
    shimmerPhase: Float,
    isWide: Boolean = true
) {
    val titleStyle = if (isWide) {
        MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
    }

    val subtitleStyle = if (isWide) {
        MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp)
    } else {
        MaterialTheme.typography.bodyLarge.copy(
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }

    val labelStyle = if (isWide) {
        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light)
    } else {
        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light)
    }

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
                    style = labelStyle,
                    color = Color.White.copy(alpha = 0.45f)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
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
                    style = titleStyle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = subtitleStyle,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

