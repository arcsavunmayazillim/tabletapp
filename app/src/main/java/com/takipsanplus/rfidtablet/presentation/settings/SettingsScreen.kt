@file:OptIn(ExperimentalMaterial3Api::class)

package com.takipsanplus.rfidtablet.presentation.settings

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.data.bluetooth.BluetoothConnectionController
import com.takipsanplus.rfidtablet.data.bluetooth.BluetoothConnectionState
import com.takipsanplus.rfidtablet.data.local.UserPreferences
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.PremiumScreenBackdrop
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveInk
import com.takipsanplus.rfidtablet.presentation.theme.ExecutiveMuted
import com.takipsanplus.rfidtablet.presentation.theme.LiveEmerald
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue

@Composable
fun SettingsScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val userPrefs = remember { UserPreferences(context.applicationContext) }
    val storedReaderSettings = userPrefs.getReaderSettings(
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

    LaunchedEffect(Unit) {
        BluetoothConnectionController.init(context)
    }

    val bluetoothAdapter = remember { 
        context.getSystemService(BluetoothManager::class.java)?.adapter 
    }

    var bondedDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var selectedDeviceAddress by remember { mutableStateOf(userPrefs.getSelectedBluetoothDeviceAddress()) }

    val connectionState by BluetoothConnectionController.state.collectAsState()

    fun hasBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val devices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()
            bondedDevices = devices
            selectedDeviceAddress = selectedDeviceAddress ?: devices.firstOrNull()?.address
        }
    }

    fun ensurePermissionAndLoad() {
        if (hasBluetoothConnectPermission()) {
            val devices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()
            bondedDevices = devices
            selectedDeviceAddress = selectedDeviceAddress ?: devices.firstOrNull()?.address
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }

    LaunchedEffect(Unit) { ensurePermissionAndLoad() }

    val selectedDevice = bondedDevices.firstOrNull { it.address == selectedDeviceAddress }

    var ant1 by remember { mutableIntStateOf(storedReaderSettings.ant1) }
    var ant2 by remember { mutableIntStateOf(storedReaderSettings.ant2) }
    var ant3 by remember { mutableIntStateOf(storedReaderSettings.ant3) }
    var ant4 by remember { mutableIntStateOf(storedReaderSettings.ant4) }

    var packCloseTimeout by remember { mutableIntStateOf(storedReaderSettings.packetCloseTimeout) }

    var weightEnabled by remember { mutableStateOf(storedReaderSettings.weightEnabled) }
    var barcodeEnabled by remember { mutableStateOf(storedReaderSettings.barcodeEnabled) }

    val sendEnabled = connectionState is BluetoothConnectionState.Connected
    val bluetoothPermissionGranted = hasBluetoothConnectPermission()

    val scroll = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PremiumScreenBackdrop(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            SettingsHeroHeader(language = language, onBack = onBack)

            Spacer(modifier = Modifier.height(18.dp))

            SettingsGlassSection {
                SectionTitleRow(
                    icon = Icons.Filled.Bluetooth,
                    title = localizedString(R.string.bluetooth_connection_title, language),
                    trailing = {
                        BluetoothStatusPill(
                            language = language,
                            state = connectionState,
                            compact = true
                        )
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                ConnectionSection(
                    language = language,
                    bondedDevices = bondedDevices,
                    selectedDeviceAddress = selectedDeviceAddress,
                    onSelectedDeviceAddressChange = { selectedDeviceAddress = it },
                    connectionState = connectionState,
                    bluetoothPermissionGranted = bluetoothPermissionGranted,
                    onConnectClicked = {
                        if (selectedDevice != null) {
                            if (!hasBluetoothConnectPermission()) {
                                ensurePermissionAndLoad()
                                return@ConnectionSection
                            }
                            BluetoothConnectionController.connect(selectedDevice)
                        }
                    },
                    onDisconnectClicked = { BluetoothConnectionController.disconnect() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGlassSection {
                SectionTitleRow(
                    icon = Icons.Filled.Tune,
                    title = localizedString(R.string.reader_settings_title, language)
                )
                Spacer(modifier = Modifier.height(14.dp))

                AntennaPowerSelectorRow(
                    language = language,
                    ant1 = ant1,
                    ant2 = ant2,
                    ant3 = ant3,
                    ant4 = ant4,
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

                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val isWide = configuration.smallestScreenWidthDp >= 600

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

                Button(
                    onClick = {
                        val weightFlag = if (weightEnabled) 1 else 0
                        val cmd =
                            """{"Status":["ReaderSettings","5","$ant1","$ant2","$ant3","$ant4","$packCloseTimeout","$weightFlag"]}"""
                        BluetoothConnectionController.sendCommand(cmd, R.string.settings_saved)
                        userPrefs.saveReaderSettings(
                            ant1 = ant1,
                            ant2 = ant2,
                            ant3 = ant3,
                            ant4 = ant4,
                            packetCloseTimeout = packCloseTimeout,
                            weightEnabled = weightEnabled,
                            barcodeEnabled = barcodeEnabled
                        )
                        userPrefs.saveSelectedBluetoothDeviceAddress(selectedDeviceAddress)
                    },
                    enabled = sendEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = ExecutiveMuted.copy(alpha = 0.35f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
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
                        text = localizedString(R.string.settings_save, language),
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun BluetoothStatusPill(
    language: AppLanguage,
    state: BluetoothConnectionState,
    compact: Boolean = false
) {
    val (bg, fg, dot, label) = when (state) {
        is BluetoothConnectionState.Connected -> {
            Quadruple(
                LiveEmerald.copy(alpha = 0.12f),
                LiveEmerald,
                LiveEmerald,
                "${localizedString(R.string.bluetooth_connected, language)} · ${state.deviceName}"
            )
        }
        BluetoothConnectionState.Connecting -> {
            Quadruple(
                PrimaryBlue.copy(alpha = 0.1f),
                PrimaryBlue,
                PrimaryBlue,
                localizedString(R.string.bluetooth_connecting, language)
            )
        }
        is BluetoothConnectionState.Error -> {
            Quadruple(
                Color(0xFFFFE4E6),
                Color(0xFFBE123C),
                Color(0xFFBE123C),
                state.message.ifBlank { localizedString(R.string.bluetooth_connect_failed, language) }
            )
        }
        BluetoothConnectionState.Disconnected -> {
            Quadruple(
                Color(0xFFF1F5F9),
                ExecutiveMuted,
                ExecutiveMuted.copy(alpha = 0.5f),
                localizedString(R.string.bluetooth_disconnected, language)
            )
        }
    }

    val rowModifier = Modifier
        .then(
            if (compact) {
                Modifier.widthIn(max = 220.dp)
            } else {
                Modifier.fillMaxWidth()
            }
        )
        .clip(RoundedCornerShape(100.dp))
        .background(bg)
        .border(1.dp, fg.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
        .padding(
            horizontal = if (compact) 10.dp else 14.dp,
            vertical = if (compact) 8.dp else 10.dp
        )

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        if (state is BluetoothConnectionState.Connected) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Filled.BluetoothConnected,
                contentDescription = null,
                tint = fg.copy(alpha = 0.85f),
                modifier = Modifier.size(if (compact) 18.dp else 20.dp)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun ConnectionSection(
    language: AppLanguage,
    bondedDevices: List<BluetoothDevice>,
    selectedDeviceAddress: String?,
    onSelectedDeviceAddressChange: (String?) -> Unit,
    connectionState: BluetoothConnectionState,
    bluetoothPermissionGranted: Boolean,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit
) {
    if (bondedDevices.isEmpty()) {
        Text(
            text = if (bluetoothPermissionGranted) {
                localizedString(R.string.bluetooth_no_bonded_devices, language)
            } else {
                localizedString(R.string.bluetooth_permission_required, language)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = ExecutiveMuted
        )
        return
    }

    var expanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = SettingsCardBorder,
        focusedLabelColor = PrimaryBlue,
        cursorColor = PrimaryBlue
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            val selected = bondedDevices.firstOrNull { it.address == selectedDeviceAddress }
            val deviceName = try {
                if (bluetoothPermissionGranted) selected?.name else null
            } catch (_: SecurityException) {
                null
            }

            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = deviceName ?: localizedString(R.string.bluetooth_select_device, language),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = {
                    Text(
                        text = localizedString(R.string.bluetooth_select_device, language),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                bondedDevices.forEach { device ->
                    val name = try {
                        if (bluetoothPermissionGranted) device.name else "Bluetooth"
                    } catch (_: SecurityException) {
                        "Bluetooth"
                    }
                    val address = device.address ?: ""

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$name  ·  $address",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onSelectedDeviceAddressChange(device.address)
                            expanded = false
                        }
                    )
                }
            }
        }

        when (connectionState) {
            is BluetoothConnectionState.Connected -> {
                Button(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 120.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEFF4FF),
                        contentColor = PrimaryBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    onClick = onDisconnectClicked
                ) {
                    Text(
                        text = localizedString(R.string.bluetooth_disconnect, language),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(min = 120.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        onClick = onConnectClicked,
                        enabled = connectionState !is BluetoothConnectionState.Connecting
                    ) {
                        when (connectionState) {
                            BluetoothConnectionState.Connecting ->
                                Text(
                                    text = localizedString(R.string.bluetooth_connecting, language),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            else ->
                                Text(
                                    text = localizedString(R.string.bluetooth_connect, language),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                        }
                    }
                    if (connectionState is BluetoothConnectionState.Connecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = PrimaryBlue,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AntennaPowerSelectorRow(
    language: AppLanguage,
    ant1: Int,
    ant2: Int,
    ant3: Int,
    ant4: Int,
    onAnt1Changed: (Int) -> Unit,
    onAnt2Changed: (Int) -> Unit,
    onAnt3Changed: (Int) -> Unit,
    onAnt4Changed: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(
                    language = language,
                    titleResId = R.string.antenna_1,
                    value = ant1,
                    onValueChange = onAnt1Changed
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(
                    language = language,
                    titleResId = R.string.antenna_2,
                    value = ant2,
                    onValueChange = onAnt2Changed
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(
                    language = language,
                    titleResId = R.string.antenna_3,
                    value = ant3,
                    onValueChange = onAnt3Changed
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                AntennaPowerSelector(
                    language = language,
                    titleResId = R.string.antenna_4,
                    value = ant4,
                    onValueChange = onAnt4Changed
                )
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .heightIn(min = 62.dp),
            value = value.toString(),
            onValueChange = { _ -> },
            readOnly = true,
            singleLine = true,
            label = { Text(text = localizedString(titleResId, language), maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (0..30).forEach { p ->
                DropdownMenuItem(
                    text = { Text(text = p.toString()) },
                    onClick = {
                        onValueChange(p)
                        expanded = false
                    }
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .heightIn(min = 62.dp),
            value = value.toString(),
            onValueChange = { _ -> },
            readOnly = true,
            singleLine = true,
            label = { Text(text = localizedString(R.string.packet_close_time, language), maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..10).forEach { t ->
                DropdownMenuItem(
                    text = { Text(text = t.toString()) },
                    onClick = {
                        onValueChange(t)
                        expanded = false
                    }
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
