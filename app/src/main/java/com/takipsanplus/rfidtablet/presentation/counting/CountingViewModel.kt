package com.takipsanplus.rfidtablet.presentation.counting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takipsanplus.rfidtablet.data.network.BridgePlusConnectionController
import com.takipsanplus.rfidtablet.data.network.DeviceConnectionState
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import android.util.Log
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CountingViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private companion object {
        const val TAG = "RFIDTabletCounting"
    }

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(CountingUiState())
    val uiState: StateFlow<CountingUiState> = _uiState.asStateFlow()

    private val uniqueSet = LinkedHashSet<String>()

    init {
        viewModelScope.launch {
            BridgePlusConnectionController.state.collectLatest { state ->
                val isConnected = state is DeviceConnectionState.Connected
                _uiState.update {
                    it.copy(
                        isDeviceConnected = isConnected,
                        showDisconnectedWarning = if (isConnected) false else it.showDisconnectedWarning
                    )
                }
                if (!isConnected && _uiState.value.isReading) {
                    stopReading()
                }
            }
        }

        viewModelScope.launch {
            BridgePlusConnectionController.epcEvents.collect { epc ->
                val reading = _uiState.value.isReading
                if (!reading) return@collect
                val normalized = epc.trim()
                if (normalized.isBlank()) return@collect

                if (uniqueSet.add(normalized)) {
                    Log.d(TAG, "Unique EPC added: $normalized (count=${uniqueSet.size})")
                    _uiState.update { current ->
                        current.copy(
                            uniqueEpcs = uniqueSet.toList(),
                            uniqueCount = uniqueSet.size
                        )
                    }
                }
            }
        }
    }

    fun onStartStopClicked() {
        if (!uiState.value.isDeviceConnected) {
            _uiState.update { it.copy(showDisconnectedWarning = true) }
            return
        }
        
        // Hide warning if we are proceeding
        _uiState.update { it.copy(showDisconnectedWarning = false) }

        if (uiState.value.isReading) {
            stopReading()
        } else {
            startReading()
        }
    }

    private fun startReading() {
        _uiState.update { it.copy(isReading = true) }
        BridgePlusConnectionController.startScan()
    }

    private fun stopReading() {
        _uiState.update { it.copy(isReading = false) }
        BridgePlusConnectionController.stopScan()
    }

    fun onClearClicked() {
        BridgePlusConnectionController.stopScan()
        uniqueSet.clear()
        _uiState.update { it.copy(isReading = false, uniqueEpcs = emptyList(), uniqueCount = 0) }
    }

    override fun onCleared() {
        // Ekrandan çıkılırken (back tuşu dahil) tarama durdurulur
        if (_uiState.value.isReading) {
            BridgePlusConnectionController.stopScan()
        }
        super.onCleared()
    }

    class Factory(
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CountingViewModel(userPreferences) as T
        }
    }
}

