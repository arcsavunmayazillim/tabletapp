package com.takipsanplus.rfidtablet.domain.usecase

import com.takipsanplus.rfidtablet.data.model.DeviceData
import com.takipsanplus.rfidtablet.data.repository.AuthRepository

class GetDeviceListUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String, companyId: Int): Result<List<DeviceData>> {
        return repository.deviceList(token, companyId)
    }
}
