package com.takipsanplus.rfidtablet.domain.usecase

import com.takipsanplus.rfidtablet.data.model.LoginResponse
import com.takipsanplus.rfidtablet.data.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<LoginResponse> {
        return repository.login(username, password)
    }
}
