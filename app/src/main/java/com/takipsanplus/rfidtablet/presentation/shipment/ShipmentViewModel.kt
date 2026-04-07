package com.takipsanplus.rfidtablet.presentation.shipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takipsanplus.rfidtablet.data.bluetooth.BluetoothConnectionController
import com.takipsanplus.rfidtablet.data.bluetooth.BluetoothConnectionState
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import com.takipsanplus.rfidtablet.data.model.ShipmentPackageUi
import com.takipsanplus.rfidtablet.data.model.ShipmentSummaryUi
import com.takipsanplus.rfidtablet.data.model.consignment.AddConsignmentZaraRequestModel
import com.takipsanplus.rfidtablet.data.model.consignment.ConsignmentData
import com.takipsanplus.rfidtablet.data.model.consignment.ZaraDataList
import com.takipsanplus.rfidtablet.data.model.consignment.consignmentKey
import com.takipsanplus.rfidtablet.data.model.consignment.toShipmentSummaryUi
import com.takipsanplus.rfidtablet.data.model.packages.CombineDataList
import com.takipsanplus.rfidtablet.data.model.packages.CombinePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackageData
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackageDataList
import com.takipsanplus.rfidtablet.data.model.packages.DeletePackageRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.PackageEpcData
import com.takipsanplus.rfidtablet.data.model.packages.AddPackageZaraResult
import com.takipsanplus.rfidtablet.data.model.packages.PackageZaraDataList
import com.takipsanplus.rfidtablet.data.model.packages.PackageZaraRequestModel
import com.takipsanplus.rfidtablet.data.model.packages.toShipmentPackageUi
import com.takipsanplus.rfidtablet.data.repository.ConsignmentRepository
import com.takipsanplus.rfidtablet.data.repository.PackagesRepository
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ShipmentViewModel(
    private val userPreferences: UserPreferences,
    private val consignmentRepository: ConsignmentRepository,
    private val packagesRepository: PackagesRepository
) : ViewModel() {

    private companion object {
        private const val TAG_QR = "ShipmentQR"
    }

    private val _state = MutableStateFlow(ShipmentUiState())
    val state: StateFlow<ShipmentUiState> = _state.asStateFlow()

    private var shipmentStore: MutableMap<String, ShipmentSummaryUi> = linkedMapOf()
    private var packageStore: MutableMap<String, MutableList<ShipmentPackageUi>> = linkedMapOf()

    private val windowBuffer = LinkedHashSet<String>()
    /** Son EPC sonrası paket kapatma süresi (idle) için zamanlayıcı; flush ayrı coroutine'te. */
    private var packetIdleJob: Job? = null
    private val flushMutex = Mutex()

    /** Barcode/QR text applied to package API for the current EPC session (after QR confirm). */
    private var sessionBarcodeForPackages: String = ""

    /** Sunucuda veya bu oturumda zaten kayıtlı EPC anahtarları (trim + uppercase); tekrar gönderilmez. */
    private val committedEpcKeysByShipment = ConcurrentHashMap<String, MutableSet<String>>()
    private val epcRegistryLock = Any()
    private val findPackageMutex = Mutex()

    init {
        viewModelScope.launch {
            BluetoothConnectionController.epcEvents.collect { epc ->
                if (!_state.value.isScanning) return@collect
                val sid = _state.value.selectedShipmentId ?: return@collect
                val key = normalizeEpcKey(epc)
                if (key.isBlank()) return@collect
                if (_state.value.isFindPackageLookupActive) {
                    processFindPackageEpc(sid, key)
                    return@collect
                }
                val blocked = synchronized(epcRegistryLock) {
                    val set = committedEpcKeysByShipment[sid] ?: run {
                        rebuildCommittedEpcKeysLocked(sid)
                        committedEpcKeysByShipment[sid]!!
                    }
                    set
                }
                if (key in blocked) return@collect
                val count = synchronized(windowBuffer) {
                    windowBuffer.add(key)
                    windowBuffer.size
                }
                _state.update { it.copy(currentBatchEpcCount = count) }
                schedulePacketIdleFlush()
            }
        }
        viewModelScope.launch {
            BluetoothConnectionController.barcodeEvents.collect { raw ->
                if (!readerSettings().barcodeEnabled) return@collect
                if (_state.value.isScanning) return@collect
                if (_state.value.selectedShipmentId == null) return@collect
                if (raw.isBlank()) return@collect
                Log.d(TAG_QR, "device Barcode -> QR dialog (Start gerekmez)")
                onQrScannerFinished(raw)
            }
        }
        refreshConsignments()
    }

    private fun normalizeEpcKey(raw: String): String = raw.trim().uppercase(Locale.ROOT)

    /** [epcRegistryLock] dışında çağırma. */
    private fun rebuildCommittedEpcKeysLocked(sid: String) {
        val keys = packageStore[sid]
            ?.asSequence()
            ?.flatMap { it.epcs.asSequence() }
            ?.map { normalizeEpcKey(it) }
            ?.filter { it.isNotBlank() }
            ?.toMutableSet()
            ?: mutableSetOf()
        committedEpcKeysByShipment[sid] = keys
    }

    private fun rebuildCommittedEpcKeys(sid: String) {
        synchronized(epcRegistryLock) {
            rebuildCommittedEpcKeysLocked(sid)
        }
    }

    private fun syncEpcRegistryWithPackageStore() {
        synchronized(epcRegistryLock) {
            committedEpcKeysByShipment.keys.retainAll(packageStore.keys)
            packageStore.keys.forEach { rebuildCommittedEpcKeysLocked(it) }
        }
    }

    override fun onCleared() {
        stopScanningInternal(sendStopCommand = true)
        super.onCleared()
    }

    fun refreshConsignments() {
        viewModelScope.launch {
            val token = userPreferences.getSessionToken()
            val companyId = userPreferences.getSessionCompanyId()
            if (token == null || companyId == null) {
                _state.update {
                    it.copy(
                        isLoadingShipments = false,
                        listLoadErrorKey = ListLoadErrorKey.Session,
                        shipments = emptyList(),
                        selectedShipmentId = null,
                        packages = emptyList()
                    )
                }
                return@launch
            }
            _state.update { it.copy(isLoadingShipments = true, listLoadErrorKey = null) }
            consignmentRepository.getConsignments(token, companyId)
                .onSuccess { rows -> applyConsignmentRows(rows) }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoadingShipments = false,
                            listLoadErrorKey = ListLoadErrorKey.Raw(e.message ?: "load_failed")
                        )
                    }
                }
        }
    }

    fun clearListLoadError() {
        _state.update { it.copy(listLoadErrorKey = null) }
    }

    private fun applyConsignmentRows(rows: List<ConsignmentData>) {
        val newShipmentStore = linkedMapOf<String, ShipmentSummaryUi>()
        val preservedPkg = packageStore.toMutableMap()
        for (row in rows) {
            val sid = row.consignmentKey()
            val localPkgs = preservedPkg[sid] ?: mutableListOf()
            val ui = row.toShipmentSummaryUi(localPkgs.size)
            newShipmentStore[sid] = ui
            preservedPkg[sid] = localPkgs
        }
        shipmentStore = newShipmentStore
        packageStore = preservedPkg.filterKeys { newShipmentStore.containsKey(it) }.toMutableMap()
        syncEpcRegistryWithPackageStore()
        syncCounts()
        val sel = _state.value.selectedShipmentId
        val nextSel = when {
            sel != null && shipmentStore.containsKey(sel) -> sel
            else -> shipmentStore.keys.firstOrNull()
        }
        _state.update {
            it.copy(
                isLoadingShipments = false,
                listLoadErrorKey = null,
                shipments = shipmentStore.values.toList(),
                selectedShipmentId = nextSel,
                packages = nextSel?.let { id -> packageStore[id].orEmpty().toList() } ?: emptyList()
            )
        }
        nextSel?.let { loadPackages(it) }
    }

    private fun loadPackages(sid: String) {
        val token = userPreferences.getSessionToken() ?: return
        val remoteId = shipmentStore[sid]?.consignmentRemoteId ?: return
        if (remoteId <= 0) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPackages = true) }
            packagesRepository.getPackages(token, remoteId)
                .onSuccess { rows ->
                    val apiPkgs = rows.map { it.toShipmentPackageUi(sid) }
                    val old = packageStore[sid].orEmpty()
                    val localOnly = old.filter { it.extraFields["source"] == "local_scan" }
                    val merged = (apiPkgs + localOnly).distinctBy { it.id }
                    packageStore[sid] = merged.toMutableList()
                    rebuildCommittedEpcKeys(sid)
                    syncCounts()
                    _state.update {
                        it.copy(
                            isLoadingPackages = false,
                            shipments = shipmentStore.values.toList(),
                            packages = if (it.selectedShipmentId == sid) merged else it.packages
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoadingPackages = false,
                            shipmentError = e.message ?: "packages_failed"
                        )
                    }
                }
        }
    }

    private fun readerSettings(): UserPreferences.ReaderSettings {
        return userPreferences.getReaderSettings(
            default = UserPreferences.ReaderSettings(
                ant1 = 0,
                ant2 = 0,
                ant3 = 0,
                ant4 = 0,
                packetCloseTimeout = 5,
                weightEnabled = true,
                barcodeEnabled = false
            )
        )
    }

    /** QR-oncesi modunda: EPC oturumunu kapat; sonraki Start yine QR ister. */
    private fun interruptEpcScanForBarcodeCycleIfNeeded() {
        if (!readerSettings().barcodeEnabled) return
        stopScanningInternal(sendStopCommand = true)
    }

    private fun syncCounts() {
        shipmentStore.keys.forEach { sid ->
            val list = packageStore[sid].orEmpty()
            val summary = shipmentStore[sid] ?: return@forEach
            shipmentStore[sid] = summary.copy(packageCount = list.size)
        }
    }

    /** Sunucuya gidecek 1 tabanlı paket no: bu sevkiyatta şu an kaç paket varsa +1 (0 paket → 1). */
    private fun nextPackageNumberForShipment(sid: String): Int {
        val count = packageStore[sid]?.size ?: 0
        return count + 1
    }

    fun clearShipmentError() {
        _state.update { it.copy(shipmentError = null) }
    }

    fun dismissAddPackageWarning() {
        _state.update { it.copy(addPackageWarningMessage = null) }
    }

    fun showSizeTotalsBreakdown() {
        val sid = _state.value.selectedShipmentId ?: return
        val remoteId = shipmentStore[sid]?.consignmentRemoteId ?: return
        if (remoteId <= 0) return
        val token = userPreferences.getSessionToken() ?: return
        _state.update { it.copy(sizeTotalsDialog = SizeTotalsDialogState.Loading) }
        viewModelScope.launch {
            consignmentRepository.getSizeTotalsByConsignment(token, remoteId.toString())
                .onSuccess { rows ->
                    _state.update { it.copy(sizeTotalsDialog = SizeTotalsDialogState.Success(rows)) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(sizeTotalsDialog = SizeTotalsDialogState.Failure(e.message ?: "size_totals_failed"))
                    }
                }
        }
    }

    fun dismissSizeTotalsDialog() {
        _state.update { it.copy(sizeTotalsDialog = SizeTotalsDialogState.Hidden) }
    }

    fun dismissFindPackageDialog() {
        _state.update { it.copy(findPackageDialog = FindPackageDialogState.Hidden) }
    }

    fun startFindPackageLookup() {
        if (BluetoothConnectionController.state.value !is BluetoothConnectionState.Connected) return
        val sid = _state.value.selectedShipmentId ?: return
        val summary = shipmentStore[sid] ?: return
        if (summary.consignmentRemoteId <= 0) return
        if (_state.value.isFindPackageLookupActive) {
            stopScanningInternal(sendStopCommand = true)
            return
        }
        if (_state.value.isScanning) {
            stopScanningInternal(sendStopCommand = true)
        }
        startFindPackageLookupInternal()
    }

    private fun startFindPackageLookupInternal() {
        val sid = _state.value.selectedShipmentId ?: return
        shipmentStore[sid] ?: return
        val ws = readerSettings()
        val weightFlag = if (ws.weightEnabled) "1" else "0"
        packetIdleJob?.cancel()
        packetIdleJob = null
        synchronized(windowBuffer) { windowBuffer.clear() }
        BluetoothConnectionController.sendCommand("""{"Status":["Start","$weightFlag"]}""")
        _state.update {
            it.copy(
                isScanning = true,
                isFindPackageLookupActive = true,
                currentBatchEpcCount = 0,
                shipmentError = null,
                isSubmittingBatch = false,
                findPackageDialog = FindPackageDialogState.Hidden
            )
        }
    }

    private fun processFindPackageEpc(sid: String, epc: String) {
        viewModelScope.launch {
            if (!findPackageMutex.tryLock()) return@launch
            try {
                val token = userPreferences.getSessionToken()
                val summary = shipmentStore[sid]
                if (token == null || summary == null || summary.consignmentRemoteId <= 0) {
                    _state.update { it.copy(shipmentError = "session") }
                    return@launch
                }
                packagesRepository.findPackage(token, summary.consignmentRemoteId, epc)
                    .onSuccess { data ->
                        stopScanningInternal(sendStopCommand = true)
                        _state.update { it.copy(findPackageDialog = FindPackageDialogState.Found(data)) }
                    }
                    .onFailure { e ->
                        _state.update {
                            it.copy(
                                findPackageDialog = FindPackageDialogState.Error(
                                    e.message?.takeIf { m -> m.isNotBlank() } ?: "find_failed"
                                )
                            )
                        }
                    }
            } finally {
                findPackageMutex.unlock()
            }
        }
    }

    fun selectShipment(id: String) {
        if (_state.value.isScanning) {
            stopScanningInternal(sendStopCommand = true)
        }
        sessionBarcodeForPackages = ""
        _state.update {
            it.copy(
                selectedShipmentId = id,
                packages = packageStore[id].orEmpty().toList(),
                expandedPackageIds = emptySet(),
                selectedPackageIds = emptySet(),
                shipmentError = null,
                pendingQrRaw = null,
                qrScanRequestId = 0L,
                sizeTotalsDialog = SizeTotalsDialogState.Hidden,
                addPackageWarningMessage = null,
                findPackageDialog = FindPackageDialogState.Hidden,
                isFindPackageLookupActive = false
            )
        }
        rebuildCommittedEpcKeys(id)
        loadPackages(id)
    }

    fun togglePackageExpanded(packageId: String) {
        _state.update { s ->
            val next = s.expandedPackageIds.toMutableSet()
            if (!next.add(packageId)) next.remove(packageId)
            s.copy(expandedPackageIds = next)
        }
    }

    fun togglePackageSelection(packageId: String) {
        _state.update { s ->
            val next = s.selectedPackageIds.toMutableSet()
            if (!next.add(packageId)) next.remove(packageId)
            s.copy(selectedPackageIds = next)
        }
    }

    fun clearPackageSelection() {
        _state.update { it.copy(selectedPackageIds = emptySet()) }
    }

    fun showNewShipmentDialog() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        _state.update {
            it.copy(
                dialog = ShipmentDialog.NewShipment(
                    draftDeliveryDate = today,
                    isLoadingConsignees = true
                )
            )
        }
        viewModelScope.launch {
            val token = userPreferences.getSessionToken()
            val companyId = userPreferences.getSessionCompanyId()
            if (token == null || companyId == null) {
                _state.update { s ->
                    when (val d = s.dialog) {
                        is ShipmentDialog.NewShipment -> s.copy(
                            dialog = d.copy(isLoadingConsignees = false, consigneesLoadError = "session")
                        )
                        else -> s
                    }
                }
                return@launch
            }
            consignmentRepository.getConsignees(token, companyId)
                .onSuccess { list ->
                    _state.update { s ->
                        when (val d = s.dialog) {
                            is ShipmentDialog.NewShipment -> {
                                val first = list.firstOrNull { it.id > 0 }
                                s.copy(
                                    dialog = d.copy(
                                        consignees = list,
                                        isLoadingConsignees = false,
                                        consigneesLoadError = null,
                                        selectedConsigneeId = first?.id
                                    )
                                )
                            }
                            else -> s
                        }
                    }
                }
                .onFailure { e ->
                    _state.update { s ->
                        when (val d = s.dialog) {
                            is ShipmentDialog.NewShipment -> s.copy(
                                dialog = d.copy(
                                    isLoadingConsignees = false,
                                    consigneesLoadError = e.message ?: "consignees_failed"
                                )
                            )
                            else -> s
                        }
                    }
                }
        }
    }

    fun showCloseConsignmentDialog() {
        val sid = _state.value.selectedShipmentId ?: return
        val remoteId = shipmentStore[sid]?.consignmentRemoteId ?: return
        if (remoteId <= 0) return
        _state.update { it.copy(dialog = ShipmentDialog.CloseConsignment()) }
    }

    fun confirmCloseConsignment() {
        val d = _state.value.dialog as? ShipmentDialog.CloseConsignment ?: return
        if (d.isSubmitting) return
        val sid = _state.value.selectedShipmentId ?: return
        val remoteId = shipmentStore[sid]?.consignmentRemoteId ?: return
        if (remoteId <= 0) return
        val token = userPreferences.getSessionToken()
        val companyId = userPreferences.getSessionCompanyId()
        if (token == null || companyId == null) {
            _state.update {
                it.copy(
                    dialog = ShipmentDialog.None,
                    shipmentError = "session"
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(dialog = ShipmentDialog.CloseConsignment(isSubmitting = true)) }
            consignmentRepository.closeConsignment(token, companyId, remoteId)
                .onSuccess {
                    _state.update { it.copy(dialog = ShipmentDialog.None) }
                    refreshConsignments()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            dialog = ShipmentDialog.None,
                            shipmentError = e.message ?: "close_failed"
                        )
                    }
                }
        }
    }

    fun updateNewShipmentDraftName(name: String) {
        _state.update { s ->
            when (val d = s.dialog) {
                is ShipmentDialog.NewShipment -> s.copy(dialog = d.copy(draftName = name))
                else -> s
            }
        }
    }

    fun updateNewShipmentDraftExpectedCount(value: String) {
        val digits = value.filter { it.isDigit() }.take(9)
        _state.update { s ->
            when (val d = s.dialog) {
                is ShipmentDialog.NewShipment -> s.copy(dialog = d.copy(draftExpectedCount = digits))
                else -> s
            }
        }
    }

    fun updateNewShipmentDraftDeliveryDate(value: String) {
        _state.update { s ->
            when (val d = s.dialog) {
                is ShipmentDialog.NewShipment -> s.copy(dialog = d.copy(draftDeliveryDate = value))
                else -> s
            }
        }
    }

    fun updateNewShipmentSelectedConsigneeId(id: Int) {
        _state.update { s ->
            when (val d = s.dialog) {
                is ShipmentDialog.NewShipment -> s.copy(dialog = d.copy(selectedConsigneeId = id))
                else -> s
            }
        }
    }

    fun dismissDialog() {
        _state.update { it.copy(dialog = ShipmentDialog.None) }
    }

    fun confirmNewShipment() {
        val d = _state.value.dialog as? ShipmentDialog.NewShipment ?: return
        if (d.isSubmitting || d.isLoadingConsignees) return
        val name = d.draftName.trim()
        val expected = d.draftExpectedCount.trim().toIntOrNull()
        val date = d.draftDeliveryDate.trim()
        val consigneeIdForApi = d.selectedConsigneeId
        if (name.isBlank() || expected == null || expected <= 0 || date.isBlank() ||
            consigneeIdForApi == null || consigneeIdForApi <= 0
        ) {
            _state.update { it.copy(shipmentError = "validation") }
            return
        }
        val token = userPreferences.getSessionToken()
        val companyId = userPreferences.getSessionCompanyId()
        if (token == null || companyId == null) {
            _state.update { it.copy(shipmentError = "session") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(dialog = d.copy(isSubmitting = true), shipmentError = null) }
            val body = AddConsignmentZaraRequestModel(
                dataList = ZaraDataList(
                    consignee = consigneeIdForApi,
                    consigneeId = consigneeIdForApi,
                    deliveryDate = date,
                    itemCount = expected.toString(),
                    poNumber = name
                )
            )
            consignmentRepository.addConsignmentZaraStore(token, companyId, body)
                .onSuccess {
                    _state.update { it.copy(dialog = ShipmentDialog.None) }
                    refreshConsignments()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            dialog = d.copy(isSubmitting = false),
                            shipmentError = e.message ?: "add_failed"
                        )
                    }
                }
        }
    }

    fun showDeletePackageDialog(packageId: String) {
        _state.update { it.copy(dialog = ShipmentDialog.DeletePackages(listOf(packageId))) }
    }

    fun showDeleteSelectedPackagesDialog() {
        val ids = _state.value.selectedPackageIds.toList()
        if (ids.isEmpty()) return
        _state.update { it.copy(dialog = ShipmentDialog.DeletePackages(ids)) }
    }

    fun selectAllPackages(packageIds: List<String>) {
        if (packageIds.isEmpty()) return
        _state.update { it.copy(selectedPackageIds = packageIds.toSet()) }
    }

    fun confirmDeletePackage() {
        val d = _state.value.dialog as? ShipmentDialog.DeletePackages ?: return
        if (d.packageIds.isEmpty()) return
        val sid = _state.value.selectedShipmentId ?: return
        val remoteId = shipmentStore[sid]?.consignmentRemoteId ?: return
        if (remoteId <= 0) {
            _state.update { it.copy(shipmentError = "package_missing_ids") }
            return
        }
        val token = userPreferences.getSessionToken()
        val companyId = userPreferences.getSessionCompanyId()
        if (token == null || companyId == null) {
            _state.update { it.copy(shipmentError = "session") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPackages = true, shipmentError = null) }
            val body = DeletePackageRequestModel(
                dataList = DeletePackageDataList(
                    consignmentId = remoteId,
                    entries = d.packageIds.map { DeletePackageData(id = it) }
                )
            )
            packagesRepository.deletePackages(token, companyId, body)
                .onSuccess {
                    val removed = d.packageIds.toSet()
                    val nextList = packageStore[sid].orEmpty()
                        .filterNot { it.id in removed }
                        .toMutableList()
                    packageStore[sid] = nextList
                    rebuildCommittedEpcKeys(sid)
                    syncCounts()
                    _state.update {
                        it.copy(
                            dialog = ShipmentDialog.None,
                            selectedPackageIds = it.selectedPackageIds - removed,
                            shipments = shipmentStore.values.toList(),
                            packages = if (it.selectedShipmentId == sid) {
                                nextList.toList()
                            } else {
                                it.packages
                            }
                        )
                    }
                    loadPackages(sid)
                    interruptEpcScanForBarcodeCycleIfNeeded()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoadingPackages = false,
                            shipmentError = e.message ?: "delete_failed"
                        )
                    }
                }
        }
    }

    fun showMergeDialog() {
        val ids = _state.value.selectedPackageIds.toList()
        if (ids.size < 2) return
        _state.update { it.copy(dialog = ShipmentDialog.MergePackages(packageIds = ids)) }
    }

    fun confirmMergePackages() {
        val d = _state.value.dialog as? ShipmentDialog.MergePackages ?: return
        if (d.packageIds.size < 2) return
        val sid = _state.value.selectedShipmentId ?: return
        val list = packageStore[sid] ?: return
        val remoteId = shipmentStore[sid]?.consignmentRemoteId ?: return
        if (remoteId <= 0) {
            _state.update { it.copy(shipmentError = "package_missing_ids") }
            return
        }
        val token = userPreferences.getSessionToken()
        val companyId = userPreferences.getSessionCompanyId()
        if (token == null || companyId == null) {
            _state.update { it.copy(shipmentError = "session") }
            return
        }
        val idSet = d.packageIds.toSet()
        if (list.none { it.id in idSet }) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPackages = true, shipmentError = null) }
            val body = CombinePackageRequestModel(
                dataList = CombineDataList(
                    consignmentId = remoteId,
                    packages = d.packageIds
                )
            )
            packagesRepository.combinePackages(token, companyId, body)
                .onSuccess {
                    _state.update {
                        it.copy(
                            dialog = ShipmentDialog.None,
                            selectedPackageIds = emptySet(),
                            expandedPackageIds = emptySet()
                        )
                    }
                    loadPackages(sid)
                    interruptEpcScanForBarcodeCycleIfNeeded()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoadingPackages = false,
                            shipmentError = e.message ?: "merge_failed"
                        )
                    }
                }
        }
    }

    fun toggleScanning() {
        if (_state.value.isScanning) {
            stopScanningInternal(sendStopCommand = true)
            return
        }
        if (BluetoothConnectionController.state.value !is BluetoothConnectionState.Connected) return
        val sid = _state.value.selectedShipmentId ?: return
        shipmentStore[sid] ?: return
        val ws = readerSettings()
        if (ws.barcodeEnabled) {
            _state.update {
                it.copy(
                    qrScanRequestId = it.qrScanRequestId + 1L,
                    shipmentError = null
                )
            }
        } else {
            sessionBarcodeForPackages = ""
            startEpcScanning()
        }
    }

    fun onQrScannerFinished(raw: String?) {
        if (raw.isNullOrBlank()) {
            Log.d(TAG_QR, "scan result: <empty or cancelled>")
            return
        }
        Log.d(
            TAG_QR,
            "length=${raw.length} codePoints=${raw.codePointCount(0, raw.length)}"
        )
        val maxChunk = 3500
        if (raw.length <= maxChunk) {
            Log.d(TAG_QR, "raw=$raw")
        } else {
            raw.chunked(maxChunk).forEachIndexed { index, chunk ->
                Log.d(TAG_QR, "raw[$index]=${chunk}")
            }
        }
        _state.update { it.copy(pendingQrRaw = raw, shipmentError = null) }
    }

    fun confirmPendingQrAndStartEpc() {
        val raw = _state.value.pendingQrRaw ?: return
        sessionBarcodeForPackages = raw
        _state.update { it.copy(pendingQrRaw = null) }
        startEpcScanning()
    }

    fun dismissPendingQrAndRescan() {
        _state.update {
            it.copy(
                pendingQrRaw = null,
                qrScanRequestId = it.qrScanRequestId + 1L
            )
        }
    }

    private fun startEpcScanning() {
        if (_state.value.isScanning && !_state.value.isFindPackageLookupActive) return
        if (BluetoothConnectionController.state.value !is BluetoothConnectionState.Connected) return
        val sid = _state.value.selectedShipmentId ?: return
        shipmentStore[sid] ?: return

        val ws = readerSettings()
        val seconds = ws.packetCloseTimeout.coerceIn(1, 10)
        val weightFlag = if (ws.weightEnabled) "1" else "0"

        packetIdleJob?.cancel()
        packetIdleJob = null
        synchronized(windowBuffer) { windowBuffer.clear() }

        rebuildCommittedEpcKeys(sid)

        val wasFind = _state.value.isFindPackageLookupActive
        if (!wasFind) {
            BluetoothConnectionController.sendCommand("""{"Status":["Start","$weightFlag"]}""")
        }
        _state.update {
            it.copy(
                isScanning = true,
                isFindPackageLookupActive = false,
                currentBatchEpcCount = 0,
                packetCloseSeconds = seconds,
                shipmentError = null,
                isSubmittingBatch = false
            )
        }
    }

    /**
     * Okuyucu ayarındaki paket kapatma süresi (ör. 5 sn) boyunca **yeni benzersiz EPC gelmezse**
     * tampondaki okumalar tek paket olarak sunucuya gider. Her yeni EPC süreyi sıfırlar.
     */
    private fun schedulePacketIdleFlush() {
        if (!_state.value.isScanning) return
        if (_state.value.isFindPackageLookupActive) return
        val seconds = readerSettings().packetCloseTimeout.coerceIn(1, 10)
        packetIdleJob?.cancel()
        packetIdleJob = viewModelScope.launch {
            delay(seconds * 1000L)
            if (!isActive) return@launch
            if (!_state.value.isScanning) return@launch
            if (_state.value.isFindPackageLookupActive) return@launch
            viewModelScope.launch {
                flushWindowBufferInternal()
            }
        }
    }

    private suspend fun flushWindowBufferInternal() {
        flushMutex.withLock {
            val currentSid = _state.value.selectedShipmentId ?: return@withLock
            val batch = synchronized(windowBuffer) {
                val b = windowBuffer.toList()
                windowBuffer.clear()
                b
            }
            _state.update { it.copy(currentBatchEpcCount = 0) }

            if (batch.isEmpty()) return@withLock

            val distinct = batch.map { normalizeEpcKey(it) }.filter { it.isNotBlank() }.distinct()
            val blocked = synchronized(epcRegistryLock) {
                committedEpcKeysByShipment[currentSid] ?: run {
                    rebuildCommittedEpcKeysLocked(currentSid)
                    committedEpcKeysByShipment[currentSid]!!
                }
            }
            val newOnly = distinct.filter { it !in blocked }
            if (newOnly.isEmpty()) return@withLock

            val token = userPreferences.getSessionToken()
            val companyId = userPreferences.getSessionCompanyId()
            val summary = shipmentStore[currentSid]
            if (token == null || companyId == null || summary == null) {
                _state.update { it.copy(shipmentError = "session") }
                return@withLock
            }
            if (summary.consignmentRemoteId <= 0) {
                _state.update { it.copy(shipmentError = "package_missing_ids") }
                return@withLock
            }

            _state.update { it.copy(isSubmittingBatch = true) }
            val nextPackageNo = nextPackageNumberForShipment(currentSid)
            val body = PackageZaraRequestModel(
                dataList = PackageZaraDataList(
                    box_type_id = 1,
                    consignmentId = summary.consignmentRemoteId,
                    device_id = companyId,
                    load_type = "RFID",
                    // Sunucu boş stringi "gönderilmedi" sayıyor (empty()).
                    model = "-",
                    orderId = summary.orderRemoteId,
                    packageId = nextPackageNo,
                    size = "-",
                    dataList = newOnly.map { PackageEpcData(epc = it) },
                    barcode = sessionBarcodeForPackages,
                    weight = "0"
                )
            )
            packagesRepository.addPackageZaraStore(token, companyId, body)
                .onSuccess { outcome ->
                    synchronized(epcRegistryLock) {
                        committedEpcKeysByShipment.getOrPut(currentSid) { mutableSetOf() }.addAll(newOnly)
                    }
                    loadPackages(currentSid)
                    interruptEpcScanForBarcodeCycleIfNeeded()
                    when (outcome) {
                        AddPackageZaraResult.Ok -> {}
                        is AddPackageZaraResult.OkWithServerMessage -> {
                            _state.update { it.copy(addPackageWarningMessage = outcome.message) }
                        }
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            addPackageWarningMessage = e.message?.takeIf { m -> m.isNotBlank() }
                                ?: "Add package failed"
                        )
                    }
                }
            _state.update { it.copy(isSubmittingBatch = false) }
        }
    }

    private fun stopScanningInternal(sendStopCommand: Boolean) {
        packetIdleJob?.cancel()
        packetIdleJob = null
        synchronized(windowBuffer) { windowBuffer.clear() }
        if (sendStopCommand) {
            BluetoothConnectionController.sendCommand("""{"Status":["Stop"]}""")
        }
        sessionBarcodeForPackages = ""
        _state.update {
            it.copy(
                isScanning = false,
                isFindPackageLookupActive = false,
                currentBatchEpcCount = 0,
                isSubmittingBatch = false,
                pendingQrRaw = null,
                qrScanRequestId = 0L
            )
        }
    }

    class Factory(
        private val userPreferences: UserPreferences,
        private val consignmentRepository: ConsignmentRepository,
        private val packagesRepository: PackagesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShipmentViewModel(
                userPreferences,
                consignmentRepository,
                packagesRepository
            ) as T
        }
    }
}
