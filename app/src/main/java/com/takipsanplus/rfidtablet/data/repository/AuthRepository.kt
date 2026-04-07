package com.takipsanplus.rfidtablet.data.repository

import com.takipsanplus.rfidtablet.data.model.DeviceData
import com.takipsanplus.rfidtablet.data.model.LoginResponse
import com.takipsanplus.rfidtablet.data.model.UserInfoResponse

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun userInfo(token: String): Result<UserInfoResponse>
    suspend fun deviceList(token: String, companyId: Int): Result<List<DeviceData>>
}
