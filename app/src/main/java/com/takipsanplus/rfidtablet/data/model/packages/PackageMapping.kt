package com.takipsanplus.rfidtablet.data.model.packages

import com.takipsanplus.rfidtablet.data.model.ShipmentPackageUi

fun PackageData.toShipmentPackageUi(shipmentKey: String): ShipmentPackageUi {
    val epcList = items.mapNotNull { it.epc?.trim()?.takeIf { e -> e.isNotEmpty() } }
    val readCount = when {
        epcList.isNotEmpty() -> epcList.size
        normalTagCount + securityTagCount > 0 -> normalTagCount + securityTagCount
        else -> itemsCount?.toIntOrNull() ?: 0
    }
    val first = items.firstOrNull()
    val extra = buildMap {
        put("package_id", id.toString())
        wrongFormatCount.takeIf { it > 0 }?.let { put("wrong_format", it.toString()) }
        normalTagCount.takeIf { it > 0 }?.let { put("normal_tag", it.toString()) }
        securityTagCount.takeIf { it > 0 }?.let { put("security_tag", it.toString()) }
        packageTrueStatus?.let { put("packageTrueStatus", it.toString()) }
        targetQuantity?.let { put("target_quantity", it.toString()) }
        boxTypeId?.let { put("box_type_id", it) }
        loadType?.let { put("load_type", it) }
        detailsMap?.forEach { (k, v) -> put("detail_$k", v.toString()) }
    }
    return ShipmentPackageUi(
        id = id.toString(),
        shipmentId = shipmentKey,
        packageNo = "$packageNo",
        model = model ?: first?.model,
        size = size ?: first?.size,
        barcode = barcode,
        quality = first?.quality,
        color = first?.color,
        readCount = readCount,
        wrongFormatCount = wrongFormatCount,
        extraAlarmCount = extraAlarmList?.size ?: 0,
        inditexQrFlag = inditexQrFlag == true,
        epcs = epcList,
        extraFields = extra
    )
}
