package com.takipsanplus.rfidtablet.presentation.device

sealed interface DeviceSelectionEvent {
    data class NavigateHome(val selectedDeviceName: String) : DeviceSelectionEvent
}
