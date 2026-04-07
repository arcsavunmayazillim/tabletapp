package com.takipsanplus.rfidtablet.data.repository

import com.takipsanplus.rfidtablet.data.model.consignment.AddConsignmentZaraRequestModel
import com.takipsanplus.rfidtablet.data.model.consignment.ConsigneeData
import com.takipsanplus.rfidtablet.data.model.consignment.ConsignmentData
import com.takipsanplus.rfidtablet.data.model.consignment.SizeQuantity

interface ConsignmentRepository {
    suspend fun getConsignees(token: String, companyId: Int): Result<List<ConsigneeData>>

    suspend fun getSizeTotalsByConsignment(token: String, consignmentId: String): Result<List<SizeQuantity>>

    suspend fun getConsignments(token: String, companyId: Int): Result<List<ConsignmentData>>
    suspend fun addConsignmentZaraStore(
        token: String,
        companyId: Int,
        body: AddConsignmentZaraRequestModel
    ): Result<Unit>

    suspend fun closeConsignment(token: String, companyId: Int, consignmentId: Int): Result<Unit>
}
