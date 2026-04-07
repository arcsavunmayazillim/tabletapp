package com.takipsanplus.rfidtablet.domain.usecase

import com.takipsanplus.rfidtablet.data.model.UserInfoResponse
import com.takipsanplus.rfidtablet.data.repository.AuthRepository

class GetUserInfoUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String): Result<UserInfoResponse> {
        return repository.userInfo(token)
    }
}
