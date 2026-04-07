package com.takipsanplus.rfidtablet.data.remote

import com.takipsanplus.rfidtablet.data.model.packages.CombinePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.CombinePackageResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.FindPackageResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackagesResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.PackageZaraRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.PackageZaraStoreResponseModel
import com.takipsanplus.rfidtablet.data.model.packages.PackagesResponseModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface PackagesApiService {

    @GET("read/packages")
    suspend fun getPackages(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("consignmentID") consignmentId: Int
    ): Response<PackagesResponseModel>

    @GET("read/findPackage")
    suspend fun findPackage(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("consignmentID") consignmentID: Int,
        @Query("epc") epc: String
    ): Response<FindPackageResponseModel>

    @POST("read/packageZaraTestStore")
    suspend fun addPackageZaraStore(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int,
        @Body body: PackageZaraRequestModel
    ): Response<PackageZaraStoreResponseModel>

    @POST("read/combinePackagesOfflineTest")
    suspend fun combinePackages(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int,
        @Body data: CombinePackageRequestModel
    ): Response<CombinePackageResponseModel>

    @POST("read/deletePackages")
    suspend fun deletePackages(
        @Header("Authorization") authorization: String? = null,
        @Header("token") token: String? = null,
        @Query("token") tokenQuery: String? = null,
        @Query("companyID") companyId: Int,
        @Body data: DeletePackageRequestModel
    ): Response<DeletePackagesResponseModel>
}
