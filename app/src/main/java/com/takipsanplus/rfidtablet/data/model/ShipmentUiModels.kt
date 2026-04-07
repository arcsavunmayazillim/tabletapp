package com.takipsanplus.rfidtablet.data.model

/**
 * UI models for Shipment / dispatch. Extend with API DTO mapping when endpoints are ready.
 * [ShipmentPackage.extraFields] holds additional API fields without schema churn in UI.
 */
data class ShipmentSummaryUi(
    val id: String,
    /** Sunucu consignment primary key — read/packages consignmentID */
    val consignmentRemoteId: Int = 0,
    /** read/packageZaraStore orderId (consignment order_id veya order.id) */
    val orderRemoteId: Int = 0,
    val name: String,
    /** Teslimat / sevkiyat tarihi (API: delivery_date) */
    val createdAtLabel: String,
    /** Okutulan paket sayısı (yerel) */
    val packageCount: Int,
    /** Beklenen ürün adedi (API: item_count) */
    val expectedItemCount: Int = 0,
    /** PO / sipariş no (API: order.po_no) */
    val poNumber: String = ""
)

data class ShipmentPackageUi(
    val id: String,
    val shipmentId: String,
    val packageNo: String,
    val model: String?,
    val size: String?,
    val barcode: String?,
    val quality: String?,
    val color: String?,
    /** Total tag reads attributed to this package */
    val readCount: Int,
    /** API wrong_format_count */
    val wrongFormatCount: Int = 0,
    /** extra_alarm_list size from API */
    val extraAlarmCount: Int = 0,
    /** `true`: hatalı ürün (Inditex QR bayrağı); paket satırı vurgulanır. */
    val inditexQrFlag: Boolean = false,
    val epcs: List<String>,
    /** Future API fields (key-value) shown in detail / expandable */
    val extraFields: Map<String, String> = emptyMap()
)
