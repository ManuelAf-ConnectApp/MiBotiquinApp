package com.connectapp.presentation.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
actual fun rememberCodeScanner(): CodeScanner {
    val context = LocalContext.current
    return remember(context) {
        object : CodeScanner {
            private var active = false
            private val scanner by lazy { GmsBarcodeScanning.getClient(context, GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_DATA_MATRIX)
                .enableAutoZoom().build()) }
            override fun launch(onResult: (CodeScanResult) -> Unit) {
                if (active) return
                active = true
                fun complete(result: CodeScanResult) { if (active) { active = false; onResult(result) } }
                try { scanner.startScan()
                    .addOnSuccessListener { barcode -> complete(barcode.rawValue?.let { CodeScanResult.Read(it) } ?: CodeScanResult.Unavailable) }
                    .addOnCanceledListener { complete(CodeScanResult.Cancelled) }
                    .addOnFailureListener { complete(CodeScanResult.Unavailable) }
                } catch (_: Exception) { complete(CodeScanResult.Unavailable) }
            }
            override fun close() { active = false }
        }
    }
}
