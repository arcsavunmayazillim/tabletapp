package com.takipsanplus.rfidtablet.data.remote

import com.takipsanplus.rfidtablet.data.model.consignment.AddConsignmentZaraRequestModel
import com.takipsanplus.rfidtablet.data.model.consignment.AddConsigmentResponseModel
import com.takipsanplus.rfidtablet.data.model.consignment.CloseConsignmentResponseModel
import com.takipsanplus.rfidtablet.data.model.consignment.ConsigneesResponseModel
import com.takipsanplus.rfidtablet.data.model.consignment.ConsignmentResponseModel
import com.takipsanplus.rfidtablet.data.model.consignment.ShipmentTotalResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ConsignmentApiService {

    @GET("read/consignees")
    suspend fun getConsignees(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int
    ): Response<ConsigneesResponseModel>

    @GET("read/getSizeTotalsByConsignment")
    suspend fun getSizeTotalsByConsignment(
        @Header("Authorization") authorization: String? = null,
        @Header("token") tokenHeader: String? = null,
        @Query("token") token: String? = null,
        @Query("consignmentId") consignmentId: String
    ): Response<ShipmentTotalResponse>

    @GET("read/consignments")
    suspend fun getConsignments(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int
    ): Response<ConsignmentResponseModel>

    @GET("read/closeConsignment")
    suspend fun closeConsignment(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int,
        @Query("consignmentId") consignmentId: Int
    ): Response<CloseConsignmentResponseModel>

    @POST("read/consigmentZaraStore")
    suspend fun addConsignmentZaraStore(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int,
        @Body body: AddConsignmentZaraRequestModel
    ): Response<AddConsigmentResponseModel>
}
