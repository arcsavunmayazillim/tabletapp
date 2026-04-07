package com.takipsanplus.rfidtablet.presentation.login

import com.takipsanplus.rfidtablet.presentation.common.AppLanguage

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isLoading: Boolean = false,
    val token: String? = null
)
