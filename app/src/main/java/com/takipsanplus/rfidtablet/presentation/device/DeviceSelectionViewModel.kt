package com.takipsanplus.rfidtablet.presentation.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import com.takipsanplus.rfidtablet.domain.usecase.GetDeviceListUseCase
import com.takipsanplus.rfidtablet.domain.usecase.GetUserInfoUseCase
import com.takipsanplus.rfidtablet.presentation.common.UiMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeviceSelectionViewModel(
    private val token: String,
    private val userPreferences: UserPreferences,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getDeviceListUseCase: GetDeviceListUseCase
) : ViewModel() {
    private fun isBoxDeviceType(deviceType: String): Boolean {
        return deviceType.contains("box", ignoreCase = true)
    }

    private val _uiState = MutableStateFlow(DeviceSelectionUiState())
    val uiState: StateFlow<DeviceSelectionUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<UiMessage>()
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()
    private val _events = MutableSharedFlow<DeviceSelectionEvent>()
    val events: SharedFlow<DeviceSelectionEvent> = _events.asSharedFlow()

    init {
        fetchDevices()
    }

    private fun fetchDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getUserInfoUseCase(token)
                .onFailure {
                    _uiState.update { state -> state.copy(isLoading = false) }
                    _messages.emit(
                        if (it.message.isNullOrBlank()) {
                            UiMessage.Resource(R.string.error_user_info_load)
                        } else {
                            UiMessage.Text(it.message!!)
                        }
                    )
                    return@launch
                }
                .onSuccess { userInfo ->
                    val companyId = userInfo.data?.companyId
                    if (companyId == null) {
                        _uiState.update { state -> state.copy(isLoading = false) }
                        _messages.emit(UiMessage.Resource(R.string.error_company_not_found))
                        return@launch
                    }

                    userPreferences.saveSession(token, companyId)

                    getDeviceListUseCase(token, companyId)
                        .onFailure {
                            _uiState.update { state -> state.copy(isLoading = false) }
                            _messages.emit(
                                if (it.message.isNullOrBlank()) {
                                    UiMessage.Resource(R.string.error_device_list_load)
                                } else {
                                    UiMessage.Text(it.message!!)
                                }
                            )
                        }
                        .onSuccess { devices ->
                            val defaultSelected = devices.firstOrNull { isBoxDeviceType(it.deviceType) }
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    devices = devices,
                                    selectedDeviceId = defaultSelected?.id,
                                    selectedDeviceName = defaultSelected?.name.orEmpty()
                                )
                            }
                        }
                }
        }
    }

    fun onDeviceSelected(deviceId: Int) {
        val picked = _uiState.value.devices.firstOrNull { it.id == deviceId } ?: return
        _uiState.update {
            it.copy(selectedDeviceId = deviceId, selectedDeviceName = picked.name)
        }
    }

    fun onContinueClicked() {
        viewModelScope.launch {
            val selectedName = _uiState.value.selectedDeviceName
            if (selectedName.isBlank()) {
                _messages.emit(UiMessage.Resource(R.string.error_select_device))
            } else {
                _events.emit(DeviceSelectionEvent.NavigateHome(selectedName))
            }
        }
    }

    class Factory(
        private val token: String,
        private val userPreferences: UserPreferences,
        private val getUserInfoUseCase: GetUserInfoUseCase,
        private val getDeviceListUseCase: GetDeviceListUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceSelectionViewModel(token, userPreferences, getUserInfoUseCase, getDeviceListUseCase) as T
        }
    }
}
