package com.takipsanplus.rfidtablet.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.StringRes
import com.takipsanplus.rfidtablet.presentation.common.UiMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

sealed interface BluetoothConnectionState {
    data object Disconnected : BluetoothConnectionState
    data object Connecting : BluetoothConnectionState
    data class Connected(val deviceName: String, val deviceAddress: String) : BluetoothConnectionState
    data class Error(val message: String) : BluetoothConnectionState
}

object BluetoothConnectionController {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private const val TAG = "RFIDTabletBT"

    private var appContext: Context? = null
    private var receiverRegistered = false

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _state = MutableStateFlow<BluetoothConnectionState>(BluetoothConnectionState.Disconnected)
    val state: StateFlow<BluetoothConnectionState> = _state

    private val _messages = MutableSharedFlow<UiMessage>()
    val messages = _messages

    private var currentSocket: BluetoothSocket? = null
    private var currentDeviceAddress: String? = null

    private val _epcEvents = MutableSharedFlow<String>(extraBufferCapacity = 128)
    val epcEvents: SharedFlow<String> = _epcEvents.asSharedFlow()

    /** QR / barkod metni (`"Barcode":"..."`); sevkiyat QR onay akışı için. */
    private val _barcodeEvents = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val barcodeEvents: SharedFlow<String> = _barcodeEvents.asSharedFlow()

    private var readJob: Job? = null

    fun init(context: Context) {
        if (receiverRegistered) return
        appContext = context.applicationContext

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }

        // Note: receiver still needs BLUETOOTH_CONNECT on Android 12+
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
                val address = device.address ?: return

                if (currentDeviceAddress == null || currentDeviceAddress != address) return

                when (intent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        // The actual connected state is usually handled in connect() after socket connect.
                        // Here we only keep UI consistent.
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        currentSocket?.closeQuietly()
                        currentSocket = null
                        currentDeviceAddress = null
                        _state.value = BluetoothConnectionState.Disconnected
                        scope.launch {
                            _messages.emit(UiMessage.Resource(com.takipsanplus.rfidtablet.R.string.bt_disconnected))
                        }
                    }
                }
            }
        }

        try {
            appContext?.registerReceiver(receiver, filter)
            receiverRegistered = true
        } catch (e: SecurityException) {
            receiverRegistered = false
        }
    }

    fun connect(device: BluetoothDevice) {
        val address = device.address ?: return

        currentDeviceAddress = address

        _state.value = BluetoothConnectionState.Connecting
        scope.launch {
            try {
                currentSocket?.closeQuietly()
                currentSocket = null

                val socket = device.createRfcommSocketToServiceRecord(sppUuid)
                currentSocket = socket

                socket.connect()

                _state.value = BluetoothConnectionState.Connected(
                    deviceName = device.name ?: "Bluetooth Device",
                    deviceAddress = address
                )

                _messages.emit(UiMessage.Resource(com.takipsanplus.rfidtablet.R.string.bt_connected))

                startReadingFromSocket(socket)
            } catch (e: SecurityException) {
                _state.value = BluetoothConnectionState.Error(e.message.orEmpty())
                _messages.emit(UiMessage.Resource(com.takipsanplus.rfidtablet.R.string.bt_permission_missing))
            } catch (e: IOException) {
                _state.value = BluetoothConnectionState.Error(e.message.orEmpty())
                _messages.emit(UiMessage.Resource(com.takipsanplus.rfidtablet.R.string.bluetooth_connect_failed))
            } catch (e: Throwable) {
                _state.value = BluetoothConnectionState.Error(e.message.orEmpty())
                _messages.emit(UiMessage.Resource(com.takipsanplus.rfidtablet.R.string.bt_connection_error))
            }
        }
    }

    fun disconnect() {
        scope.launch {
            readJob?.cancel()
            readJob = null
            currentSocket?.closeQuietly()
            currentSocket = null
            currentDeviceAddress = null
            _state.value = BluetoothConnectionState.Disconnected
        }
    }

    fun sendCommand(
        commandJson: String,
        @StringRes successMessageResId: Int? = null
    ) {
        val socket = currentSocket
        if (socket == null) {
            scope.launch {
                _messages.emit(UiMessage.Resource(com.takipsanplus.rfidtablet.R.string.bt_not_connected))
            }
            return
        }

        scope.launch {
            try {
                val out = socket.outputStream
                val payload = (commandJson + "\n").toByteArray(Charsets.UTF_8)
                out.write(payload)
                out.flush()
                if (successMessageResId != null) {
                    _messages.emit(UiMessage.Resource(successMessageResId))
                }
            } catch (e: IOException) {
                _state.value = BluetoothConnectionState.Error(e.message.orEmpty())
                _messages.emit(UiMessage.Resource(com.takipsanplus.rfidtablet.R.string.bt_send_failed))
            }
        }
    }

    private fun BluetoothSocket.closeQuietly() {
        try {
            close()
        } catch (_: Throwable) {
        }
    }

    private fun startReadingFromSocket(socket: BluetoothSocket) {
        readJob?.cancel()
        readJob = scope.launch {
            val reader = InputStreamReader(socket.inputStream, Charsets.UTF_8)
            val buffer = CharArray(4096)
            val rxBuilder = StringBuilder()

            Log.d(TAG, "Reading job started (device=$currentDeviceAddress)")
            while (isActive) {
                val count = try {
                    reader.read(buffer)
                } catch (_: Throwable) {
                    break
                }

                if (count <= 0) break

                val chunk = String(buffer, 0, count)
                rxBuilder.append(chunk)

                if (rxBuilder.length > 16_000) {
                    rxBuilder.delete(0, rxBuilder.length - 16_000)
                }

                val text = rxBuilder.toString()
                if (text.contains("epc", ignoreCase = true)) {
                    Log.d(TAG, "RX chunk contains epc (len=${text.length}): ${text.take(450)}")
                }
                if (text.contains("barcode", ignoreCase = true)) {
                    Log.d(TAG, "RX chunk contains barcode (len=${text.length}): ${text.take(450)}")
                }

                val epcs = parseEpcsFromText(text)
                if (epcs.isNotEmpty()) {
                    Log.d(TAG, "Parsed EPCs (${epcs.size}): ${epcs.joinToString(limit = 120)}")
                    epcs.forEach { epc -> _epcEvents.emit(epc) }
                }

                val barcodes = parseBarcodesFromText(text)
                if (barcodes.isNotEmpty()) {
                    Log.d(TAG, "Parsed Barcode payloads (${barcodes.size}): ${barcodes.joinToString(limit = 120)}")
                    barcodes.forEach { b -> _barcodeEvents.emit(b) }
                }
            }
        }
    }

    private fun parseEpcsFromText(rawText: String): List<String> {
        val text = rawText.trim()
        if (text.isEmpty()) return emptyList()

        // Fast path: regex extraction works even if JSON is streaming/partial.
        val stringValueRegex = Regex("\"epc\"\\s*:\\s*\"([^\"]+)\"")
        val objectValueRegex = Regex("\"epc\"\\s*:\\s*\\{[^}]*\"value\"\\s*:\\s*\"([^\"]+)\"")
        val arrayRegex = Regex("\"epc\"\\s*:\\s*\\[([^\\]]+)\\]")

        val out = ArrayList<String>()
        stringValueRegex.findAll(text).forEach { m ->
            val v = m.groupValues.getOrNull(1).orEmpty()
            if (v.isNotBlank()) out.add(v)
        }
        objectValueRegex.findAll(text).forEach { m ->
            val v = m.groupValues.getOrNull(1).orEmpty()
            if (v.isNotBlank()) out.add(v)
        }
        arrayRegex.findAll(text).forEach { m ->
            val inner = m.groupValues.getOrNull(1).orEmpty()
            // Extract quoted strings inside array.
            Regex("\"([^\"]+)\"").findAll(inner).forEach { mm ->
                val v = mm.groupValues.getOrNull(1).orEmpty()
                if (v.isNotBlank()) out.add(v)
            }
        }

        if (out.isNotEmpty()) return out

        // Fallback: attempt strict JSON parse if we got lucky.
        try {
            val json = JSONObject(text)
            if (!json.has("epc")) return emptyList()
            val epcAny = json.get("epc")
            return when (epcAny) {
                is String -> listOf(epcAny)
                is JSONArray -> {
                    val arrOut = ArrayList<String>(epcAny.length())
                    for (i in 0 until epcAny.length()) {
                        val v = epcAny.opt(i)
                        if (v is String && v.isNotBlank()) arrOut.add(v)
                    }
                    arrOut
                }
                is JSONObject -> {
                    val value = epcAny.optString("value", "").ifBlank { epcAny.optString("epc", "") }
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                else -> emptyList()
            }
        } catch (_: Throwable) {
            return emptyList()
        }
    }

    /** `"Barcode":"..."` veya `barcode` anahtarı — EPC ile aynı regex / JSON stratejisi. */
    private fun parseBarcodesFromText(rawText: String): List<String> {
        val text = rawText.trim()
        if (text.isEmpty()) return emptyList()

        val stringValueRegex = Regex("(?i)\"barcode\"\\s*:\\s*\"([^\"]+)\"")
        val objectValueRegex =
            Regex("(?i)\"barcode\"\\s*:\\s*\\{[^}]*\"value\"\\s*:\\s*\"([^\"]+)\"")
        val arrayRegex = Regex("(?i)\"barcode\"\\s*:\\s*\\[([^\\]]+)\\]")

        val out = ArrayList<String>()
        stringValueRegex.findAll(text).forEach { m ->
            val v = m.groupValues.getOrNull(1).orEmpty()
            if (v.isNotBlank()) out.add(v)
        }
        objectValueRegex.findAll(text).forEach { m ->
            val v = m.groupValues.getOrNull(1).orEmpty()
            if (v.isNotBlank()) out.add(v)
        }
        arrayRegex.findAll(text).forEach { m ->
            val inner = m.groupValues.getOrNull(1).orEmpty()
            Regex("\"([^\"]+)\"").findAll(inner).forEach { mm ->
                val v = mm.groupValues.getOrNull(1).orEmpty()
                if (v.isNotBlank()) out.add(v)
            }
        }

        if (out.isNotEmpty()) return out

        try {
            val json = JSONObject(text)
            val key = json.keys().asSequence().firstOrNull { it.equals("barcode", ignoreCase = true) }
                ?: return emptyList()
            val barcodeAny = json.get(key)
            return when (barcodeAny) {
                is String -> if (barcodeAny.isNotBlank()) listOf(barcodeAny) else emptyList()
                is JSONArray -> {
                    val arrOut = ArrayList<String>(barcodeAny.length())
                    for (i in 0 until barcodeAny.length()) {
                        val v = barcodeAny.opt(i)
                        if (v is String && v.isNotBlank()) arrOut.add(v)
                    }
                    arrOut
                }
                is JSONObject -> {
                    val value = barcodeAny.optString("value", "").ifBlank { barcodeAny.optString("barcode", "") }
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                else -> emptyList()
            }
        } catch (_: Throwable) {
            return emptyList()
        }
    }
}
