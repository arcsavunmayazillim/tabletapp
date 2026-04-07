package com.takipsanplus.rfidtablet.data.model.consignment

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ConsigneesResponseModel(
    val data: List<ConsigneeData> = emptyList(),
    val errorMessage: String = "",
    val status: String = ""
)

/**
 * `read/consignees` satırı — sunucu snake_case döner.
 */
data class ConsigneeData(
    val address: String = "",
    @SerializedName("auth_name")
    val authName: String = "",
    @SerializedName("auth_phone")
    val authPhone: String = "",
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("created_user_id")
    val createdUserId: Int = 0,
    @SerializedName("deleted_at")
    val deletedAt: JsonElement? = null,
    val id: Int = 0,
    val logo: String = "",
    val name: String = "",
    @SerializedName("old_id")
    val oldId: Int = 0,
    val phone: String = "",
    @SerializedName("record_id")
    val recordId: JsonElement? = null,
    val status: Int = 0,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("updated_user_id")
    val updatedUserId: Int = 0,
    @SerializedName("viewid")
    val viewId: Int = 0
)
