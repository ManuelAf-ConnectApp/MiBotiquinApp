package com.connectapp.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.*
import com.connectapp.domain.catalogue.SpanishMedicationCode
import com.connectapp.domain.catalogue.Gtin
import com.connectapp.domain.model.CatalogPresentation
import com.connectapp.domain.repository.CatalogueRepository
import com.connectapp.presentation.util.UiText
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanState(
    val code: String = "", val result: CatalogPresentation? = null,
    val confirmed: Boolean = false, val loading: Boolean = false, val cameraActive: Boolean = false,
    val message: UiText? = null, val recognizedGtin: String? = null
)
class ScanViewModel(private val catalogue: CatalogueRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ScanState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null
    fun code(value: String) {
        if (state.value.loading || state.value.cameraActive) return
        mutableState.value = ScanState(code = value)
    }
    fun cameraStarted() { mutableState.value = state.value.copy(cameraActive = true, message = null, result = null, confirmed = false) }
    fun cameraResult(result: CodeScanResult) {
        mutableState.value = when (result) {
            is CodeScanResult.Read -> {
                val code = SpanishMedicationCode.nationalCode(result.value)
                when {
                    code != null -> ScanState(code = code)
                    Gtin.normalize(result.value) != null -> ScanState(recognizedGtin = Gtin.normalize(result.value), message = UiText.StringRes(scan_gtin_recognized))
                    else -> ScanState(message = UiText.StringRes(scan_unknown_code))
                }
            }
            CodeScanResult.Cancelled -> state.value.copy(cameraActive = false)
            CodeScanResult.PermissionDenied -> state.value.copy(cameraActive = false, message = UiText.StringRes(scan_camera_denied))
            CodeScanResult.Unavailable -> state.value.copy(cameraActive = false, message = UiText.StringRes(scan_camera_unavailable))
        }
    }
    fun confirmed(value: Boolean) { mutableState.value = state.value.copy(confirmed = value) }
    fun lookup() {
        val current = state.value
        if (current.loading || current.cameraActive) return
        val code = SpanishMedicationCode.nationalCode(current.code)
        if (code == null) { mutableState.value = current.copy(message = UiText.StringRes(scan_unknown_code)); return }
        mutableState.value = ScanState(code = code, loading = true)
        job = viewModelScope.launch {
            catalogue.presentation(code)
                .onSuccess { mutableState.value = ScanState(code = code, result = it, message = if (it == null) UiText.StringRes(scan_not_found) else null) }
                .onFailure { mutableState.value = ScanState(code = code, message = it.asUiText()) }
        }
    }
    fun clear() { job?.cancel(); mutableState.value = ScanState() }
}
