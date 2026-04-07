package com.takipsanplus.rfidtablet.data.model.consignment

import com.takipsanplus.rfidtablet.data.model.ShipmentSummaryUi

fun ConsignmentData.consignmentKey(): String {
    if (recordId > 0) return recordId.toString()
    return id.toString()
}

fun ConsignmentData.toShipmentSummaryUi(packageCountLocal: Int): ShipmentSummaryUi {
    val o = order
    val displayName = name.orEmpty()
        .ifBlank { o?.name.orEmpty() }
        .ifBlank { o?.orderCode.orEmpty() }
    val date = deliveryDate.orEmpty()
        .ifBlank { createdAt.orEmpty().take(10) }
    val resolvedOrderId = when {
        orderId > 0 -> orderId
        o?.id != null && o.id > 0 -> o.id
        else -> 0
    }
    return ShipmentSummaryUi(
        id = consignmentKey(),
        consignmentRemoteId = id,
        orderRemoteId = resolvedOrderId,
        name = displayName,
        createdAtLabel = date,
        packageCount = packageCountLocal,
        expectedItemCount = itemCount,
        poNumber = o?.poNo.orEmpty()
    )
}
