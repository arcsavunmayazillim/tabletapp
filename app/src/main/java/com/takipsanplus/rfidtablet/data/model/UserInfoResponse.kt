package com.takipsanplus.rfidtablet.data.model

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    val data: UserInfoData?,
    val errorMessage: String,
    val status: String
)

data class UserInfoData(
    val company: Company?,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_user_id")
    val createdUserId: Int,
    @SerializedName("deleted_at")
    val deletedAt: Any?,
    val email: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any?,
    val id: Int,
    @SerializedName("is_admin")
    val isAdmin: Int,
    val name: String,
    val status: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_user_id")
    val updatedUserId: Int,
    val username: String
)

data class Company(
    val address: String,
    @SerializedName("atma_level")
    val atmaLevel: Int,
    @SerializedName("atma_siteId")
    val atmaSiteId: Int,
    @SerializedName("consignment_close")
    val consignmentClose: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_user_id")
    val createdUserId: Int,
    @SerializedName("deleted_at")
    val deletedAt: Any?,
    val email: String,
    val id: Int,
    @SerializedName("is_spc_member")
    val isSpcMember: Int,
    @SerializedName("ssccMethod")
    val hmMethod: List<Any>?,
    @SerializedName("is_atma_member")
    val isAtmaMember: Int,
    @SerializedName("is_partner_company")
    val isPartnerCompany: Int,
    val latitude: String,
    val logo: String,
    val longitude: String,
    @SerializedName("main_company_id")
    val mainCompanyId: Int,
    val name: String,
    @SerializedName("old_id")
    val oldId: Int,
    val organization: Any?,
    val phone: String,
    @SerializedName("record_id")
    val recordId: Any?,
    val status: Int,
    val title: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_user_id")
    val updatedUserId: Int,
    @SerializedName("is_inditex_member")
    val isInditexMember: Int,
    @SerializedName("is_inditex_supplier_id")
    val isInditexSupplier: Any?,
    @SerializedName("is_inditex_atma_member")
    val isInditexAtma: Int
)
