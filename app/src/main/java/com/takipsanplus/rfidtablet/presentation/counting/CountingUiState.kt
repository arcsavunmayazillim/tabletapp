package com.takipsanplus.rfidtablet.presentation.counting

data class CountingUiState(
    val isReading: Boolean = false,
    val uniqueEpcs: List<String> = emptyList(),
    val uniqueCount: Int = 0
)

