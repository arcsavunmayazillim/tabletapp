package com.takipsanplus.rfidtablet.presentation.counting

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.PremiumScreenBackdrop
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveInk
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveMuted
import com.takipsanplus.rfidtablet.presentation.theme.GlassStroke
import com.takipsanplus.rfidtablet.presentation.theme.GlassWhite
import com.takipsanplus.rfidtablet.presentation.theme.LiveEmerald
import com.takipsanplus.rfidtablet.presentation.theme.LiveEmeraldSoft
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue
import kotlinx.coroutines.delay

@Composable
fun CountingScreen(
    uiState: CountingUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    onStartStopClicked: () -> Unit,
    onClearClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        PremiumScreenBackdrop(Modifier.fillMaxSize())

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.34f)
                    .fillMaxHeight()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = ExecutiveInk
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localizedString(R.string.counting_title, language),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = ExecutiveInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = localizedString(R.string.counting_subtitle, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExecutiveMuted,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.isReading) {
                    LiveReadingBadge(language = language)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    UniqueCountHero(
                        language = language,
                        count = uiState.uniqueCount,
                        isReading = uiState.isReading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                CountingBottomIconBar(
                    language = language,
                    isReading = uiState.isReading,
                    onStartStopClicked = onStartStopClicked,
                    onClearClicked = onClearClicked
                )
            }

            EpcGlassPanel(
                language = language,
                epcs = uiState.uniqueEpcs,
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun LiveReadingBadge(language: AppLanguage) {
    val transition = rememberInfiniteTransition(label = "live")
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val ringExpand by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(LiveEmeraldSoft)
                .border(1.dp, LiveEmerald.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .scale(0.65f + ringExpand * 0.5f)
                        .clip(CircleShape)
                        .background(LiveEmerald.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(LiveEmerald)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = localizedString(R.string.counting_reading_started, language),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = LiveEmerald,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UniqueCountHero(
    language: AppLanguage,
    count: Int,
    isReading: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(480, easing = FastOutSlowInEasing),
        label = "countTween"
    )
    var bumpTarget by remember { mutableFloatStateOf(1f) }
    var previousCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(count) {
        if (count > previousCount) {
            bumpTarget = 1.12f
            delay(90)
            bumpTarget = 1f
        }
        previousCount = count
    }
    val countScale by animateFloatAsState(
        targetValue = bumpTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "countBump"
    )
    val glow by animateFloatAsState(
        targetValue = if (isReading) 1f else 0.55f,
        animationSpec = tween(400),
        label = "glow"
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = localizedString(R.string.counting_read_count_label, language).uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
            color = ExecutiveMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(countScale)
                .shadow(16.dp, CircleShape, ambientColor = PrimaryBlue.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            PrimaryBlue.copy(alpha = 0.06f + 0.1f * glow),
                            Color(0xFF8B5CF6).copy(alpha = 0.05f + 0.08f * glow)
                        ),
                        center = Offset(100f, 80f),
                        radius = 220f
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.35f + 0.25f * glow),
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFF8B5CF6).copy(alpha = 0.2f),
                            PrimaryBlue.copy(alpha = 0.35f + 0.25f * glow)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = animatedCount.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-1).sp,
                    fontFamily = FontFamily.Monospace
                ),
                fontSize = 56.sp,
                color = ExecutiveInk,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CountingBottomIconBar(
    language: AppLanguage,
    isReading: Boolean,
    onStartStopClicked: () -> Unit,
    onClearClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onStartStopClicked,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isReading) Color(0xFFE11D48) else PrimaryBlue
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 5.dp,
                pressedElevation = 1.dp
            )
        ) {
            Icon(
                imageVector = if (isReading) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = localizedString(
                    if (isReading) R.string.stop_action else R.string.start_action,
                    language
                ),
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        Button(
            onClick = onClearClicked,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF1F5F9),
                contentColor = ExecutiveInk
            ),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(PrimaryBlue.copy(alpha = 0.45f), PrimaryBlue.copy(alpha = 0.2f))
                )
            )
        ) {
            Icon(
                Icons.Filled.DeleteOutline,
                contentDescription = localizedString(R.string.clear_action, language),
                modifier = Modifier.size(28.dp),
                tint = PrimaryBlue
            )
        }
    }
}

@Composable
private fun EpcGlassPanel(
    language: AppLanguage,
    epcs: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(GlassWhite)
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3F6FB))
                .drawBehind {
                    val step = 40.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx() * 0.3f
                        )
                        x += step
                    }
                }
                .padding(8.dp)
        ) {
            if (epcs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.displaySmall,
                            color = ExecutiveMuted.copy(alpha = 0.22f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = localizedString(R.string.counting_empty, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExecutiveMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(epcs, key = { _, e -> e }) { index, epc ->
                        EpcRowCard(epc = epc, index = index + 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun EpcRowCard(epc: String, index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%03d", index),
            style = MaterialTheme.typography.labelSmall,
            color = ExecutiveMuted.copy(alpha = 0.65f),
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = epc,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = ExecutiveInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
