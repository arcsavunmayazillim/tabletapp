package com.takipsanplus.rfidtablet.data.repository

import com.takipsanplus.rfidtablet.data.model.packages.CombinePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.CombinePackageResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackagesResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.PackageData
import com.takipsanplus.rfidtablet.data.model.packages.AddPackageZaraResult
import com.takipsanplus.rfidtablet.data.model.packages.PackageZaraRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.FindPackageData
import com.takipsanplus.rfidtablet.data.model.packages.FindPackageResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.PackageZaraStoreResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.PackagesResponseModel
import com.takipsanplus.rfidtablet.data.remote.PackagesApiService
import retrofit2.Response

class PackagesRepositoryImpl(
    private val api: PackagesApiService
) : PackagesRepository {

    private fun isSuccessStatus(status: String?): Boolean {
        return status.equals("success", ignoreCase = true)
    }

    private fun normalizeToken(rawToken: String): String {
        return rawToken.trim().removePrefix("Bearer ").trim()
    }

    private suspend fun requestPackagesWithFallback(
        token: String,
        consignmentId: Int
    ): Response<PackagesResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.getPackages(authorization = normalizedToken, consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getPackages(authorization = "Bearer $normalizedToken", consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getPackages(token = normalizedToken, consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getPackages(tokenQuery = normalizedToken, consignmentId = consignmentId)
        return last
    }

    private suspend fun requestFindPackageWithFallback(
        token: String,
        consignmentId: Int,
        epc: String
    ): Response<FindPackageResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.findPackage(authorization = normalizedToken, consignmentID = consignmentId, epc = epc)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.findPackage(authorization = "Bearer $normalizedToken", consignmentID = consignmentId, epc = epc)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.findPackage(token = normalizedToken, consignmentID = consignmentId, epc = epc)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.findPackage(tokenQuery = normalizedToken, consignmentID = consignmentId, epc = epc)
        return last
    }

    private suspend fun requestAddPackageWithFallback(
        token: String,
        companyId: Int,
        body: PackageZaraRequestModel
    ): Response<PackageZaraStoreResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.addPackageZaraStore(
            authorization = normalizedToken,
            companyId = companyId,
            body = body
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.addPackageZaraStore(
            authorization = "Bearer $normalizedToken",
            companyId = companyId,
            body = body
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.addPackageZaraStore(
            token = normalizedToken,
            companyId = companyId,
            body = body
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.addPackageZaraStore(
            tokenQuery = normalizedToken,
            companyId = companyId,
            body = body
        )
        return last
    }

    private fun describeFailedAddPackage(
        response: Response<PackageZaraStoreResponseModel>
    ): String {
        return describeFailedGeneric(response.code(), response.body()?.errorMessage) {
            response.errorBody()?.string()?.trim()?.take(500)
        }
    }

    private fun describeFailedGeneric(
        code: Int,
        errorMessage: String?,
        rawError: () -> String?
    ): String {
        if (!errorMessage.isNullOrBlank()) return errorMessage
        val raw = try {
            rawError()
        } catch (_: Exception) {
            null
        }
        return buildString {
            append("HTTP $code")
            if (!raw.isNullOrBlank()) {
                append(" — ")
                append(raw)
            }
        }
    }

    private suspend fun requestCombineWithFallback(
        token: String,
        companyId: Int,
        body: CombinePackageRequestModel
    ): Response<CombinePackageResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.combinePackages(authorization = normalizedToken, companyId = companyId, data = body)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.combinePackages(authorization = "Bearer $normalizedToken", companyId = companyId, data = body)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.combinePackages(token = normalizedToken, companyId = companyId, data = body)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.combinePackages(tokenQuery = normalizedToken, companyId = companyId, data = body)
        return last
    }

    private suspend fun requestDeleteWithFallback(
        token: String,
        companyId: Int,
        body: DeletePackageRequestModel
    ): Response<DeletePackagesResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.deletePackages(authorization = normalizedToken, companyId = companyId, data = body)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.deletePackages(authorization = "Bearer $normalizedToken", companyId = companyId, data = body)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.deletePackages(token = normalizedToken, companyId = companyId, data = body)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.deletePackages(tokenQuery = normalizedToken, companyId = companyId, data = body)
        return last
    }

    override suspend fun getPackages(token: String, consignmentId: Int): Result<List<PackageData>> {
        return runCatching {
            val resolved = requestPackagesWithFallback(token, consignmentId)
            if (resolved.isSuccessful) {
                val body = resolved.body()
                if (body == null) {
                    throw IllegalStateException("Packages request failed")
                }
                val rows = body.data
                if (rows.isEmpty()) {
                    return@runCatching emptyList()
                }
                if (body.status.equals("success", ignoreCase = true)) {
                    rows
                } else {
                    throw IllegalStateException(body.errorMessage.takeIf { it.isNotBlank() } ?: "Packages request failed")
                }
            } else {
                throw IllegalStateException("HTTP ${resolved.code()} ${resolved.message()}")
            }
        }
    }

    override suspend fun findPackage(
        token: String,
        consignmentId: Int,
        epc: String
    ): Result<FindPackageData> {
        return runCatching {
            val resolved = requestFindPackageWithFallback(token, consignmentId, epc)
            if (resolved.isSuccessful) {
                val b = resolved.body()
                if (b != null && b.status.equals("success", ignoreCase = true) && b.data != null) {
                    b.data
                } else {
                    throw IllegalStateException(b?.errorMessage?.takeIf { it.isNotBlank() } ?: "Find package failed")
                }
            } else {
                throw IllegalStateException(
                    describeFailedGeneric(resolved.code(), resolved.body()?.errorMessage) {
                        resolved.errorBody()?.string()?.trim()?.take(500)
                    }
                )
            }
        }
    }

    override suspend fun addPackageZaraStore(
        token: String,
        companyId: Int,
        body: PackageZaraRequestModel
    ): Result<AddPackageZaraResult> {
        return runCatching {
            val resolved = requestAddPackageWithFallback(token, companyId, body)
            if (resolved.isSuccessful) {
                val b = resolved.body()
                if (b != null && b.status.equals("success", ignoreCase = true)) {
                    val msg = b.errorMessage.trim()
                    if (msg.isNotEmpty()) {
                        AddPackageZaraResult.OkWithServerMessage(msg)
                    } else {
                        AddPackageZaraResult.Ok
                    }
                } else {
                    throw IllegalStateException(b?.errorMessage?.takeIf { it.isNotBlank() } ?: "Add package failed")
                }
            } else {
                throw IllegalStateException(describeFailedAddPackage(resolved))
            }
        }
    }

    override suspend fun combinePackages(
        token: String,
        companyId: Int,
        body: CombinePackageRequestModel
    ): Result<Unit> {
        return runCatching {
            val resolved = requestCombineWithFallback(token, companyId, body)
            if (resolved.isSuccessful) {
                val b = resolved.body()
                if (b != null && b.status.equals("success", ignoreCase = true)) {
                    Unit
                } else {
                    throw IllegalStateException(b?.errorMessage?.takeIf { it.isNotBlank() } ?: "Combine packages failed")
                }
            } else {
                throw IllegalStateException(
                    describeFailedGeneric(resolved.code(), resolved.body()?.errorMessage) {
                        resolved.errorBody()?.string()?.trim()?.take(500)
                    }
                )
            }
        }
    }

    override suspend fun deletePackages(
        token: String,
        companyId: Int,
        body: DeletePackageRequestModel
    ): Result<Unit> {
        return runCatching {
            val resolved = requestDeleteWithFallback(token, companyId, body)
            if (resolved.isSuccessful) {
                val b = resolved.body()
                if (b != null && b.status.equals("success", ignoreCase = true)) {
                    Unit
                } else {
                    throw IllegalStateException(b?.errorMessage?.takeIf { it.isNotBlank() } ?: "Delete packages failed")
                }
            } else {
                throw IllegalStateException(
                    describeFailedGeneric(resolved.code(), resolved.body()?.errorMessage) {
                        resolved.errorBody()?.string()?.trim()?.take(500)
                    }
                )
            }
        }
    }
}
