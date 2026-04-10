package com.takipsanplus.rfidtablet.presentation.device

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue

private val DeviceBg = Color(0xFFF3F4F9)
private val DeviceDark = Color(0xFF000842)
private val DeviceBlue = Color(0xFF2B7CB0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectionScreen(
    uiState: DeviceSelectionUiState,
    language: AppLanguage,
    onDeviceSelected: (Int) -> Unit,
    onContinueClick: () -> Unit
) {
    val boxDevices = remember(uiState.devices) {
        uiState.devices.filter { it.deviceType.contains("box", ignoreCase = true) }
    }

    var expanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableIntStateOf(0) }
    val selectedDevice = boxDevices.firstOrNull { it.id == uiState.selectedDeviceId }
    val isWide = LocalConfiguration.current.smallestScreenWidthDp >= 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeviceBg),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_pat3),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(if (isWide) 0.35f else 0.45f),
            contentScale = ContentScale.FillBounds,
            alpha = 0.6f
        )

        Image(
            painter = painterResource(id = R.drawable.bg_pat2),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(if (isWide) 0.35f else 0.45f),
            contentScale = ContentScale.FillBounds,
            alpha = 0.6f
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(if (isWide) 0.75f else 0.88f)
                .fillMaxHeight(if (isWide) 0.85f else 0.80f)
                .shadow(elevation = 6.dp, shape = RectangleShape)
                .clip(RoundedCornerShape(10.dp))
                .background(DeviceBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (isWide) 0.55f else 0.85f)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.u_logs_logo),
                    contentDescription = "U-LOGS",
                    modifier = Modifier
                        .height(if (isWide) 90.dp else 68.dp)
                        .fillMaxWidth(if (isWide) 0.65f else 0.80f),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(if (isWide) 50.dp else 36.dp))

                Text(
                    text = localizedString(R.string.device_selection_subtitle, language),
                    fontSize = if (isWide) 20.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = DeviceDark,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isWide) 28.sp else 24.sp
                )

                Spacer(Modifier.height(if (isWide) 24.dp else 14.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else if (boxDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localizedString(R.string.no_box_devices_found, language),
                            fontSize = if (isWide) 18.sp else 15.sp,
                            color = Color(0xFF5F6B7A),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val density = LocalDensity.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { dropdownWidth = it.width }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = selectedDevice?.name
                                ?: localizedString(R.string.select_device_placeholder, language),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = if (isWide) 18.sp else 16.sp),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                disabledTextColor = Color(0xFF1C1B1F),
                                disabledBorderColor = Color(0xFF79747E),
                                disabledTrailingIconColor = Color(0xFF49454F),
                                disabledLabelColor = Color(0xFF49454F),
                            )
                        )
                        // Transparent tap area over the disabled field
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { expanded = !expanded }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .width(with(density) { dropdownWidth.toDp() })
                                .background(DeviceBg)
                        ) {
                            boxDevices.forEach { device ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = "${device.name} (${device.ipAddress})",
                                            fontSize = if (isWide) 18.sp else 16.sp
                                        ) 
                                    },
                                    onClick = {
                                        onDeviceSelected(device.id)
                                        expanded = false
                                    },
                                    modifier = Modifier.background(DeviceBg)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(if (isWide) 20.dp else 12.dp))

                    Button(
                        onClick = onContinueClick,
                        enabled = uiState.selectedDeviceId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isWide) 56.dp else 52.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeviceBlue)
                    ) {
                        Text(
                            text = localizedString(R.string.continue_action, language),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isWide) 18.sp else 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
