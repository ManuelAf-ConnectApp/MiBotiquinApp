package com.connectapp.presentation.scan

sealed interface CodeScanResult {
    data class Read(val value: String) : CodeScanResult
    data object Cancelled : CodeScanResult
    data object PermissionDenied : CodeScanResult
    data object Unavailable : CodeScanResult
}
interface CodeScanner {
    fun launch(onResult: (CodeScanResult) -> Unit)
    fun close()
}
