package com.takipsanplus.rfidtablet.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.domain.usecase.LoginUseCase
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.UiMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _messages = MutableSharedFlow<UiMessage>()
    val messages: SharedFlow<UiMessage> = _messages

    init {
        val remembered = userPreferences.getRememberedCredentials()
        val storedLanguageCode = userPreferences.getLanguageCode(AppLanguage.ENGLISH.code)
        val initialLanguage = AppLanguage.entries.firstOrNull { it.code == storedLanguageCode }
            ?: AppLanguage.ENGLISH
        _uiState.update {
            it.copy(
                username = remembered.username,
                password = remembered.password,
                rememberMe = remembered.rememberMe,
                selectedLanguage = initialLanguage
            )
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onRememberMeChanged(value: Boolean) {
        _uiState.update { it.copy(rememberMe = value) }
    }

    fun onLanguageChanged(language: AppLanguage) {
        userPreferences.saveLanguageCode(language.code)
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            viewModelScope.launch {
                _messages.emit(UiMessage.Resource(R.string.error_empty_fields))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = loginUseCase(state.username.trim(), state.password)
            result.onSuccess { response ->
                val token = response.data.token.trim()
                if (token.isBlank()) {
                    _uiState.update { current -> current.copy(isLoading = false) }
                    _messages.emit(UiMessage.Resource(R.string.error_token_empty))
                    return@onSuccess
                }
                // Remember me kaydetme — getUserInfo gerekmez, token yeterli.
                if (state.rememberMe) {
                    userPreferences.saveRememberedCredentials(
                        username = state.username.trim(),
                        password = state.password
                    )
                } else {
                    userPreferences.clearRememberedCredentials()
                }
                // DeviceSelectionViewModel getUserInfo + saveSession işini zaten yapıyor.
                _uiState.update { current ->
                    current.copy(isLoading = false, token = token)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
                _messages.emit(
                    if (throwable.message.isNullOrBlank()) {
                        UiMessage.Resource(R.string.error_unknown)
                    } else {
                        UiMessage.Text(throwable.message!!)
                    }
                )
            }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(token = null) }
    }

    class Factory(
        private val loginUseCase: LoginUseCase,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(loginUseCase, userPreferences) as T
        }
    }
}
