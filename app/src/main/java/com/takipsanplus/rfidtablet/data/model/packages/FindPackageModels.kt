package com.takipsanplus.rfidtablet.data.model.packages

import com.google.gson.annotations.SerializedName

data class FindPackageResponseModel(
    val data: FindPackageData? = null,
    val errorMessage: String = "",
    val status: String = ""
)

data class FindPackageData(
    val barcode: String? = null,
    @SerializedName("box_type_id") val boxTypeId: String? = null,
    @SerializedName("company_id") val companyId: Int? = null,
    @SerializedName("consignment_id") val consignmentId: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("created_user_id") val createdUserId: Int? = null,
    @SerializedName("deleted_at") val deletedAt: Any? = null,
    @SerializedName("device_id") val deviceId: Int? = null,
    val id: Int? = null,
    @SerializedName("items_count") val itemsCount: Int? = null,
    @SerializedName("load_type") val loadType: String? = null,
    val model: String? = null,
    @SerializedName("order_id") val orderId: Int? = null,
    @SerializedName("order_model_id") val orderModelId: Int? = null,
    @SerializedName("order_size_id") val orderSizeId: Int? = null,
    @SerializedName("package_no") val packageNo: Int? = null,
    val size: String? = null,
    val status: Int? = null,
    @SerializedName("target_quantity") val targetQuantity: Any? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    val weight: String? = null
)
