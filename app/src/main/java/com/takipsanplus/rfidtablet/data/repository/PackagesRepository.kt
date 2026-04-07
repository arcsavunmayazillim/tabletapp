package com.takipsanplus.rfidtablet.data.repository

import com.takipsanplus.rfidtablet.data.model.packages.CombinePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.AddPackageZaraResult
import com.takipsanplus.rfidtablet.data.model.packages.FindPackageData
import com.takipsanplus.rfidtablet.data.model.packages.PackageData
import com.takipsanplus.rfidtablet.data.model.packages.PackageZaraRequestModel

interface PackagesRepository {
    suspend fun getPackages(token: String, consignmentId: Int): Result<List<PackageData>>

    suspend fun findPackage(token: String, consignmentId: Int, epc: String): Result<FindPackageData>

    suspend fun addPackageZaraStore(
        token: String,
        companyId: Int,
        body: PackageZaraRequestModel
    ): Result<AddPackageZaraResult>

    suspend fun combinePackages(
        token: String,
        companyId: Int,
        body: CombinePackageRequestModel
    ): Result<Unit>

    suspend fun deletePackages(
        token: String,
        companyId: Int,
        body: DeletePackageRequestModel
    ): Result<Unit>
}
