package com.takipsanplus.rfidtablet.data.model

import com.google.gson.annotations.SerializedName

data class DevicesResponse(
    val data: List<DeviceData?>,
    val errorMessage: String,
    val status: String
)

data class DeviceData(
    val antennas: String,
    @SerializedName("auto_model_name")
    val autoModelName: Int,
    @SerializedName("auto_print")
    val autoPrint: Int,
    @SerializedName("auto_size_name")
    val autoSizeName: Int,
    @SerializedName("barcode_ip_address")
    val barcodeIpAddress: Any?,
    @SerializedName("barcode_status")
    val barcodeStatus: String,
    val bridgeCloseTime: Int,
    @SerializedName("common_power")
    val commonPower: Int,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_user_id")
    val createdUserId: Int,
    @SerializedName("deleted_at")
    val deletedAt: Any?,
    val deviceCheck: Int,
    val deviceCheckDate: Any?,
    @SerializedName("device_aim")
    val deviceAim: Int,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("device_version")
    val deviceVersion: Any?,
    @SerializedName("estimated_population")
    val estimatedPopulation: Int,
    val gpioError: String,
    val gpioStart: String,
    val gpioStop: String,
    val id: Int,
    @SerializedName("ip_address")
    val ipAddress: String,
    val name: String,
    @SerializedName("package_timeout")
    val packageTimeout: String,
    @SerializedName("printer_address")
    val printerAddress: Any?,
    @SerializedName("read_type_id")
    val readTypeId: Int,
    val reader: String,
    @SerializedName("reader_mode")
    val readerMode: String,
    @SerializedName("search_mode")
    val searchMode: String,
    @SerializedName("serial_no")
    val serialNo: Any?,
    val session: Int,
    val status: Int,
    @SerializedName("string_set")
    val stringSet: Any?,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_user_id")
    val updatedUserId: Int
)
