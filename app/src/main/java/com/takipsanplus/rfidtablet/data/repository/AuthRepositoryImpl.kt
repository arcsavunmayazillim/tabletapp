package com.takipsanplus.rfidtablet.data.repository

import com.takipsanplus.rfidtablet.data.model.DeviceData
import com.takipsanplus.rfidtablet.data.model.LoginResponse
import com.takipsanplus.rfidtablet.data.model.UserInfoResponse
import com.takipsanplus.rfidtablet.data.remote.AuthApiService

class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {
    private fun isSuccessStatus(status: String?): Boolean {
        return status.equals("success", ignoreCase = true)
    }

    private fun normalizeToken(rawToken: String): String {
        return rawToken.trim().removePrefix("Bearer ").trim()
    }

    private suspend fun requestUserInfoWithFallback(token: String): retrofit2.Response<UserInfoResponse> {
        val normalizedToken = normalizeToken(token)

        var last = apiService.userInfo(authorization = normalizedToken)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = apiService.userInfo(authorization = "Bearer $normalizedToken")
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = apiService.userInfo(token = normalizedToken)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = apiService.userInfo(tokenQuery = normalizedToken)
        return last
    }

    private suspend fun requestDeviceListWithFallback(
        token: String,
        companyId: Int
    ): retrofit2.Response<com.takipsanplus.rfidtablet.data.model.DevicesResponse> {
        val normalizedToken = normalizeToken(token)

        var last = apiService.deviceList(
            authorization = normalizedToken,
            companyId = companyId
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = apiService.deviceList(
            authorization = "Bearer $normalizedToken",
            companyId = companyId
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = apiService.deviceList(
            token = normalizedToken,
            companyId = companyId
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = apiService.deviceList(
            tokenQuery = normalizedToken,
            companyId = companyId
        )
        return last
    }


    override suspend fun login(username: String, password: String): Result<LoginResponse> {
        return runCatching {
            val response = apiService.login(username, password)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status.equals("success", ignoreCase = true)) {
                    body
                } else {
                    throw IllegalStateException(body?.errorMessage ?: "Login failed")
                }
            } else {
                throw IllegalStateException("HTTP ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun userInfo(token: String): Result<UserInfoResponse> {
        return runCatching {
            val resolved = requestUserInfoWithFallback(token)
            if (resolved.isSuccessful) {
                val body = resolved.body()
                if (body != null && body.status.equals("success", ignoreCase = true)) {
                    body
                } else {
                    throw IllegalStateException(body?.errorMessage ?: "User info request failed")
                }
            } else {
                throw IllegalStateException("HTTP ${resolved.code()} ${resolved.message()}")
            }
        }
    }

    override suspend fun deviceList(token: String, companyId: Int): Result<List<DeviceData>> {
        return runCatching {
            val resolved = requestDeviceListWithFallback(token, companyId)
            if (resolved.isSuccessful) {
                val body = resolved.body()
                if (body != null && body.status.equals("success", ignoreCase = true)) {
                    body.data.filterNotNull()
                } else {
                    throw IllegalStateException(body?.errorMessage ?: "Device list request failed")
                }
            } else {
                throw IllegalStateException("HTTP ${resolved.code()} ${resolved.message()}")
            }
        }
    }
}
