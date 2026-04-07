package com.takipsanplus.rfidtablet.data.model

data class LoginResponse(
    val data: LoginData,
    val errorMessage: String,
    val status: String
)

data class LoginData(
    val token: String
)
