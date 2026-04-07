package com.takipsanplus.rfidtablet.presentation.common

import android.content.Context
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import com.takipsanplus.rfidtablet.data.remote.NetworkModule
import com.takipsanplus.rfidtablet.data.repository.AuthRepositoryImpl
import com.takipsanplus.rfidtablet.domain.usecase.GetDeviceListUseCase
import com.takipsanplus.rfidtablet.domain.usecase.GetUserInfoUseCase
import com.takipsanplus.rfidtablet.data.repository.ConsignmentRepository
import com.takipsanplus.rfidtablet.data.repository.ConsignmentRepositoryImpl
import com.takipsanplus.rfidtablet.data.repository.PackagesRepository
import com.takipsanplus.rfidtablet.data.repository.PackagesRepositoryImpl
import com.takipsanplus.rfidtablet.domain.usecase.LoginUseCase

object ServiceLocator {
    private val authRepository by lazy { AuthRepositoryImpl(NetworkModule.authApi) }
    private val consignmentRepository: ConsignmentRepository by lazy {
        ConsignmentRepositoryImpl(NetworkModule.consignmentApi)
    }
    private val packagesRepository: PackagesRepository by lazy {
        PackagesRepositoryImpl(NetworkModule.packagesApi)
    }

    fun provideLoginUseCase(): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    fun provideGetUserInfoUseCase(): GetUserInfoUseCase {
        return GetUserInfoUseCase(authRepository)
    }

    fun provideGetDeviceListUseCase(): GetDeviceListUseCase {
        return GetDeviceListUseCase(authRepository)
    }

    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences(context.applicationContext)
    }

    fun provideConsignmentRepository(): ConsignmentRepository = consignmentRepository

    fun providePackagesRepository(): PackagesRepository = packagesRepository
}
