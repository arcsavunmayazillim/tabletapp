package com.takipsanplus.rfidtablet.data.model.consignment

import com.google.gson.annotations.SerializedName

/** Sunucu ReadApiController::consigmentZaraStore `dataList` anahtarını bekliyor (camelCase). */
data class AddConsignmentZaraRequestModel(
    val dataList: ZaraDataList
)

/**
 * Backend hem `consignee` hem `consignee_id` okuyabiliyor; eksik anahtar PHP'de $consignee atanmamasına yol açabiliyor.
 * Değer, `read/consignees` listesinden seçilen marka (alıcı) kaydının `id`'sidir.
 */
data class ZaraDataList(
    val consignee: Int,
    @SerializedName("consignee_id")
    val consigneeId: Int,
    @SerializedName("delivery_date")
    val deliveryDate: String,
    @SerializedName("item_count")
    val itemCount: String,
    @SerializedName("po_number")
    val poNumber: String
)

data class AddConsigmentResponseModel(
    val data: AddConsignmentResponseData?,
    val errorMessage: String,
    val status: String,
    val statusCode: String?
)

class AddConsignmentResponseData

data class ConsignmentResponseModel(
    val data: List<ConsignmentData>,
    val errorMessage: String,
    val status: String
)

/**
 * Parsed by [CloseConsignmentResponseModelDeserializer]: `data` may be a JSON array **or** a single object.
 */
data class CloseConsignmentResponseModel(
    val data: List<ConsignmentData> = emptyList(),
    val errorMessage: String = "",
    val status: String = ""
)

data class ConsignmentData(
    val id: Int = 0,
    @SerializedName("order_id")
    val orderId: Int = 0,
    @SerializedName("company_id")
    val companyId: Int = 0,
    @SerializedName("consignee_id")
    val consigneeId: Int = 0,
    @SerializedName("country_code")
    val countryCode: String? = null,
    /** Server may send null. */
    var name: String? = null,
    @SerializedName("plate_no")
    val plateNo: String? = null,
    @SerializedName("item_count")
    val itemCount: Int = 0,
    @SerializedName("delivery_date")
    val deliveryDate: String? = null,
    @SerializedName("record_id")
    val recordId: Int = 0,
    @SerializedName("created_user_id")
    val createdUserId: Int = 0,
    @SerializedName("updated_user_id")
    val updatedUserId: Int = 0,
    @SerializedName("old_id")
    val oldId: Int = 0,
    val status: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("deleted_at")
    val deletedAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val consignee: Consignee? = null,
    val order: Order? = null
)

data class Consignee(
    val address: String = "",
    @SerializedName("auth_name")
    val authName: String = "",
    @SerializedName("auth_phone")
    val authPhone: String = "",
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("created_user_id")
    val createdUserId: Int = 0,
    @SerializedName("deleted_at")
    val deletedAt: String = "",
    val id: Int = 0,
    val logo: String = "",
    val name: String = "",
    @SerializedName("old_id")
    val oldId: Int = 0,
    val phone: String = "",
    @SerializedName("record_id")
    val recordId: Int = 0,
    val status: Int = 0,
    @SerializedName("updated_at")
    val updatedAt: String = "",
    @SerializedName("updated_user_id")
    val updatedUserId: Int = 0,
    val viewid: Int = 0
)

data class Order(
    @SerializedName("consignee_id")
    val consigneeId: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("created_user_id")
    val createdUserId: Int = 0,
    @SerializedName("deleted_at")
    val deletedAt: String? = null,
    val id: Int = 0,
    @SerializedName("item_count")
    val itemCount: Int = 0,
    val name: String? = null,
    @SerializedName("order_code")
    val orderCode: String? = null,
    @SerializedName("po_no")
    val poNo: String? = null,
    @SerializedName("record_id")
    val recordId: Int = 0,
    val status: Int = 0,
    @SerializedName("updated_at")
    val updatedAt: String = "",
    @SerializedName("updated_user_id")
    val updatedUserId: Int = 0
)
