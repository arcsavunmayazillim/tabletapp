package com.takipsanplus.rfidtablet.data.repository

import com.takipsanplus.rfidtablet.data.model.consignment.AddConsignmentZaraRequestModel
import com.takipsanplus.rfidtablet.data.model.consignment.ConsigneeData
import com.takipsanplus.rfidtablet.data.model.consignment.ConsignmentData
import com.takipsanplus.rfidtablet.data.model.consignment.SizeQuantity
import com.takipsanplus.rfidtablet.data.remote.ConsignmentApiService
import retrofit2.Response

class ConsignmentRepositoryImpl(
    private val api: ConsignmentApiService
) : ConsignmentRepository {

    private fun isSuccessStatus(status: String?): Boolean {
        return status.equals("success", ignoreCase = true)
    }

    private fun normalizeToken(rawToken: String): String {
        return rawToken.trim().removePrefix("Bearer ").trim()
    }

    private suspend fun requestConsigneesWithFallback(
        token: String,
        companyId: Int
    ): Response<com.takipsanplus.rfidtablet.data.model.consignment.ConsigneesResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.getConsignees(authorization = normalizedToken, companyId = companyId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getConsignees(authorization = "Bearer $normalizedToken", companyId = companyId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getConsignees(token = normalizedToken, companyId = companyId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getConsignees(tokenQuery = normalizedToken, companyId = companyId)
        return last
    }

    private suspend fun requestConsignmentsWithFallback(
        token: String,
        companyId: Int
    ): Response<com.takipsanplus.rfidtablet.data.model.consignment.ConsignmentResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.getConsignments(authorization = normalizedToken, companyId = companyId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getConsignments(authorization = "Bearer $normalizedToken", companyId = companyId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getConsignments(token = normalizedToken, companyId = companyId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getConsignments(tokenQuery = normalizedToken, companyId = companyId)
        return last
    }

    private suspend fun requestAddConsignmentWithFallback(
        token: String,
        companyId: Int,
        body: AddConsignmentZaraRequestModel
    ): Response<com.takipsanplus.rfidtablet.data.model.consignment.AddConsigmentResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.addConsignmentZaraStore(
            authorization = normalizedToken,
            companyId = companyId,
            body = body
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.addConsignmentZaraStore(
            authorization = "Bearer $normalizedToken",
            companyId = companyId,
            body = body
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.addConsignmentZaraStore(
            token = normalizedToken,
            companyId = companyId,
            body = body
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.addConsignmentZaraStore(
            tokenQuery = normalizedToken,
            companyId = companyId,
            body = body
        )
        return last
    }

    private suspend fun requestCloseConsignmentWithFallback(
        token: String,
        companyId: Int,
        consignmentId: Int
    ): Response<com.takipsanplus.rfidtablet.data.model.consignment.CloseConsignmentResponseModel> {
        val normalizedToken = normalizeToken(token)
        var last = api.closeConsignment(
            authorization = normalizedToken,
            companyId = companyId,
            consignmentId = consignmentId
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.closeConsignment(
            authorization = "Bearer $normalizedToken",
            companyId = companyId,
            consignmentId = consignmentId
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.closeConsignment(
            token = normalizedToken,
            companyId = companyId,
            consignmentId = consignmentId
        )
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.closeConsignment(
            tokenQuery = normalizedToken,
            companyId = companyId,
            consignmentId = consignmentId
        )
        return last
    }

    private suspend fun requestSizeTotalsWithFallback(
        token: String,
        consignmentId: String
    ): Response<com.takipsanplus.rfidtablet.data.model.consignment.ShipmentTotalResponse> {
        val normalizedToken = normalizeToken(token)
        var last = api.getSizeTotalsByConsignment(token = normalizedToken, consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getSizeTotalsByConsignment(token = "Bearer $normalizedToken", consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getSizeTotalsByConsignment(authorization = normalizedToken, consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getSizeTotalsByConsignment(authorization = "Bearer $normalizedToken", consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getSizeTotalsByConsignment(tokenHeader = normalizedToken, consignmentId = consignmentId)
        if (last.isSuccessful && isSuccessStatus(last.body()?.status)) return last

        last = api.getSizeTotalsByConsignment(tokenHeader = "Bearer $normalizedToken", consignmentId = consignmentId)
        return last
    }

    private fun describeFailedResponse(
        response: Response<com.takipsanplus.rfidtablet.data.model.consignment.AddConsigmentResponseModel>
    ): String {
        val code = response.code()
        val body = response.body()
        if (body != null && body.errorMessage.isNotBlank()) {
            return body.errorMessage
        }
        val raw = try {
            response.errorBody()?.string()?.trim()?.take(500)
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

    override suspend fun getSizeTotalsByConsignment(token: String, consignmentId: String): Result<List<SizeQuantity>> {
        return runCatching {
            val resolved = requestSizeTotalsWithFallback(token, consignmentId)
            if (resolved.isSuccessful) {
                val body = resolved.body()
                if (body != null && body.status.equals("success", ignoreCase = true)) {
                    body.data.orEmpty()
                } else {
                    throw IllegalStateException(body?.errorMessage?.takeIf { !it.isNullOrBlank() } ?: "Size totals failed")
                }
            } else {
                throw IllegalStateException("HTTP ${resolved.code()} ${resolved.message()}")
            }
        }
    }

    override suspend fun getConsignees(token: String, companyId: Int): Result<List<ConsigneeData>> {
        return runCatching {
            val resolved = requestConsigneesWithFallback(token, companyId)
            if (resolved.isSuccessful) {
                val body = resolved.body()
                if (body != null && body.status.equals("success", ignoreCase = true)) {
                    body.data
                } else {
                    throw IllegalStateException(body?.errorMessage ?: "Consignees request failed")
                }
            } else {
                throw IllegalStateException("HTTP ${resolved.code()} ${resolved.message()}")
            }
        }
    }

    override suspend fun getConsignments(token: String, companyId: Int): Result<List<ConsignmentData>> {
        return runCatching {
            val resolved = requestConsignmentsWithFallback(token, companyId)
            if (resolved.isSuccessful) {
                val body = resolved.body()
                if (body != null && body.status.equals("success", ignoreCase = true)) {
                    body.data
                } else {
                    throw IllegalStateException(body?.errorMessage ?: "Consignments request failed")
                }
            } else {
                throw IllegalStateException("HTTP ${resolved.code()} ${resolved.message()}")
            }
        }
    }

    override suspend fun addConsignmentZaraStore(
        token: String,
        companyId: Int,
        body: AddConsignmentZaraRequestModel
    ): Result<Unit> {
        return runCatching {
            val resolved = requestAddConsignmentWithFallback(token, companyId, body)
            if (resolved.isSuccessful) {
                val b = resolved.body()
                if (b != null && b.status.equals("success", ignoreCase = true)) {
                    Unit
                } else {
                    throw IllegalStateException(b?.errorMessage?.takeIf { it.isNotBlank() } ?: "Add consignment failed")
                }
            } else {
                throw IllegalStateException(describeFailedResponse(resolved))
            }
        }
    }

    override suspend fun closeConsignment(token: String, companyId: Int, consignmentId: Int): Result<Unit> {
        return runCatching {
            val resolved = requestCloseConsignmentWithFallback(token, companyId, consignmentId)
            if (resolved.isSuccessful) {
                val body = resolved.body()
                if (body != null && body.status.equals("success", ignoreCase = true)) {
                    Unit
                } else {
                    throw IllegalStateException(body?.errorMessage?.takeIf { it.isNotBlank() } ?: "Close consignment failed")
                }
            } else {
                val raw = try {
                    resolved.errorBody()?.string()?.trim()?.take(500)
                } catch (_: Exception) {
                    null
                }
                throw IllegalStateException(
                    buildString {
                        append("HTTP ${resolved.code()}")
                        if (!raw.isNullOrBlank()) {
                            append(" — ")
                            append(raw)
                        }
                    }
                )
            }
        }
    }
}
