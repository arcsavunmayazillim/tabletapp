package com.takipsanplus.rfidtablet.data.remote

import com.takipsanplus.rfidtablet.data.model.DevicesResponse
import com.takipsanplus.rfidtablet.data.model.LoginResponse
import com.takipsanplus.rfidtablet.data.model.UserInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface AuthApiService {
    @GET("read/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<LoginResponse>

    @GET("read/customer")
    suspend fun userInfo(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null
    ): Response<UserInfoResponse>

    @GET("read/devices")
    suspend fun deviceList(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int
    ): Response<DevicesResponse>
}
