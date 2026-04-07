package com.takipsanplus.rfidtablet.data.local

import android.content.Context

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("rfid_prefs", Context.MODE_PRIVATE)

    fun saveRememberedCredentials(username: String, password: String) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, true)
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun clearRememberedCredentials() {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, false)
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }

    fun getRememberedCredentials(): RememberedCredentials {
        return RememberedCredentials(
            rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false),
            username = prefs.getString(KEY_USERNAME, "").orEmpty(),
            password = prefs.getString(KEY_PASSWORD, "").orEmpty()
        )
    }

    fun saveLanguageCode(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE_CODE, languageCode).apply()
    }

    fun getLanguageCode(defaultCode: String): String {
        return prefs.getString(KEY_LANGUAGE_CODE, defaultCode).orEmpty()
    }

    data class ReaderSettings(
        val ant1: Int,
        val ant2: Int,
        val ant3: Int,
        val ant4: Int,
        val packetCloseTimeout: Int,
        val weightEnabled: Boolean,
        /** Shipment: require QR scan before EPC read when true (stored key remains barcode_enabled). */
        val barcodeEnabled: Boolean
    )

    fun saveReaderSettings(
        ant1: Int,
        ant2: Int,
        ant3: Int,
        ant4: Int,
        packetCloseTimeout: Int,
        weightEnabled: Boolean,
        barcodeEnabled: Boolean
    ) {
        prefs.edit()
            .putInt(KEY_ANT1, ant1)
            .putInt(KEY_ANT2, ant2)
            .putInt(KEY_ANT3, ant3)
            .putInt(KEY_ANT4, ant4)
            .putInt(KEY_PACKET_CLOSE_TIMEOUT, packetCloseTimeout)
            .putBoolean(KEY_WEIGHT_ENABLED, weightEnabled)
            .putBoolean(KEY_BARCODE_ENABLED, barcodeEnabled)
            .apply()
    }

    fun getReaderSettings(default: ReaderSettings): ReaderSettings {
        return ReaderSettings(
            ant1 = prefs.getInt(KEY_ANT1, default.ant1),
            ant2 = prefs.getInt(KEY_ANT2, default.ant2),
            ant3 = prefs.getInt(KEY_ANT3, default.ant3),
            ant4 = prefs.getInt(KEY_ANT4, default.ant4),
            packetCloseTimeout = prefs.getInt(KEY_PACKET_CLOSE_TIMEOUT, default.packetCloseTimeout),
            weightEnabled = prefs.getBoolean(KEY_WEIGHT_ENABLED, default.weightEnabled),
            barcodeEnabled = prefs.getBoolean(KEY_BARCODE_ENABLED, default.barcodeEnabled)
        )
    }

    fun saveSelectedBluetoothDeviceAddress(address: String?) {
        prefs.edit()
            .putString(KEY_BT_ADDRESS, address)
            .apply()
    }

    fun getSelectedBluetoothDeviceAddress(): String? {
        return prefs.getString(KEY_BT_ADDRESS, null)
    }

    /** Session after login + device selection (used for API calls such as consignments). */
    fun saveSession(token: String, companyId: Int) {
        prefs.edit()
            .putString(KEY_SESSION_TOKEN, token.trim())
            .putInt(KEY_SESSION_COMPANY_ID, companyId)
            .apply()
    }

    fun getSessionToken(): String? {
        return prefs.getString(KEY_SESSION_TOKEN, null)?.takeIf { it.isNotBlank() }
    }

    fun getSessionCompanyId(): Int? {
        val id = prefs.getInt(KEY_SESSION_COMPANY_ID, -1)
        return if (id >= 0) id else null
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_SESSION_COMPANY_ID)
            .apply()
    }

    data class RememberedCredentials(
        val rememberMe: Boolean,
        val username: String,
        val password: String
    )

    private companion object {
        const val KEY_REMEMBER_ME = "remember_me"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val KEY_LANGUAGE_CODE = "language_code"

        const val KEY_BT_ADDRESS = "bt_address"

        const val KEY_ANT1 = "ant1"
        const val KEY_ANT2 = "ant2"
        const val KEY_ANT3 = "ant3"
        const val KEY_ANT4 = "ant4"
        const val KEY_PACKET_CLOSE_TIMEOUT = "packet_close_timeout"
        const val KEY_WEIGHT_ENABLED = "weight_enabled"
        const val KEY_BARCODE_ENABLED = "barcode_enabled"

        const val KEY_SESSION_TOKEN = "session_token"
        const val KEY_SESSION_COMPANY_ID = "session_company_id"
    }
}
