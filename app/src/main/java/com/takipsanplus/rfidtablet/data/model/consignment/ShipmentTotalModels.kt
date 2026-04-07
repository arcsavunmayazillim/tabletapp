package com.takipsanplus.rfidtablet.data.model.consignment

/** read/getSizeTotalsByConsignment satırı */
data class SizeQuantity(
    val size: String = "",
    val quantity: Int = 0
)

data class ShipmentTotalResponse(
    val status: String = "",
    val data: List<SizeQuantity>? = null,
    val errorMessage: String? = null
)
