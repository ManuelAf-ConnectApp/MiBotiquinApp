package com.connectapp.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.domain.model.CatalogPresentation
import com.connectapp.presentation.component.*
import com.connectapp.presentation.dose.doseTimeLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanScreen(modifier: Modifier = Modifier, onConfirm: (CatalogPresentation) -> Unit, onManual: () -> Unit) {
    val viewModel: ScanViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scanner = rememberCodeScanner()
    DisposableEffect(viewModel, scanner) { onDispose { scanner.close(); viewModel.clear() } }
    AuthLayout(modifier) {
        SectionIntro(stringResource(scan_heading), stringResource(scan_intro), Icons.Default.QrCodeScanner)
        InformationPanel(stringResource(scan_camera_explanation), Icons.Default.Info)
        PrimaryActionButton(stringResource(scan_camera_action), {
            viewModel.cameraStarted()
            scanner.launch(viewModel::cameraResult)
        }, enabled = !state.loading, busy = state.cameraActive)
        AppTextField(state.code, viewModel::code, stringResource(scan_cn_label), keyboardType = KeyboardType.Number,
            enabled = !state.loading && !state.cameraActive)
        Text(stringResource(scan_online_explanation), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = viewModel::lookup, enabled = !state.loading && !state.cameraActive && state.code.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(scan_lookup))
        }
        if (state.loading) CircularProgressIndicator()
        state.message?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
        state.result?.let { result ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Dimen20), verticalArrangement = Arrangement.spacedBy(Dimen12)) {
                    Text(result.name, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(scan_cn_value, result.nationalCode))
                    if (result.commercialized == false) Text(stringResource(scan_not_commercialized), color = MaterialTheme.colorScheme.error)
                    Text(stringResource(scan_source_attribution), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(scan_retrieved_at, doseTimeLabel(result.retrievedAt)), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(scan_prefill_limits), style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.fillMaxWidth().toggleable(state.confirmed, role = Role.Checkbox, onValueChange = viewModel::confirmed),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(state.confirmed, onCheckedChange = null)
                        Text(stringResource(scan_confirm_match), modifier = Modifier.weight(1f))
                    }
                    PrimaryActionButton(stringResource(scan_use_match), { onConfirm(result) }, enabled = state.confirmed)
                }
            }
        }
        TextButton(onClick = onManual, enabled = !state.loading && !state.cameraActive, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(scan_manual))
        }
    }
}
