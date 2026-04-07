package com.takipsanplus.rfidtablet.presentation.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.data.model.DeviceData
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue

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
    val selectedDevice = boxDevices.firstOrNull { it.id == uiState.selectedDeviceId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        DeviceBackgroundVisual()

        Card(
            modifier = Modifier
                .fillMaxWidth(0.64f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localizedString(R.string.device_selection_title, language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = localizedString(R.string.device_selection_subtitle, language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5F6B7A)
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                } else {
                    if (boxDevices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = localizedString(R.string.no_box_devices_found, language),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF5F6B7A)
                            )
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                value = selectedDevice?.name
                                    ?: localizedString(R.string.select_device_placeholder, language),
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text(localizedString(R.string.device_dropdown_label, language)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                shape = RoundedCornerShape(14.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                boxDevices.forEach { device ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = "${device.name} (${device.ipAddress})")
                                        },
                                        onClick = {
                                            onDeviceSelected(device.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onContinueClick,
                            enabled = uiState.selectedDeviceId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(
                                text = localizedString(R.string.continue_action, language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceBackgroundVisual() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(Color(0x66C9E3FF), Color(0x00FFFFFF))
                )
            )
    )
}
