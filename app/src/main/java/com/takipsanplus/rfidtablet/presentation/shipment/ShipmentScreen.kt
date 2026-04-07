package com.takipsanplus.rfidtablet.presentation.shipment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.data.bluetooth.BluetoothConnectionController
import com.takipsanplus.rfidtablet.data.bluetooth.BluetoothConnectionState
import com.takipsanplus.rfidtablet.data.model.consignment.SizeQuantity
import com.takipsanplus.rfidtablet.data.model.ShipmentPackageUi
import com.takipsanplus.rfidtablet.data.model.ShipmentSummaryUi
import com.takipsanplus.rfidtablet.data.model.packages.FindPackageData
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.PremiumScreenBackdrop
import com.takipsanplus.rfidtablet.presentation.common.findActivity
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.common.scanQrWithGoogleCodeScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveInk
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveMuted
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CardLine = Color(0xFFE2E8F0)
private val CardBg = Color(0xFFFCFCFD)
/** inditex_qr_flag: hatalı ürün uyarısı — paket satırı arka planı */
private val PackageInditexQrErrorBg = Color(0xFFFFF1F2)
private val PackageInditexQrErrorBorder = Color(0xFFFECACA)
private val PanelBg = Color(0xFFF8FAFC)
private val PanelStroke = Color(0xFFE2E8F0)
private val AccentGlow = PrimaryBlue.copy(alpha = 0.12f)
private val ScanStatBg = Color(0xFFEFF6FF)

@Composable
fun ShipmentScreen(
    uiState: ShipmentUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    onSelectShipment: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onTogglePackageSelect: (String) -> Unit,
    onClearSelection: () -> Unit,
    onNewShipment: () -> Unit,
    onShowCloseConsignment: () -> Unit,
    onShowDeletePackage: (String) -> Unit,
    onShowDeleteSelected: () -> Unit,
    onSelectAllVisiblePackages: (List<String>) -> Unit,
    onShowMerge: () -> Unit,
    onToggleScan: () -> Unit,
    onDismissShipmentError: () -> Unit,
    onDismissDialog: () -> Unit,
    onRetryConsignments: () -> Unit,
    onClearListLoadError: () -> Unit,
    onUpdateNewShipmentDraftName: (String) -> Unit,
    onUpdateNewShipmentDraftExpectedCount: (String) -> Unit,
    onUpdateNewShipmentDraftDeliveryDate: (String) -> Unit,
    onUpdateNewShipmentSelectedConsigneeId: (Int) -> Unit,
    onConfirmNew: () -> Unit,
    onConfirmDelete: () -> Unit,
    onConfirmMerge: () -> Unit,
    onConfirmCloseConsignment: () -> Unit,
    onQrScannerFinished: (String?) -> Unit,
    onConfirmPendingQr: () -> Unit,
    onDismissPendingQrRescan: () -> Unit,
    onShowSizeTotalsBreakdown: () -> Unit,
    onDismissSizeTotalsDialog: () -> Unit,
    onDismissAddPackageWarning: () -> Unit,
    onDismissFindPackageDialog: () -> Unit,
    onFindPackageByTag: () -> Unit
) {
    val context = LocalContext.current
    var qrScanBusy by remember { mutableStateOf(false) }
    val latestPendingQr by rememberUpdatedState(uiState.pendingQrRaw)

    LaunchedEffect(uiState.qrScanRequestId) {
        if (uiState.qrScanRequestId == 0L) return@LaunchedEffect
        delay(160)
        if (!latestPendingQr.isNullOrBlank()) return@LaunchedEffect
        val activity = context.findActivity()
        if (activity == null) {
            onQrScannerFinished(null)
            return@LaunchedEffect
        }
        qrScanBusy = true
        try {
            val raw = withContext(Dispatchers.Main) {
                scanQrWithGoogleCodeScanner(activity)
            }
            onQrScannerFinished(raw)
        } finally {
            qrScanBusy = false
        }
    }

    val selected = uiState.shipments.find { it.id == uiState.selectedShipmentId }
    val canCloseConsignment =
        selected != null && selected.consignmentRemoteId > 0
    val bluetoothState by BluetoothConnectionController.state.collectAsState()
    val isBluetoothConnected = bluetoothState is BluetoothConnectionState.Connected
    val scanStartEnabled =
        !qrScanBusy && uiState.pendingQrRaw == null && isBluetoothConnected
    val canFindPackage =
        selected != null &&
            selected.consignmentRemoteId > 0 &&
            isBluetoothConnected &&
            uiState.pendingQrRaw == null &&
            !qrScanBusy

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        PremiumScreenBackdrop(Modifier.fillMaxSize())

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShipmentListPane(
                language = language,
                shipments = uiState.shipments,
                selectedId = uiState.selectedShipmentId,
                isLoading = uiState.isLoadingShipments,
                listLoadErrorKey = uiState.listLoadErrorKey,
                onRetry = onRetryConsignments,
                onDismissListError = onClearListLoadError,
                onBack = onBack,
                onSelect = onSelectShipment,
                onNew = onNewShipment,
                onCloseConsignment = onShowCloseConsignment,
                canCloseConsignment = canCloseConsignment,
                isScanning = uiState.isScanning,
                currentBatchEpcCount = uiState.currentBatchEpcCount,
                isSubmittingBatch = uiState.isSubmittingBatch,
                onToggleScan = onToggleScan,
                scanStartEnabled = scanStartEnabled,
                canFindPackage = canFindPackage,
                isFindPackageLookupActive = uiState.isFindPackageLookupActive,
                onFindPackageByTag = onFindPackageByTag,
                modifier = Modifier
                    .weight(0.36f)
                    .fillMaxHeight()
            )

            ShipmentDetailPane(
                language = language,
                selected = selected,
                packages = uiState.packages,
                expandedIds = uiState.expandedPackageIds,
                selectedIds = uiState.selectedPackageIds,
                isLoadingPackages = uiState.isLoadingPackages,
                onToggleExpand = onToggleExpand,
                onTogglePackageSelect = onTogglePackageSelect,
                onClearSelection = onClearSelection,
                onShowDeletePackage = onShowDeletePackage,
                onShowDeleteSelected = onShowDeleteSelected,
                onSelectAllVisiblePackages = onSelectAllVisiblePackages,
                visiblePackageIds = uiState.packages.map { it.id },
                onShowMerge = onShowMerge,
                canSizeTotalsBreakdown = (selected?.consignmentRemoteId ?: 0) > 0,
                onShowSizeTotalsBreakdown = onShowSizeTotalsBreakdown,
                modifier = Modifier
                    .weight(0.64f)
                    .fillMaxHeight()
            )
        }

        ShipmentDialogs(
            language = language,
            dialog = uiState.dialog,
            onDismiss = onDismissDialog,
            onNewShipmentDraftName = onUpdateNewShipmentDraftName,
            onNewShipmentDraftExpectedCount = onUpdateNewShipmentDraftExpectedCount,
            onNewShipmentDraftDeliveryDate = onUpdateNewShipmentDraftDeliveryDate,
            onNewShipmentSelectedConsigneeId = onUpdateNewShipmentSelectedConsigneeId,
            onConfirmNew = onConfirmNew,
            onConfirmDelete = onConfirmDelete,
            onConfirmMerge = onConfirmMerge,
            onConfirmCloseConsignment = onConfirmCloseConsignment
        )

        ShipmentApiMessageDialog(
            language = language,
            messageKey = uiState.shipmentError,
            onDismiss = onDismissShipmentError
        )

        ShipmentQrConfirmDialog(
            language = language,
            rawPayload = uiState.pendingQrRaw,
            onConfirm = onConfirmPendingQr,
            onDismissRescan = onDismissPendingQrRescan
        )

        ShipmentSizeTotalsDialog(
            language = language,
            state = uiState.sizeTotalsDialog,
            onDismiss = onDismissSizeTotalsDialog
        )

        ShipmentAddPackageWarningDialog(
            language = language,
            message = uiState.addPackageWarningMessage,
            onDismiss = onDismissAddPackageWarning
        )

        ShipmentFindPackageDialog(
            language = language,
            state = uiState.findPackageDialog,
            onDismiss = onDismissFindPackageDialog
        )
    }
}

@Composable
private fun ShipmentListPane(
    language: AppLanguage,
    shipments: List<ShipmentSummaryUi>,
    selectedId: String?,
    isLoading: Boolean,
    listLoadErrorKey: ListLoadErrorKey?,
    onRetry: () -> Unit,
    onDismissListError: () -> Unit,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    onNew: () -> Unit,
    onCloseConsignment: () -> Unit,
    canCloseConsignment: Boolean,
    isScanning: Boolean,
    currentBatchEpcCount: Int,
    isSubmittingBatch: Boolean,
    onToggleScan: () -> Unit,
    scanStartEnabled: Boolean,
    canFindPackage: Boolean,
    isFindPackageLookupActive: Boolean,
    onFindPackageByTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredShipments = remember(shipments, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) shipments
        else shipments.filter { it.name.contains(q, ignoreCase = true) }
    }
    val canScan = selectedId != null
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(PanelBg)
            .border(1.dp, PanelStroke, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = ExecutiveInk)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizedString(R.string.shipment_title, language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ExecutiveInk
                )
                Text(
                    text = localizedString(R.string.module_shipment_subtitle, language),
                    style = MaterialTheme.typography.labelSmall,
                    color = ExecutiveMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNew,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizedString(R.string.shipment_new, language),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            IconButton(
                onClick = onCloseConsignment,
                enabled = canCloseConsignment,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (canCloseConsignment) PrimaryBlue.copy(alpha = 0.12f)
                        else ExecutiveMuted.copy(alpha = 0.2f)
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = localizedString(R.string.shipment_close_consignment_cd, language),
                    tint = if (canCloseConsignment) PrimaryBlue else ExecutiveMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        listLoadErrorKey?.let { key ->
            val msg = when (key) {
                is ListLoadErrorKey.Session -> localizedString(R.string.shipment_error_session, language)
                is ListLoadErrorKey.Raw -> key.message
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF1F2))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRetry) {
                    Text(localizedString(R.string.shipment_retry, language))
                }
                TextButton(onClick = onDismissListError) {
                    Text(localizedString(R.string.shipment_error_dismiss, language))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            shipments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = localizedString(R.string.shipment_list_empty, language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExecutiveMuted
                    )
                }
            }
            else -> {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            localizedString(R.string.shipment_search_hint, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExecutiveMuted
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = ExecutiveMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = null, tint = ExecutiveMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardLine,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (filteredShipments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localizedString(R.string.shipment_search_no_results, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExecutiveMuted
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(filteredShipments, key = { it.id }) { s ->
                            ShipmentListCard(
                                language = language,
                                item = s,
                                selected = s.id == selectedId,
                                onClick = { onSelect(s.id) }
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ScanRow(
            language = language,
            isScanning = isScanning,
            currentBatchEpcCount = currentBatchEpcCount,
            isSubmittingBatch = isSubmittingBatch,
            onToggleScan = onToggleScan,
            enabled = canScan,
            startEnabled = scanStartEnabled,
            canFindPackage = canFindPackage,
            isFindPackageLookupActive = isFindPackageLookupActive,
            onFindPackageByTag = onFindPackageByTag
        )
    }
}

@Composable
private fun ShipmentListCard(
    language: AppLanguage,
    item: ShipmentSummaryUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) PrimaryBlue.copy(alpha = 0.55f) else CardLine
    val bg = if (selected) PrimaryBlue.copy(alpha = 0.06f) else CardBg
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) PrimaryBlue else CardLine.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = ExecutiveInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.createdAtLabel,
                style = MaterialTheme.typography.labelSmall,
                color = ExecutiveMuted
            )
        }
    }
}

@Composable
private fun ShipmentDetailPane(
    language: AppLanguage,
    selected: ShipmentSummaryUi?,
    packages: List<ShipmentPackageUi>,
    expandedIds: Set<String>,
    selectedIds: Set<String>,
    isLoadingPackages: Boolean,
    onToggleExpand: (String) -> Unit,
    onTogglePackageSelect: (String) -> Unit,
    onClearSelection: () -> Unit,
    onShowDeletePackage: (String) -> Unit,
    onShowDeleteSelected: () -> Unit,
    onSelectAllVisiblePackages: (List<String>) -> Unit,
    visiblePackageIds: List<String>,
    onShowMerge: () -> Unit,
    canSizeTotalsBreakdown: Boolean,
    onShowSizeTotalsBreakdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(PanelBg)
            .border(1.dp, PanelStroke, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        if (selected == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = localizedString(R.string.shipment_select_hint, language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ExecutiveMuted
                )
            }
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PrimaryBlue)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selected.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ExecutiveInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.ulogslogo),
                contentDescription = localizedString(R.string.shipment_ulogs_logo_cd, language),
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(max = 140.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoadingPackages) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryBlue,
                trackColor = CardLine
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (packages.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString(R.string.shipment_packages_list_title, language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ExecutiveInk,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { onSelectAllVisiblePackages(visiblePackageIds) },
                    enabled = visiblePackageIds.isNotEmpty()
                ) {
                    Icon(Icons.Filled.SelectAll, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localizedString(R.string.shipment_select_all, language))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (selectedIds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentGlow)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString(R.string.shipment_selected_count, language, selectedIds.size),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearSelection) {
                    Text(localizedString(R.string.shipment_clear_selection, language))
                }
                OutlinedButton(
                    onClick = onShowMerge,
                    enabled = selectedIds.size >= 2,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localizedString(R.string.shipment_merge, language))
                }
                Button(
                    onClick = onShowDeleteSelected,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localizedString(R.string.shipment_delete_selected, language))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.takipsan_logo_w),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .alpha(0.55f),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )
            if (packages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = localizedString(R.string.shipment_no_packages, language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExecutiveMuted
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(packages, key = { it.id }) { pkg ->
                        PackageRowCompact(
                            language = language,
                            pkg = pkg,
                            expanded = expandedIds.contains(pkg.id),
                            selected = selectedIds.contains(pkg.id),
                            onToggleExpand = { onToggleExpand(pkg.id) },
                            onToggleSelect = { onTogglePackageSelect(pkg.id) },
                            onDelete = { onShowDeletePackage(pkg.id) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ShipmentReadTotalsStrip(
            language = language,
            totalRead = packages.sumOf { it.readCount },
            totalWrongFormat = packages.sumOf { it.wrongFormatCount },
            totalExtraAlarm = packages.sumOf { it.extraAlarmCount },
            canShowSizeTotalsBreakdown = canSizeTotalsBreakdown,
            onTotalReadClick = onShowSizeTotalsBreakdown
        )
    }
}

@Composable
private fun ShipmentReadTotalsStrip(
    language: AppLanguage,
    totalRead: Int,
    totalWrongFormat: Int,
    totalExtraAlarm: Int,
    canShowSizeTotalsBreakdown: Boolean,
    onTotalReadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, CardLine, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = localizedString(R.string.shipment_read_totals_title, language),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = ExecutiveInk
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShipmentTotalStatCell(
                label = localizedString(R.string.shipment_stat_total_read, language),
                value = totalRead,
                valueColor = PrimaryBlue,
                modifier = Modifier.weight(1f),
                onClick = if (canShowSizeTotalsBreakdown) onTotalReadClick else null
            )
            ShipmentTotalStatCell(
                label = localizedString(R.string.shipment_stat_wrong_format, language),
                value = totalWrongFormat,
                valueColor = Color(0xFFEA580C),
                modifier = Modifier.weight(1f)
            )
            ShipmentTotalStatCell(
                label = localizedString(R.string.shipment_stat_extra_alarm, language),
                value = totalExtraAlarm,
                valueColor = Color(0xFFDC2626),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ShipmentTotalStatCell(
    label: String,
    value: Int,
    valueColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ScanStatBg)
            .border(1.dp, CardLine.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ExecutiveMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun ScanRow(
    language: AppLanguage,
    isScanning: Boolean,
    currentBatchEpcCount: Int,
    isSubmittingBatch: Boolean,
    onToggleScan: () -> Unit,
    enabled: Boolean,
    startEnabled: Boolean = true,
    canFindPackage: Boolean,
    isFindPackageLookupActive: Boolean,
    onFindPackageByTag: () -> Unit
) {
    val countSp = 42.sp
    val findEnabled = canFindPackage && !isSubmittingBatch
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, CardLine, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onToggleScan,
                    enabled = if (isScanning) enabled else enabled && startEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) Color(0xFFDC2626) else PrimaryBlue,
                        disabledContainerColor = ExecutiveMuted.copy(alpha = 0.35f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        if (isScanning) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isScanning) {
                            localizedString(R.string.shipment_scan_stop, language)
                        } else {
                            localizedString(R.string.shipment_scan_start, language)
                        },
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(
                    onClick = onFindPackageByTag,
                    enabled = findEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (findEnabled) {
                                if (isFindPackageLookupActive) PrimaryBlue.copy(alpha = 0.2f)
                                else PrimaryBlue.copy(alpha = 0.12f)
                            } else {
                                ExecutiveMuted.copy(alpha = 0.15f)
                            }
                        )
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = localizedString(R.string.shipment_find_package_cd, language),
                        tint = if (findEnabled) PrimaryBlue else ExecutiveMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 136.dp, minHeight = 96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isScanning) ScanStatBg else CardBg)
                    .border(
                        1.dp,
                        if (isScanning) PrimaryBlue.copy(alpha = 0.4f) else CardLine,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$currentBatchEpcCount",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = countSp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isScanning) PrimaryBlue else ExecutiveMuted,
                    maxLines = 1
                )
            }
        }
        if (isSubmittingBatch) {
            Text(
                text = localizedString(R.string.shipment_batch_submitting, language),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = PrimaryBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun packageNumberForDisplay(rawPackageNo: String): String =
    rawPackageNo.removePrefix("PK-").removePrefix("pk-").trim().ifBlank { rawPackageNo }

@Composable
private fun PackageRowCompact(
    language: AppLanguage,
    pkg: ShipmentPackageUi,
    expanded: Boolean,
    selected: Boolean,
    onToggleExpand: () -> Unit,
    onToggleSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val rowBg = when {
        pkg.inditexQrFlag -> PackageInditexQrErrorBg
        selected -> PrimaryBlue.copy(alpha = 0.07f)
        else -> CardBg
    }
    val rowBorder = when {
        selected -> PrimaryBlue.copy(alpha = 0.4f)
        pkg.inditexQrFlag -> PackageInditexQrErrorBorder
        else -> CardLine
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggleSelect() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizedString(
                        R.string.shipment_package_line,
                        language,
                        packageNumberForDisplay(pkg.packageNo)
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ExecutiveInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val sub = listOfNotNull(pkg.model, pkg.size).joinToString(" · ").ifBlank { null }
                Text(
                    text = sub ?: "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExecutiveMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pkg.readCount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    maxLines = 1
                )
            }
            IconButton(onClick = onToggleExpand, modifier = Modifier.size(40.dp)) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = ExecutiveMuted
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626))
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PanelBg)
                        .border(1.dp, PanelStroke, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    val detailFields = listOf(
                        R.string.shipment_model to pkg.model,
                        R.string.shipment_size to pkg.size,
                        R.string.shipment_barcode to pkg.barcode,
                        R.string.shipment_quality to pkg.quality,
                        R.string.shipment_color to pkg.color
                    )
                    detailFields.forEachIndexed { index, (labelRes, value) ->
                        PackageDetailLine(
                            language = language,
                            labelRes = labelRes,
                            value = value
                        )
                        if (index < detailFields.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                thickness = 1.dp,
                                color = CardLine.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageDetailLine(
    language: AppLanguage,
    labelRes: Int,
    value: String?
) {
    val display = value?.trim()?.takeIf { it.isNotEmpty() } ?: "—"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = localizedString(labelRes, language),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = ExecutiveMuted,
            modifier = Modifier.widthIn(max = 120.dp)
        )
        Text(
            text = display,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = ExecutiveInk,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ShipmentQrConfirmDialog(
    language: AppLanguage,
    rawPayload: String?,
    onConfirm: () -> Unit,
    onDismissRescan: () -> Unit
) {
    val raw = rawPayload ?: return
    val fields = remember(raw) { ShipmentQrPayloadParser.extractAiFields(raw) }
    val hasAnyParsed = remember(fields) {
        ShipmentQrPayloadParser.DISPLAY_AI_ORDER.any { ai ->
            fields[ai]?.isNotBlank() == true
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRescan,
        title = { Text(localizedString(R.string.shipment_qr_confirm_title, language)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = localizedString(R.string.shipment_qr_confirm_body, language),
                    style = MaterialTheme.typography.bodySmall,
                    color = ExecutiveMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                ShipmentQrParsedFieldsTable(language = language, fields = fields)
                if (!hasAnyParsed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizedString(R.string.shipment_qr_no_parsed_fields, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = ExecutiveMuted
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(localizedString(R.string.shipment_qr_confirm_ok, language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRescan) {
                Text(localizedString(R.string.shipment_qr_confirm_cancel, language))
            }
        }
    )
}

@Composable
private fun ShipmentQrParsedFieldsTable(
    language: AppLanguage,
    fields: Map<String, String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardLine, RoundedCornerShape(12.dp))
            .background(PanelBg)
    ) {
        ShipmentQrPayloadParser.DISPLAY_AI_ORDER.forEachIndexed { index, ai ->
            val labelRes = ShipmentQrPayloadParser.labelResForAi(ai) ?: return@forEachIndexed
            val value = fields[ai]?.trim()?.takeIf { it.isNotEmpty() } ?: "—"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString(labelRes, language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = ExecutiveMuted,
                    modifier = Modifier.widthIn(max = 140.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ExecutiveInk,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (index < ShipmentQrPayloadParser.DISPLAY_AI_ORDER.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = CardLine.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SizeTotalsDialogTitleBar(language: AppLanguage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(PrimaryBlue)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = localizedString(R.string.shipment_size_totals_dialog_title, language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = ExecutiveInk,
            letterSpacing = (-0.2).sp
        )
    }
}

@Composable
private fun SizeTotalsDialogDismissButton(language: AppLanguage, onDismiss: () -> Unit) {
    TextButton(
        onClick = onDismiss,
        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue)
    ) {
        Text(
            text = localizedString(R.string.shipment_error_dismiss, language),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SizeTotalsDataTable(language: AppLanguage, rows: List<SizeQuantity>) {
    val totalQty = rows.sumOf { it.quantity }
    val stripeB = Color(0xFFF1F5F9)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardLine, RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.14f),
                            Color(0xFFEFF6FF),
                            Color(0xFFF8FAFC)
                        ),
                        start = Offset.Zero,
                        end = Offset(900f, 0f)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizedString(R.string.shipment_size_totals_column_size, language).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                letterSpacing = 0.6.sp
            )
            Text(
                text = localizedString(R.string.shipment_size_totals_column_qty, language).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                letterSpacing = 0.6.sp
            )
        }
        HorizontalDivider(thickness = 1.dp, color = PrimaryBlue.copy(alpha = 0.12f))
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelBg)
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = localizedString(R.string.shipment_size_totals_empty, language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ExecutiveMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) Color.White else stripeB
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.size.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = ExecutiveInk,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryBlue.copy(alpha = 0.1f))
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${row.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 1.dp,
                        color = CardLine.copy(alpha = 0.65f)
                    )
                }
            }
            HorizontalDivider(thickness = 1.dp, color = CardLine)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = 0.08f),
                                Color(0xFFEFF6FF)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString(R.string.shipment_size_totals_total_label, language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExecutiveInk
                )
                Text(
                    text = "$totalQty",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
private fun ShipmentSizeTotalsDialog(
    language: AppLanguage,
    state: SizeTotalsDialogState,
    onDismiss: () -> Unit
) {
    val dialogShape = RoundedCornerShape(24.dp)
    when (state) {
        SizeTotalsDialogState.Hidden -> {}
        SizeTotalsDialogState.Loading -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = dialogShape,
                containerColor = Color.White,
                modifier = Modifier.widthIn(max = 440.dp),
                title = { SizeTotalsDialogTitleBar(language) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PanelBg)
                            .border(1.dp, CardLine.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                },
                confirmButton = { SizeTotalsDialogDismissButton(language, onDismiss) }
            )
        }
        is SizeTotalsDialogState.Success -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = dialogShape,
                containerColor = Color.White,
                modifier = Modifier.widthIn(max = 440.dp),
                title = { SizeTotalsDialogTitleBar(language) },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 440.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SizeTotalsDataTable(language = language, rows = state.rows)
                    }
                },
                confirmButton = { SizeTotalsDialogDismissButton(language, onDismiss) }
            )
        }
        is SizeTotalsDialogState.Failure -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = dialogShape,
                containerColor = Color.White,
                modifier = Modifier.widthIn(max = 440.dp),
                title = { SizeTotalsDialogTitleBar(language) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB91C1C),
                            lineHeight = 22.sp
                        )
                    }
                },
                confirmButton = { SizeTotalsDialogDismissButton(language, onDismiss) }
            )
        }
    }
}

@Composable
private fun ShipmentFindPackageDialog(
    language: AppLanguage,
    state: FindPackageDialogState,
    onDismiss: () -> Unit
) {
    when (state) {
        FindPackageDialogState.Hidden -> {}
        is FindPackageDialogState.Found -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(localizedString(R.string.shipment_find_package_dialog_title, language))
                },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        FindPackageDetailTable(language = language, data = state.data)
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(localizedString(R.string.shipment_error_dismiss, language))
                    }
                }
            )
        }
        is FindPackageDialogState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(localizedString(R.string.shipment_find_package_error_title, language))
                },
                text = {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFDC2626)
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(localizedString(R.string.shipment_error_dismiss, language))
                    }
                }
            )
        }
    }
}

@Composable
private fun FindPackageDetailTable(language: AppLanguage, data: FindPackageData) {
    val packageLabel = data.packageNo?.toString()?.let { packageNumberForDisplay(it) } ?: "—"
    val rows = listOf(
        R.string.shipment_find_package_column_package to packageLabel,
        R.string.shipment_model to (data.model?.trim()?.takeIf { it.isNotEmpty() } ?: "—"),
        R.string.shipment_size to (data.size?.trim()?.takeIf { it.isNotEmpty() } ?: "—"),
        R.string.shipment_barcode to (data.barcode?.trim()?.takeIf { it.isNotEmpty() } ?: "—")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardLine, RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        rows.forEachIndexed { index, (labelRes, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedString(labelRes, language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = ExecutiveMuted,
                    modifier = Modifier.widthIn(max = 140.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ExecutiveInk,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = CardLine.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ShipmentAddPackageWarningDialog(
    language: AppLanguage,
    message: String?,
    onDismiss: () -> Unit
) {
    val text = message?.trim()?.takeIf { it.isNotEmpty() } ?: return
    val warnBorder = Color(0xFFFED7AA)
    val warnBg = Color(0xFFFFF7ED)
    val warnIcon = Color(0xFFEA580C)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = warnIcon,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = localizedString(R.string.shipment_add_package_warning_title, language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ExecutiveInk
            )
        },
        text = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = warnBg,
                border = BorderStroke(1.dp, warnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ExecutiveInk,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString(R.string.shipment_error_dismiss, language))
            }
        }
    )
}

@Composable
private fun ShipmentApiMessageDialog(
    language: AppLanguage,
    messageKey: String?,
    onDismiss: () -> Unit
) {
    val err = messageKey ?: return
    val errText = when (err) {
        "validation" -> localizedString(R.string.shipment_error_new_validation, language)
        "session" -> localizedString(R.string.shipment_error_session, language)
        else -> err
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizedString(R.string.shipment_message_dialog_title, language)) },
        text = { Text(errText) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizedString(R.string.shipment_error_dismiss, language))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShipmentDialogs(
    language: AppLanguage,
    dialog: ShipmentDialog,
    onDismiss: () -> Unit,
    onNewShipmentDraftName: (String) -> Unit,
    onNewShipmentDraftExpectedCount: (String) -> Unit,
    onNewShipmentDraftDeliveryDate: (String) -> Unit,
    onNewShipmentSelectedConsigneeId: (Int) -> Unit,
    onConfirmNew: () -> Unit,
    onConfirmDelete: () -> Unit,
    onConfirmMerge: () -> Unit,
    onConfirmCloseConsignment: () -> Unit
) {
    when (dialog) {
        ShipmentDialog.None -> {}
        is ShipmentDialog.NewShipment -> {
            val scroll = rememberScrollState()
            var consigneeMenuExpanded by remember { mutableStateOf(false) }
            var showDatePicker by remember { mutableStateOf(false) }
            val initialMillis = remember(dialog.draftDeliveryDate) {
                parseShipmentDateToMillis(dialog.draftDeliveryDate) ?: System.currentTimeMillis()
            }
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
            Box {
                AlertDialog(
                    onDismissRequest = { if (!dialog.isSubmitting) onDismiss() },
                    title = { Text(localizedString(R.string.shipment_dialog_new_title, language)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scroll)
                        ) {
                            if (dialog.isLoadingConsignees) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = PrimaryBlue,
                                    trackColor = CardLine
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            dialog.consigneesLoadError?.let { key ->
                                Text(
                                    text = when (key) {
                                        "session" -> localizedString(R.string.shipment_error_session, language)
                                        else -> key
                                    },
                                    color = Color(0xFFDC2626),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            val consigneeFieldColors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = CardLine,
                                focusedLabelColor = PrimaryBlue,
                                cursorColor = PrimaryBlue
                            )
                            ExposedDropdownMenuBox(
                                expanded = consigneeMenuExpanded,
                                onExpandedChange = { if (!dialog.isSubmitting) consigneeMenuExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val selectedConsignee =
                                    dialog.consignees.find { it.id == dialog.selectedConsigneeId }
                                OutlinedTextField(
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    value = selectedConsignee?.name?.takeIf { it.isNotBlank() } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = !dialog.isSubmitting &&
                                        !dialog.isLoadingConsignees &&
                                        dialog.consignees.isNotEmpty(),
                                    singleLine = true,
                                    label = { Text(localizedString(R.string.shipment_brand_label, language)) },
                                    placeholder = {
                                        Text(localizedString(R.string.shipment_select_brand_placeholder, language))
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = consigneeMenuExpanded)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = consigneeFieldColors
                                )
                                DropdownMenu(
                                    expanded = consigneeMenuExpanded,
                                    onDismissRequest = { consigneeMenuExpanded = false }
                                ) {
                                    dialog.consignees.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c.name.ifBlank { "—" }) },
                                            onClick = {
                                                onNewShipmentSelectedConsigneeId(c.id)
                                                consigneeMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = dialog.draftName,
                                onValueChange = onNewShipmentDraftName,
                                enabled = !dialog.isSubmitting,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(localizedString(R.string.shipment_name_label, language)) },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = dialog.draftExpectedCount,
                                onValueChange = { v ->
                                    onNewShipmentDraftExpectedCount(v.filter { it.isDigit() }.take(9))
                                },
                                enabled = !dialog.isSubmitting,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(localizedString(R.string.shipment_expected_count_label, language)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = dialog.draftDeliveryDate,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !dialog.isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !dialog.isSubmitting) { showDatePicker = true },
                                label = { Text(localizedString(R.string.shipment_delivery_date_label, language)) },
                                placeholder = { Text(localizedString(R.string.shipment_date_hint, language)) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showDatePicker = true },
                                        enabled = !dialog.isSubmitting
                                    ) {
                                        Icon(Icons.Filled.Event, contentDescription = null, tint = PrimaryBlue)
                                    }
                                },
                                singleLine = true
                            )
                        }
                    },
                confirmButton = {
                    val canSubmitNew = !dialog.isSubmitting &&
                        !dialog.isLoadingConsignees &&
                        (dialog.selectedConsigneeId ?: 0) > 0
                    TextButton(
                        onClick = onConfirmNew,
                        enabled = canSubmitNew
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (dialog.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryBlue
                                )
                            }
                            Text(
                                text = if (dialog.isSubmitting) {
                                    localizedString(R.string.shipment_submitting, language)
                                } else {
                                    localizedString(R.string.shipment_confirm, language)
                                }
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !dialog.isSubmitting) {
                        Text(localizedString(R.string.shipment_cancel, language))
                    }
                }
                )
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { ms ->
                                        onNewShipmentDraftDeliveryDate(dateFormat.format(Date(ms)))
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text(localizedString(R.string.shipment_confirm, language))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(localizedString(R.string.shipment_cancel, language))
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
        }
        is ShipmentDialog.DeletePackages -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(localizedString(R.string.shipment_dialog_delete_title, language)) },
                text = {
                    Text(
                        localizedString(
                            R.string.shipment_dialog_delete_body_n,
                            language,
                            dialog.packageIds.size
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = onConfirmDelete) {
                        Text(localizedString(R.string.shipment_confirm, language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(localizedString(R.string.shipment_cancel, language))
                    }
                }
            )
        }
        is ShipmentDialog.MergePackages -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(localizedString(R.string.shipment_dialog_merge_title, language)) },
                text = {
                    Text(
                        localizedString(
                            R.string.shipment_dialog_merge_body_n,
                            language,
                            dialog.packageIds.size
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = onConfirmMerge) {
                        Text(localizedString(R.string.shipment_confirm, language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(localizedString(R.string.shipment_cancel, language))
                    }
                }
            )
        }
        is ShipmentDialog.CloseConsignment -> {
            AlertDialog(
                onDismissRequest = { if (!dialog.isSubmitting) onDismiss() },
                title = { Text(localizedString(R.string.shipment_dialog_close_consignment_title, language)) },
                text = { Text(localizedString(R.string.shipment_dialog_close_consignment_body, language)) },
                confirmButton = {
                    TextButton(
                        onClick = onConfirmCloseConsignment,
                        enabled = !dialog.isSubmitting
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (dialog.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryBlue
                                )
                            }
                            Text(
                                text = if (dialog.isSubmitting) {
                                    localizedString(R.string.shipment_submitting, language)
                                } else {
                                    localizedString(R.string.shipment_confirm, language)
                                }
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !dialog.isSubmitting) {
                        Text(localizedString(R.string.shipment_cancel, language))
                    }
                }
            )
        }
    }
}

private fun parseShipmentDateToMillis(ymd: String): Long? {
    if (ymd.isBlank()) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(ymd.trim())?.time
    } catch (_: Exception) {
        null
    }
}
