package com.takipsanplus.rfidtablet.data.model.packages

import com.google.gson.annotations.SerializedName

data class PackagesResponseModel(
    val data: List<PackageData> = emptyList(),
    val errorMessage: String = "",
    val status: String = "",
    val totalSecurityTag: Int? = null,
    val totalWrongTag: Int? = null
)

data class PackageData(
    @SerializedName("box_type_id")
    val boxTypeId: String? = null,
    @SerializedName("details")
    val detailsMap: HashMap<String, Int>? = null,
    val id: Int = 0,
    val items: List<Item> = emptyList(),
    @SerializedName("load_type")
    val loadType: String? = null,
    var model: String? = null,
    @SerializedName("package_no")
    var packageNo: Int = 0,
    var size: String? = null,
    val barcode: String? = null,
    @SerializedName("inditex_qr_flag")
    val inditexQrFlag: Boolean? = null,
    val weight: String? = null,
    @SerializedName("items_count")
    val itemsCount: String? = null,
    @SerializedName("wrong_format_count")
    val wrongFormatCount: Int = 0,
    @SerializedName("normal_tag_count")
    val normalTagCount: Int = 0,
    @SerializedName("security_tag_count")
    val securityTagCount: Int = 0,
    @SerializedName("extra_alarm_list")
    val extraAlarmList: List<Item>? = null,
    @SerializedName("target_quantity")
    val targetQuantity: Int? = null,
    @SerializedName("packageTrueStatus")
    val packageTrueStatus: Int? = null,
    @SerializedName("statusScan")
    val statusScan: String? = null
)

data class ItemDetail(
    @SerializedName("sds_code")
    val sdsCode: String? = null,
    val description: String? = null
)

/** POST read/packageZaraTestStore */
data class PackageZaraRequestModel(
    val dataList: PackageZaraDataList
)

data class PackageZaraDataList(
    val box_type_id: Int,
    val consignmentId: Int,
    val device_id: Int,
    val load_type: String,
    val model: String,
    val orderId: Int,
    /** Gson Kotlin data class: annotation must target JVM field or JSON key `package` is omitted. */
    @field:SerializedName("package")
    val packageId: Int,
    val size: String,
    @field:SerializedName("data")
    val dataList: List<PackageEpcData>,
    val barcode: String,
    val weight: String
)

data class PackageEpcData(
    val epc: String
)

data class PackageZaraStoreResponseModel(
    val data: AddPackageData?,
    val errorMessage: String = "",
    val status: String = "",
    val statusCode: String = ""
)

/** Outcome of POST add package (RFID batch). */
sealed interface AddPackageZaraResult {
    data object Ok : AddPackageZaraResult
    /** HTTP success with a non-empty `errorMessage` from server (show as warning). */
    data class OkWithServerMessage(val message: String) : AddPackageZaraResult
}

data class AddPackageData(
    val packageId: Int? = null
)

/** POST read/combinePackagesOfflineTest */
data class CombinePackageRequestModel(
    val dataList: CombineDataList
)

data class CombineDataList(
    val consignmentId: Int,
    val packages: List<String>
)

data class CombinePackageResponseModel(
    val data: CombineResponseData?,
    val errorMessage: String = "",
    val status: String = ""
)

data class CombineResponseData(
    val id: Int = 0,
    @SerializedName("items_count")
    val itemsCount: Int = 0,
    val model: String = "",
    @SerializedName("package_no")
    val packageNo: Int = 0,
    val size: String = ""
)

/** POST read/deletePackages */
data class DeletePackageRequestModel(
    val dataList: DeletePackageDataList
)

data class DeletePackageDataList(
    val consignmentId: Int,
    @field:SerializedName("data")
    val entries: List<DeletePackageData>
)

data class DeletePackageData(
    val id: String
)

data class DeletePackagesResponseModel(
    val data: AddPackageData?,
    val errorMessage: String = "",
    val status: String = "",
    val statusCode: String = ""
)

data class Item(
    @SerializedName("company_id")
    val companyId: Int = 0,
    @SerializedName("consignment_id")
    val consignmentId: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("created_user_id")
    val createdUserId: Int = 0,
    @SerializedName("deleted_at")
    val deletedAt: String? = null,
    @SerializedName("device_id")
    val deviceId: Int = 0,
    val epc: String? = null,
    val gtin: String? = null,
    val id: Int = 0,
    @SerializedName("order_id")
    val orderId: Int = 0,
    @SerializedName("package_id")
    val packageId: Int = 0,
    val size: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("item_details")
    val itemDetails: ItemDetail? = null,
    val model: String? = null,
    val quality: String? = null,
    val color: String? = null,
    @SerializedName("security_tag")
    val securityTag: Int = 0
)
