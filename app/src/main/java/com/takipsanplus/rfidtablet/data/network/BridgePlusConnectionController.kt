package com.takipsanplus.rfidtablet.data.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.takipsanplus.rfidtablet.presentation.common.UiMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class ConnectionType { WebSocket, Ble }

sealed interface DeviceConnectionState {
    data object Disconnected : DeviceConnectionState
    data object Connecting : DeviceConnectionState
    data class Connected(
        val deviceName: String,
        val deviceIp: String,
        val connectionType: ConnectionType = ConnectionType.WebSocket
    ) : DeviceConnectionState
    data class Error(val message: String) : DeviceConnectionState
}

object BridgePlusConnectionController {
    private const val TAG = "BridgePlus"
    private const val PORT = 5000

    // Nordic UART Service — ilk denenecek servis
    private val NORDIC_UART_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val NORDIC_UART_TX     = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e") // cihaz→telefon (notify)
    private val NORDIC_UART_RX     = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e") // telefon→cihaz (write)
    private val CLIENT_CHAR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Disconnected)
    val state: StateFlow<DeviceConnectionState> = _state

    private val _messages = MutableSharedFlow<UiMessage>()
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    private val _epcEvents = MutableSharedFlow<String>(extraBufferCapacity = 128)
    val epcEvents: SharedFlow<String> = _epcEvents.asSharedFlow()

    private val _barcodeEvents = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val barcodeEvents: SharedFlow<String> = _barcodeEvents.asSharedFlow()

    // WebSocket state
    private var webSocket: WebSocket? = null
    private var currentIp: String? = null

    // BLE state
    private var currentGatt: BluetoothGatt? = null
    private var bleNotifyChar: BluetoothGattCharacteristic? = null
    private var bleWriteChar: BluetoothGattCharacteristic? = null
    private val bleRxBuffer = StringBuilder()

    // ─────────────────────── WebSocket ───────────────────────

    fun connect(ipAddress: String) {
        if (ipAddress.isBlank()) return
        disconnectBleInternal()
        currentIp = ipAddress
        _state.value = DeviceConnectionState.Connecting

        // Cihaza WebSocket moduna geçmesini söyle, ardından bağlan
        scope.launch {
            setProtocol(ipAddress, "WEB")
            openWebSocket(ipAddress)
        }
    }

    private fun openWebSocket(ipAddress: String) {
        val request = Request.Builder()
            .url("ws://$ipAddress:$PORT/rfid/stream")
            .build()

        webSocket?.cancel()
        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WS onOpen: $ipAddress")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "WS msg: ${text.take(300)}")
                handleWsMessage(text, ipAddress)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WS failure: ${t.message}")
                _state.value = DeviceConnectionState.Error(t.message.orEmpty())
                scope.launch { _messages.emit(UiMessage.Text("Bağlantı hatası: ${t.message}")) }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WS closed: $code $reason")
                if (_state.value !is DeviceConnectionState.Error) {
                    _state.value = DeviceConnectionState.Disconnected
                }
            }
        })
    }

    private fun handleWsMessage(text: String, ip: String) {
        try {
            val json = JSONObject(text)
            if (json.has("code") && json.optString("code") == "DEVICE_INFO") {
                val deviceName = json.optJSONObject("data")
                    ?.optString("device_name").takeIf { !it.isNullOrBlank() } ?: ip
                _state.value = DeviceConnectionState.Connected(deviceName, ip, ConnectionType.WebSocket)
                scope.launch { _messages.emit(UiMessage.Text("$deviceName bağlandı")) }
                return
            }
            when (json.optString("type")) {
                "RFID_READ"    -> handleRfidRead(json)
                "BARCODE_READ" -> {
                    val b = json.optJSONObject("data")?.optString("barcode").orEmpty().trim()
                    if (b.isNotBlank()) scope.launch { _barcodeEvents.emit(b) }
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "WS parse error: ${e.message}")
        }
    }

    // ─────────────────────── BLE ───────────────────────

    fun connectBle(device: BluetoothDevice, context: Context, deviceIp: String = "") {
        webSocket?.cancel()
        webSocket = null
        currentIp = null
        bleRxBuffer.clear()
        disconnectBleInternal()
        _state.value = DeviceConnectionState.Connecting

        // IP biliniyorsa cihaza BT moduna geçmesini söyle
        if (deviceIp.isNotBlank()) {
            scope.launch { setProtocol(deviceIp, "BT") }
        }

        try {
            currentGatt = device.connectGatt(
                context, false, bleGattCallback, BluetoothDevice.TRANSPORT_LE
            )
        } catch (e: SecurityException) {
            _state.value = DeviceConnectionState.Error("BLE izni yok")
        }
    }

    private val bleGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "BLE bağlandı, MTU isteniyor")
                    try { gatt.requestMtu(512) } catch (_: SecurityException) { gatt.discoverServices() }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "BLE koptu: status=$status")
                    currentGatt = null
                    bleNotifyChar = null
                    bleWriteChar = null
                    if (_state.value !is DeviceConnectionState.Error) {
                        _state.value = DeviceConnectionState.Disconnected
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(TAG, "MTU → $mtu (status=$status)")
            try { gatt.discoverServices() } catch (_: SecurityException) {}
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "Servis keşfi başarısız: $status")
                return
            }

            // Tüm servisleri logla — UUID'ler buradan okunabilir
            gatt.services.forEach { svc ->
                Log.i(TAG, "BLE Servis: ${svc.uuid}")
                svc.characteristics.forEach { c ->
                    Log.i(TAG, "  Karakteristik: ${c.uuid}  props=0x${c.properties.toString(16)}")
                }
            }

            // 1. Nordic UART dene
            val nordicSvc = gatt.getService(NORDIC_UART_SERVICE)
            if (nordicSvc != null) {
                bleNotifyChar = nordicSvc.getCharacteristic(NORDIC_UART_TX)
                bleWriteChar  = nordicSvc.getCharacteristic(NORDIC_UART_RX)
                Log.d(TAG, "Nordic UART bulundu")
            } else {
                // 2. İlk notify + write çiftini bul
                for (svc in gatt.services) {
                    if (bleNotifyChar == null)
                        bleNotifyChar = svc.characteristics.firstOrNull {
                            it.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
                        }
                    if (bleWriteChar == null)
                        bleWriteChar = svc.characteristics.firstOrNull {
                            (it.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) ||
                            (it.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0)
                        }
                }
                Log.d(TAG, "Genel notify: ${bleNotifyChar?.uuid}, write: ${bleWriteChar?.uuid}")
            }

            // Bildirimleri etkinleştir
            val notifyChar = bleNotifyChar
            if (notifyChar != null) {
                try {
                    gatt.setCharacteristicNotification(notifyChar, true)
                    val desc = notifyChar.getDescriptor(CLIENT_CHAR_CONFIG)
                    if (desc != null) {
                        @Suppress("DEPRECATION")
                        desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        @Suppress("DEPRECATION")
                        gatt.writeDescriptor(desc)
                    }
                } catch (_: SecurityException) {}
            }

            val deviceName = try { gatt.device.name } catch (_: SecurityException) { null }
            val label = deviceName ?: gatt.device.address
            _state.value = DeviceConnectionState.Connected(label, gatt.device.address, ConnectionType.Ble)
            scope.launch { _messages.emit(UiMessage.Text("$label bağlandı (BLE)")) }
        }

        // API < 33
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleBleBytes(characteristic.value ?: return)
        }

        // API 33+
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleBleBytes(value)
        }
    }

    private fun handleBleBytes(bytes: ByteArray) {
        val chunk = bytes.toString(Charsets.UTF_8)
        bleRxBuffer.append(chunk)

        val text = bleRxBuffer.toString()

        // WebSocket JSON formatını dene
        val trimmed = text.trim()
        if (trimmed.startsWith("{")) {
            try {
                val json = JSONObject(trimmed)
                when (json.optString("type")) {
                    "RFID_READ" -> { handleRfidRead(json); bleRxBuffer.clear(); return }
                    "BARCODE_READ" -> {
                        val b = json.optJSONObject("data")?.optString("barcode").orEmpty().trim()
                        if (b.isNotBlank()) scope.launch { _barcodeEvents.emit(b) }
                        bleRxBuffer.clear()
                        return
                    }
                }
                // Başka bir mesaj tipi ya da incomplete JSON — bekle
            } catch (_: Throwable) { /* JSON henüz tamamlanmadı */ }
        }

        // Regex fallback (eski BT formatı için)
        val epcs     = parseEpcsFromText(text)
        val barcodes = parseBarcodesFromText(text)
        epcs.forEach     { epc -> scope.launch { _epcEvents.emit(epc) } }
        barcodes.forEach { b   -> scope.launch { _barcodeEvents.emit(b) } }
        if (epcs.isNotEmpty() || barcodes.isNotEmpty()) {
            bleRxBuffer.clear()
        }

        if (bleRxBuffer.length > 16_000) {
            bleRxBuffer.delete(0, bleRxBuffer.length - 16_000)
        }
    }

    // ─────────────────────── Ortak ───────────────────────

    /**
     * BLE taramasından önce çağrılır.
     * Mevcut WebSocket bağlantısını kapatır ve cihaza HTTP üzerinden "BT" moduna
     * geçmesini söyler. Tamamlandığında çağıran taraf BLE taramasını başlatabilir.
     * IP bilinmiyorsa (blank) sadece WS kapatılır, protocol komutu atlanır.
     */
    suspend fun prepareForBle(deviceIp: String) {
        // WebSocket'i temizle
        webSocket?.close(1000, "BLE moduna geçiliyor")
        webSocket = null
        currentIp = null
        _state.value = DeviceConnectionState.Disconnected

        if (deviceIp.isNotBlank()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                setProtocol(deviceIp, "BT")
            }
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Kullanıcı bağlantıyı kesti")
        webSocket = null
        currentIp = null
        disconnectBleInternal()
        _state.value = DeviceConnectionState.Disconnected
    }

    fun startScan() {
        if (currentIp != null) {
            scope.launch { postScanAction(currentIp!!, "start") }
        } else {
            writeBleCommand("""{"action":"start"}""")
        }
    }

    fun stopScan() {
        if (currentIp != null) {
            scope.launch { postScanAction(currentIp!!, "stop") }
        } else {
            writeBleCommand("""{"action":"stop"}""")
        }
    }

    private fun disconnectBleInternal() {
        try {
            currentGatt?.disconnect()
            currentGatt?.close()
        } catch (_: SecurityException) {}
        currentGatt = null
        bleNotifyChar = null
        bleWriteChar = null
        bleRxBuffer.clear()
    }

    private fun writeBleCommand(command: String) {
        val gatt = currentGatt ?: return
        val char = bleWriteChar ?: return
        try {
            @Suppress("DEPRECATION")
            char.value = command.toByteArray(Charsets.UTF_8)
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(char)
        } catch (_: SecurityException) {}
    }

    /**
     * Dokümantasyon §3.2 — GET /api/takipsan/v1/rfid/config
     * Cihazın mevcut anten güçlerini ve okuyucu ayarlarını okur.
     * Bağlantı kurulduğunda UI'ı güncellemek için kullanılır.
     */
    data class DeviceReaderConfig(val ant1: Int, val ant2: Int, val ant3: Int, val ant4: Int)

    suspend fun readReaderConfig(ip: String): DeviceReaderConfig? {
        if (ip.isBlank()) return null
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("http://$ip:$PORT/api/takipsan/v1/rfid/config")
                    .get()
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    val data = json.optJSONObject("data") ?: return@withContext null
                    val powers = data.optJSONArray("antenna_powers") ?: return@withContext null
                    var a1 = 0; var a2 = 0; var a3 = 0; var a4 = 0
                    for (i in 0 until powers.length()) {
                        val item = powers.optJSONObject(i) ?: continue
                        when (item.optInt("port")) {
                            1 -> a1 = item.optDouble("power_dbm").toInt()
                            2 -> a2 = item.optDouble("power_dbm").toInt()
                            3 -> a3 = item.optDouble("power_dbm").toInt()
                            4 -> a4 = item.optDouble("power_dbm").toInt()
                        }
                    }
                    Log.d(TAG, "readReaderConfig → ant[$a1, $a2, $a3, $a4]")
                    DeviceReaderConfig(a1, a2, a3, a4)
                }
            } catch (e: Throwable) {
                Log.w(TAG, "readReaderConfig failed: ${e.message}")
                null
            }
        }
    }

    /**
     * Dokümantasyon §3.1 — PUT /api/takipsan/v1/rfid/config
     * Anten güçlerini ve okuyucu ayarlarını cihaza gönderir.
     * Ayarlar kaydedildiğinde çağrılır.
     */
    fun applyReaderConfig(ip: String, ant1: Int, ant2: Int, ant3: Int, ant4: Int) {
        if (ip.isBlank()) return
        scope.launch {
            try {
                val antennaPowers = JSONArray().apply {
                    listOf(1 to ant1, 2 to ant2, 3 to ant3, 4 to ant4).forEach { (port, power) ->
                        put(JSONObject().apply {
                            put("port", port)
                            put("power_dbm", power.toDouble())
                        })
                    }
                }
                val bodyStr = JSONObject().apply {
                    put("region", "EU")
                    put("session", 1)
                    put("report_rssi", false)
                    put("read_tid", false)
                    put("search_mode", "SingleTarget")
                    put("rf_mode", 1002)
                    put("antenna_powers", antennaPowers)
                }.toString()
                val request = Request.Builder()
                    .url("http://$ip:$PORT/api/takipsan/v1/rfid/config")
                    .put(bodyStr.toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(request).execute().use {
                    Log.d(TAG, "applyReaderConfig → ${it.code}")
                }
            } catch (e: Throwable) {
                Log.w(TAG, "applyReaderConfig failed: ${e.message}")
            }
        }
    }

    /** Cihazın streaming protokolünü değiştirir: "WEB" | "BT" | "USB" */
    private fun setProtocol(ip: String, protocol: String) {
        try {
            val body = """{"protocol":"$protocol"}""".toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://$ip:$PORT/api/takipsan/v1/rfid/protocol")
                .post(body)
                .build()
            httpClient.newCall(request).execute().use {
                Log.d(TAG, "setProtocol $protocol → ${it.code}")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "setProtocol failed: ${e.message}")
            // Hata olsa bile bağlantıya devam et
        }
    }

    private fun postScanAction(ip: String, action: String) {
        try {
            val body = """{"action":"$action"}""".toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://$ip:$PORT/api/takipsan/v1/rfid/scan")
                .post(body)
                .build()
            httpClient.newCall(request).execute().use {}
        } catch (e: Throwable) {
            Log.w(TAG, "scan $action failed: ${e.message}")
        }
    }

    private fun handleRfidRead(json: JSONObject) {
        when (val dataAny = json.opt("data")) {
            is JSONObject -> emitEpc(dataAny.optString("epc").trim())
            is JSONArray  -> {
                for (i in 0 until dataAny.length()) {
                    val epc = dataAny.optJSONObject(i)?.optString("epc")?.trim() ?: continue
                    emitEpc(epc)
                }
            }
        }
    }

    private fun emitEpc(epc: String) {
        if (epc.isBlank()) return
        scope.launch { _epcEvents.emit(epc) }
    }

    // ─── Regex parsers (BLE fallback) ───

    private fun parseEpcsFromText(rawText: String): List<String> {
        val text = rawText.trim()
        if (text.isEmpty()) return emptyList()
        val stringValueRegex = Regex("\"epc\"\\s*:\\s*\"([^\"]+)\"")
        val out = ArrayList<String>()
        stringValueRegex.findAll(text).forEach { m ->
            val v = m.groupValues.getOrNull(1).orEmpty()
            if (v.isNotBlank()) out.add(v)
        }
        return out
    }

    private fun parseBarcodesFromText(rawText: String): List<String> {
        val text = rawText.trim()
        if (text.isEmpty()) return emptyList()
        val stringValueRegex = Regex("(?i)\"barcode\"\\s*:\\s*\"([^\"]+)\"")
        val out = ArrayList<String>()
        stringValueRegex.findAll(text).forEach { m ->
            val v = m.groupValues.getOrNull(1).orEmpty()
            if (v.isNotBlank()) out.add(v)
        }
        return out
    }
}
