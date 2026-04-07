package com.takipsanplus.rfidtablet.presentation.common

import android.app.Activity
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun scanQrWithGoogleCodeScanner(activity: Activity): String? =
    suspendCancellableCoroutine { cont ->
        val client = GmsBarcodeScanning.getClient(activity)
        client.startScan()
            .addOnCompleteListener { task ->
                if (!cont.isActive) return@addOnCompleteListener
                val barcode = if (task.isSuccessful) task.result else null
                cont.resume(barcode?.rawValue?.trim()?.takeIf { it.isNotEmpty() })
            }
    }
