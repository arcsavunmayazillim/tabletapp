@file:OptIn(ExperimentalMaterial3Api::class)

package com.takipsanplus.rfidtablet.presentation.settings

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import com.takipsanplus.rfidtablet.data.network.BridgePlusConnectionController
import com.takipsanplus.rfidtablet.data.network.ConnectionType
import com.takipsanplus.rfidtablet.data.network.DeviceConnectionState
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.PremiumScreenBackdrop
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveInk
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveMuted
import com.takipsanplus.rfidtablet.presentation.theme.LiveEmerald
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

private enum class ConnectionMode { WebSocket, Ble }

@SuppressLint("MissingPermission")
private fun startBleScanSafe(scanner: BluetoothLeScanner?, callback: ScanCallback) {
    try { scanner?.startScan(callback) } catch (_: Exception) {}
}

@SuppressLint("MissingPermission")
private fun stopBleScanSafe(scanner: BluetoothLeScanner?, callback: ScanCallback) {
    try { scanner?.stopScan(callback) } catch (_: Exception) {}
}

// ─────────────────────────────────────────────────────────────────────────────
// Main screen
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@Composable
fun SettingsScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context.applicationContext) }

    val storedReaderSettings = remember {
        userPrefs.getReaderSettings(
            default = UserPreferences.ReaderSettings(
                ant1 = 0, ant2 = 0, ant3 = 0, ant4 = 0,
                packetCloseTimeout = 5,
                weightEnabled = true,
                barcodeEnabled = false
            )
        )
    }

    val connectionState by BridgePlusConnectionController.state.collectAsState()

    // ── Connection UI state ───────────────────────────────────────────────────
    var connectionMode by remember { mutableStateOf(ConnectionMode.WebSocket) }
    var ipInput by remember { mutableStateOf(userPrefs.getSelectedDeviceIp().orEmpty()) }

    // BLE scan state
    val bleDevices = remember { mutableStateMapOf<String, ScanResult>() }
    var isScanning by remember { mutableStateOf(false) }
    var isPreparingBle by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val btAdapter = remember(context) {
        context.getSystemService(BluetoothManager::class.java)?.adapter
    }
    val bleScanner = remember(btAdapter) { btAdapter?.bluetoothLeScanner }

    val scanCallback = remember {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                bleDevices[result.device.address] = result
            }
            override fun onScanFailed(errorCode: Int) {
                isScanning = false
            }
        }
    }

    val blePermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.all { it }) {
            bleDevices.clear()
            isPreparingBle = true
            coroutineScope.launch {
                BridgePlusConnectionController.prepareForBle(ipInput.trim())
                delay(800) // cihazın BLE moduna geçmesi için
                startBleScanSafe(bleScanner, scanCallback)
                isScanning = true
                isPreparingBle = false
            }
        }
    }

    // Stop scan when the screen leaves composition or mode changes
    DisposableEffect(Unit) {
        onDispose { stopBleScanSafe(bleScanner, scanCallback) }
    }
    LaunchedEffect(connectionMode) {
        if (connectionMode != ConnectionMode.Ble && isScanning) {
            stopBleScanSafe(bleScanner, scanCallback)
            isScanning = false
        }
    }

    // ── Reader settings state ────────────────────────────────────────────────
    var settingsSaved by remember { mutableStateOf(false) }
    var ant1 by remember { mutableIntStateOf(storedReaderSettings.ant1) }
    var ant2 by remember { mutableIntStateOf(storedReaderSettings.ant2) }
    var ant3 by remember { mutableIntStateOf(storedReaderSettings.ant3) }
    var ant4 by remember { mutableIntStateOf(storedReaderSettings.ant4) }
    var packCloseTimeout by remember { mutableIntStateOf(storedReaderSettings.packetCloseTimeout) }
    var weightEnabled by remember { mutableStateOf(storedReaderSettings.weightEnabled) }
    var barcodeEnabled by remember { mutableStateOf(storedReaderSettings.barcodeEnabled) }

    // WebSocket bağlantısı kurulduğunda cihazın mevcut config'ini oku → UI'ı güncelle
    LaunchedEffect(connectionState) {
        val cs = connectionState
        if (cs is DeviceConnectionState.Connected && cs.connectionType == ConnectionType.WebSocket) {
            val config = BridgePlusConnectionController.readReaderConfig(cs.deviceIp)
            if (config != null) {
                ant1 = config.ant1
                ant2 = config.ant2
                ant3 = config.ant3
                ant4 = config.ant4
            }
        }
    }

    val scroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumScreenBackdrop(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            SettingsHeroHeader(language = language, onBack = onBack)
            Spacer(modifier = Modifier.height(18.dp))

            // ── Device connection section ─────────────────────────────────────
            SettingsGlassSection {
                SectionTitleRow(
                    icon = Icons.Filled.Wifi,
                    title = localizedString(R.string.connection_title, language),
                    trailing = {
                        DeviceStatusPill(
                            language = language,
                            state = connectionState,
                            compact = true
                        )
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))

                ConnectionModeTabRow(
                    mode = connectionMode,
                    language = language,
                    onModeChanged = { connectionMode = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                when (connectionMode) {
                    ConnectionMode.WebSocket -> WebSocketTab(
                        language = language,
                        ipInput = ipInput,
                        onIpChanged = { ipInput = it },
                        connectionState = connectionState,
                        onConnectClick = {
                            val ip = ipInput.trim()
                            userPrefs.saveSelectedDeviceIp(ip)
                            BridgePlusConnectionController.connect(ip)
                        },
                        onDisconnectClick = { BridgePlusConnectionController.disconnect() }
                    )
                    ConnectionMode.Ble -> BleTab(
                        language = language,
                        bleDevices = bleDevices.values.toList(),
                        isScanning = isScanning,
                        isPreparing = isPreparingBle,
                        connectionState = connectionState,
                        onScanClick = {
                            val neededPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                            } else {
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            val allGranted = neededPerms.all { perm ->
                                ContextCompat.checkSelfPermission(context, perm) ==
                                    PackageManager.PERMISSION_GRANTED
                            }
                            if (allGranted) {
                                bleDevices.clear()
                                isPreparingBle = true
                                coroutineScope.launch {
                                    // Cihazı BLE moduna geçir, ardından tara
                                    // ipInput: kullanıcının WebSocket alanında gördüğü IP
                                    BridgePlusConnectionController.prepareForBle(ipInput.trim())
                                    delay(800) // cihazın BLE moduna geçmesi için
                                    startBleScanSafe(bleScanner, scanCallback)
                                    isScanning = true
                                    isPreparingBle = false
                                }
                            } else {
                                blePermLauncher.launch(neededPerms)
                            }
                        },
                        onStopScanClick = {
                            stopBleScanSafe(bleScanner, scanCallback)
                            isScanning = false
                        },
                        onDeviceConnectClick = { result ->
                            BridgePlusConnectionController.connectBle(result.device, context)
                        },
                        onDisconnectClick = { BridgePlusConnectionController.disconnect() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Reader settings section ───────────────────────────────────────
            SettingsGlassSection {
                SectionTitleRow(
                    icon = Icons.Filled.Tune,
                    title = localizedString(R.string.reader_settings_title, language)
                )
                Spacer(modifier = Modifier.height(14.dp))

                AntennaPowerSelectorRow(
                    language = language,
                    ant1 = ant1, ant2 = ant2, ant3 = ant3, ant4 = ant4,
                    onAnt1Changed = { ant1 = it },
                    onAnt2Changed = { ant2 = it },
                    onAnt3Changed = { ant3 = it },
                    onAnt4Changed = { ant4 = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                PacketCloseTimeSelector(
                    language = language,
                    value = packCloseTimeout,
                    onValueChange = { packCloseTimeout = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                val isWide = LocalConfiguration.current.smallestScreenWidthDp >= 600

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SettingsToggleRow(
                                label = localizedString(R.string.weight_enabled, language),
                                checked = weightEnabled,
                                onCheckedChange = { weightEnabled = it }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SettingsToggleRow(
                                label = localizedString(R.string.barcode_enabled, language),
                                checked = barcodeEnabled,
                                onCheckedChange = { barcodeEnabled = it }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsToggleRow(
                            label = localizedString(R.string.weight_enabled, language),
                            checked = weightEnabled,
                            onCheckedChange = { weightEnabled = it }
                        )
                        SettingsToggleRow(
                            label = localizedString(R.string.barcode_enabled, language),
                            checked = barcodeEnabled,
                            onCheckedChange = { barcodeEnabled = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Kaydetme onayı: 2 saniye göster
                LaunchedEffect(settingsSaved) {
                    if (settingsSaved) {
                        kotlinx.coroutines.delay(2000)
                        settingsSaved = false
                    }
                }

                Button(
                    onClick = {
                        userPrefs.saveReaderSettings(
                            ant1 = ant1, ant2 = ant2, ant3 = ant3, ant4 = ant4,
                            packetCloseTimeout = packCloseTimeout,
                            weightEnabled = weightEnabled,
                            barcodeEnabled = barcodeEnabled
                        )
                        settingsSaved = true
                        // WebSocket bağlıysa anten ayarlarını cihaza da gönder
                        val cs = connectionState
                        if (cs is DeviceConnectionState.Connected &&
                            cs.connectionType == ConnectionType.WebSocket) {
                            BridgePlusConnectionController.applyReaderConfig(
                                ip = cs.deviceIp,
                                ant1 = ant1, ant2 = ant2, ant3 = ant3, ant4 = ant4
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (settingsSaved)
                            localizedString(R.string.settings_saved, language)
                        else
                            localizedString(R.string.settings_save, language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Connection sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConnectionModeTabRow(
    mode: ConnectionMode,
    language: AppLanguage,
    onModeChanged: (ConnectionMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F5F9))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ConnectionModeTab(
            label = localizedString(R.string.connection_mode_websocket, language),
            icon = Icons.Filled.Wifi,
            selected = mode == ConnectionMode.WebSocket,
            modifier = Modifier.weight(1f),
            onClick = { onModeChanged(ConnectionMode.WebSocket) }
        )
        ConnectionModeTab(
            label = localizedString(R.string.connection_mode_ble, language),
            icon = Icons.Filled.Bluetooth,
            selected = mode == ConnectionMode.Ble,
            modifier = Modifier.weight(1f),
            onClick = { onModeChanged(ConnectionMode.Ble) }
        )
    }
}

@Composable
private fun ConnectionModeTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) PrimaryBlue else ExecutiveMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) PrimaryBlue else ExecutiveMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun WebSocketTab(
    language: AppLanguage,
    ipInput: String,
    onIpChanged: (String) -> Unit,
    connectionState: DeviceConnectionState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    val isConnected = connectionState is DeviceConnectionState.Connected
    val isConnecting = connectionState is DeviceConnectionState.Connecting

    OutlinedTextField(
        value = ipInput,
        onValueChange = onIpChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(localizedString(R.string.device_ip_label, language)) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        enabled = !isConnected && !isConnecting,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = SettingsCardBorder,
            focusedLabelColor = PrimaryBlue
        )
    )

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isConnected) {
            Button(
                onClick = onDisconnectClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFF4FF),
                    contentColor = PrimaryBlue
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.Filled.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = localizedString(R.string.bluetooth_disconnect, language),
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onConnectClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                enabled = ipInput.isNotBlank() && !isConnecting
            ) {
                Text(
                    text = if (isConnecting) localizedString(R.string.bluetooth_connecting, language)
                           else localizedString(R.string.bluetooth_connect, language),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = PrimaryBlue,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun BleTab(
    language: AppLanguage,
    bleDevices: List<ScanResult>,
    isScanning: Boolean,
    isPreparing: Boolean,
    connectionState: DeviceConnectionState,
    onScanClick: () -> Unit,
    onStopScanClick: () -> Unit,
    onDeviceConnectClick: (ScanResult) -> Unit,
    onDisconnectClick: () -> Unit
) {
    // ── Hazırlanıyor / Tarama / Başlat satırı ────────────────────────────────
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPreparing) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = PrimaryBlue,
                strokeWidth = 2.5.dp
            )
            Text(
                text = localizedString(R.string.ble_preparing, language),
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
        } else if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = PrimaryBlue,
                strokeWidth = 2.5.dp
            )
            Text(
                text = localizedString(R.string.ble_scanning, language),
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onStopScanClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFE4E6),
                    contentColor = Color(0xFFBE123C)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = localizedString(R.string.stop_action, language),
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onScanClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.Filled.BluetoothSearching,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = localizedString(R.string.ble_scan, language),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // ── Disconnect (when BLE is connected) ───────────────────────────────────
    if (connectionState is DeviceConnectionState.Connected) {
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onDisconnectClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEFF4FF),
                contentColor = PrimaryBlue
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                Icons.Filled.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = localizedString(R.string.bluetooth_disconnect, language),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    // ── Device list ───────────────────────────────────────────────────────────
    Spacer(Modifier.height(10.dp))
    if (bleDevices.isEmpty()) {
        if (!isScanning) {
            Text(
                text = localizedString(R.string.ble_no_devices, language),
                style = MaterialTheme.typography.bodyMedium,
                color = ExecutiveMuted
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            bleDevices.forEach { result ->
                BleDeviceRow(
                    result = result,
                    language = language,
                    onClick = { onDeviceConnectClick(result) }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun BleDeviceRow(
    result: ScanResult,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val name = result.device.name?.takeIf { it.isNotBlank() } ?: result.device.address
    val address = result.device.address

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, SettingsCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Filled.Bluetooth,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = ExecutiveInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = ExecutiveMuted,
                maxLines = 1
            )
        }
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            modifier = Modifier.heightIn(min = 36.dp)
        ) {
            Text(
                text = localizedString(R.string.bluetooth_connect, language),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared layout composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsHeroHeader(language: AppLanguage, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = ExecutiveInk
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = localizedString(R.string.settings_title, language),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp
            ),
            color = ExecutiveInk
        )
    }
}

private val SettingsCardBorder = Color(0xFFE2E8F0)
private val SettingsCardSurface = Color(0xFFFCFCFD)

@Composable
private fun SettingsGlassSection(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SettingsCardSurface)
            .border(1.dp, SettingsCardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        content()
    }
}

@Composable
private fun SectionTitleRow(
    icon: ImageVector,
    title: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue.copy(alpha = 0.08f))
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ExecutiveInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(10.dp))
            trailing()
        }
    }
}

@Composable
private fun DeviceStatusPill(
    language: AppLanguage,
    state: DeviceConnectionState,
    compact: Boolean = false
) {
    val (bg, fg, dot, label) = when (state) {
        is DeviceConnectionState.Connected -> {
            val typeLabel = when (state.connectionType) {
                ConnectionType.WebSocket -> "WebSocket"
                ConnectionType.Ble -> "Bluetooth LE"
            }
            Quadruple(
                LiveEmerald.copy(alpha = 0.12f),
                LiveEmerald,
                LiveEmerald,
                "$typeLabel · ${state.deviceName}"
            )
        }
        DeviceConnectionState.Connecting -> Quadruple(
            PrimaryBlue.copy(alpha = 0.1f),
            PrimaryBlue,
            PrimaryBlue,
            localizedString(R.string.bluetooth_connecting, language)
        )
        is DeviceConnectionState.Error -> Quadruple(
            Color(0xFFFFE4E6),
            Color(0xFFBE123C),
            Color(0xFFBE123C),
            state.message.ifBlank { localizedString(R.string.bluetooth_connect_failed, language) }
        )
        DeviceConnectionState.Disconnected -> Quadruple(
            Color(0xFFF1F5F9),
            ExecutiveMuted,
            ExecutiveMuted.copy(alpha = 0.5f),
            localizedString(R.string.bluetooth_disconnected, language)
        )
    }

    val rowModifier = Modifier
        .then(if (compact) Modifier.widthIn(max = 220.dp) else Modifier.fillMaxWidth())
        .clip(RoundedCornerShape(100.dp))
        .background(bg)
        .border(1.dp, fg.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
        .padding(
            horizontal = if (compact) 10.dp else 14.dp,
            vertical = if (compact) 8.dp else 10.dp
        )

    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dot)
        )
        Spacer(modifier = Modifier.width(if (compact) 8.dp else 10.dp))
        Text(
            text = label,
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minWidth = 0.dp)
        )
        if (state is DeviceConnectionState.Connected) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = when (state.connectionType) {
                    ConnectionType.Ble -> Icons.Filled.Bluetooth
                    ConnectionType.WebSocket -> Icons.Filled.Wifi
                },
                contentDescription = null,
                tint = fg.copy(alpha = 0.85f),
                modifier = Modifier.size(if (compact) 18.dp else 20.dp)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ─────────────────────────────────────────────────────────────────────────────
// Reader settings composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AntennaPowerSelectorRow(
    language: AppLanguage,
    ant1: Int, ant2: Int, ant3: Int, ant4: Int,
    onAnt1Changed: (Int) -> Unit,
    onAnt2Changed: (Int) -> Unit,
    onAnt3Changed: (Int) -> Unit,
    onAnt4Changed: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(language, R.string.antenna_1, ant1, onAnt1Changed)
            }
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(language, R.string.antenna_2, ant2, onAnt2Changed)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(language, R.string.antenna_3, ant3, onAnt3Changed)
            }
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(language, R.string.antenna_4, ant4, onAnt4Changed)
            }
        }
    }
}

@Composable
private fun AntennaPowerSelector(
    language: AppLanguage,
    titleResId: Int,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = SettingsCardBorder,
        focusedLabelColor = PrimaryBlue
    )

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .heightIn(min = 62.dp),
            value = value.toString(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(text = localizedString(titleResId, language), maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (0..30).forEach { p ->
                DropdownMenuItem(
                    text = { Text(text = p.toString()) },
                    onClick = { onValueChange(p); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun PacketCloseTimeSelector(
    language: AppLanguage,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = SettingsCardBorder,
        focusedLabelColor = PrimaryBlue
    )

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .heightIn(min = 62.dp),
            value = value.toString(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(text = localizedString(R.string.packet_close_time, language), maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (1..10).forEach { t ->
                DropdownMenuItem(
                    text = { Text(text = t.toString()) },
                    onClick = { onValueChange(t); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, SettingsCardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = ExecutiveInk
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ExecutiveMuted.copy(alpha = 0.35f)
            )
        )
    }
}
