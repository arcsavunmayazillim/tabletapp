package com.takipsanplus.rfidtablet.presentation.device

import com.takipsanplus.rfidtablet.data.model.DeviceData

data class DeviceSelectionUiState(
    val isLoading: Boolean = true,
    val devices: List<DeviceData> = emptyList(),
    val selectedDeviceId: Int? = null,
    val selectedDeviceName: String = ""
)
