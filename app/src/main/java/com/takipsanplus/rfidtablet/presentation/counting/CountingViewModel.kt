package com.takipsanplus.rfidtablet.presentation.counting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takipsanplus.rfidtablet.data.bluetooth.BluetoothConnectionController
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import android.util.Log
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

    private val weightEnabled: Boolean by lazy {
        val stored = userPreferences.getReaderSettings(
            default = UserPreferences.ReaderSettings(
                ant1 = 0,
                ant2 = 0,
                ant3 = 0,
                ant4 = 0,
                packetCloseTimeout = 5,
                weightEnabled = true,
                barcodeEnabled = false
            )
        )
        stored.weightEnabled
    }

    init {
        // Collect EPCs from bluetooth and add them only while reading.
        viewModelScope.launch {
            BluetoothConnectionController.epcEvents.collectLatest { epc ->
                val reading = _uiState.value.isReading
                if (!reading) return@collectLatest
                val normalized = epc.trim()
                if (normalized.isBlank()) return@collectLatest

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
        if (uiState.value.isReading) {
            stopReading()
        } else {
            startReading()
        }
    }

    private fun startReading() {
        val weightFlag = if (weightEnabled) "1" else "0"
        _uiState.update { it.copy(isReading = true) }
        BluetoothConnectionController.sendCommand("""{"Status":["Start","$weightFlag"]}""")
    }

    private fun stopReading() {
        _uiState.update { it.copy(isReading = false) }
        BluetoothConnectionController.sendCommand("""{"Status":["Stop"]}""")
    }

    fun onClearClicked() {
        // Stop first (if running), then clear UI.
        _uiState.update { it.copy(isReading = false) }
        BluetoothConnectionController.sendCommand("""{"Status":["Stop"]}""")
        uniqueSet.clear()
        _uiState.update { it.copy(uniqueEpcs = emptyList(), uniqueCount = 0) }
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

