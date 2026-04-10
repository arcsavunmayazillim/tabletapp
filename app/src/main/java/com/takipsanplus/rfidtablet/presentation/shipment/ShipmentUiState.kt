package com.takipsanplus.rfidtablet.presentation.shipment

import com.takipsanplus.rfidtablet.data.model.consignment.ConsigneeData
import com.takipsanplus.rfidtablet.data.model.consignment.SizeQuantity
import com.takipsanplus.rfidtablet.data.model.packages.FindPackageData
import com.takipsanplus.rfidtablet.data.model.ShipmentPackageUi
import com.takipsanplus.rfidtablet.data.model.ShipmentSummaryUi

sealed interface SizeTotalsDialogState {
    data object Hidden : SizeTotalsDialogState
    data object Loading : SizeTotalsDialogState
    data class Success(val rows: List<SizeQuantity>) : SizeTotalsDialogState
    data class Failure(val message: String) : SizeTotalsDialogState
}

sealed interface ListLoadErrorKey {
    data object Session : ListLoadErrorKey
    data class Raw(val message: String) : ListLoadErrorKey
}

sealed interface FindPackageDialogState {
    data object Hidden : FindPackageDialogState
    data class Found(val data: FindPackageData) : FindPackageDialogState
    data class Error(val message: String) : FindPackageDialogState
}

data class ShipmentUiState(
    val shipments: List<ShipmentSummaryUi> = emptyList(),
    val selectedShipmentId: String? = null,
    val packages: List<ShipmentPackageUi> = emptyList(),
    val expandedPackageIds: Set<String> = emptySet(),
    val selectedPackageIds: Set<String> = emptySet(),
    val isScanning: Boolean = false,
    /** RFID okuma sadece EPC → findPackage API (toplu paket penceresi yok). */
    val isFindPackageLookupActive: Boolean = false,
    /** Unique EPCs collected in the current packet-close window (from reader settings). */
    val currentBatchEpcCount: Int = 0,
    val packetCloseSeconds: Int = 5,
    val isSubmittingBatch: Boolean = false,
    val shipmentError: String? = null,
    val isLoadingShipments: Boolean = false,
    val listLoadErrorKey: ListLoadErrorKey? = null,
    val isLoadingPackages: Boolean = false,
    val dialog: ShipmentDialog = ShipmentDialog.None,
    /** Incremented to trigger one Google Code Scanner session (Compose LaunchedEffect). */
    val qrScanRequestId: Long = 0L,
    /** Non-null: show confirm dialog with scanned QR payload before EPC read. */
    val pendingQrRaw: String? = null,
    val sizeTotalsDialog: SizeTotalsDialogState = SizeTotalsDialogState.Hidden,
    /** Server `errorMessage` after add package (batch); shown in a warning dialog. */
    val addPackageWarningMessage: String? = null,
    val findPackageDialog: FindPackageDialogState = FindPackageDialogState.Hidden,
    /** Incremented on successful creation of a new shipment to trigger auto-navigation to read screen. */
    val successfulCreationRequestId: Long = 0L
)

sealed interface ShipmentDialog {
    data object None : ShipmentDialog
    data class NewShipment(
        val draftName: String = "",
        val draftExpectedCount: String = "",
        val draftDeliveryDate: String = "",
        val consignees: List<ConsigneeData> = emptyList(),
        val isLoadingConsignees: Boolean = false,
        val consigneesLoadError: String? = null,
        val selectedConsigneeId: Int? = null,
        val isSubmitting: Boolean = false
    ) : ShipmentDialog
    data class DeletePackages(val packageIds: List<String>) : ShipmentDialog
    data class MergePackages(val packageIds: List<String>) : ShipmentDialog
    data class CloseConsignment(val isSubmitting: Boolean = false) : ShipmentDialog
    data class EditShipment(
        val draftName: String = "",
        val draftExpectedCount: String = "",
        val draftDeliveryDate: String = "",
        val isSubmitting: Boolean = false
    ) : ShipmentDialog
}
