package com.connectapp.presentation.dose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.resources.dose_snooze
import com.connectapp.domain.model.DoseStatus
import com.connectapp.domain.model.DoseUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

fun doseTimeLabel(millis: Long): String {
    val zone = TimeZone.currentSystemDefault()
    return "${Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)} (${zone.id})"
}

@Composable
fun DoseScreen(eventId: String, modifier: Modifier = Modifier, onStock: (Long) -> Unit, onHome: () -> Unit) {
    val viewModel: DoseViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(eventId) { viewModel.load(eventId) }
    DisposableEffect(viewModel) { onDispose { viewModel.clear() } }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimen24), verticalArrangement = Arrangement.spacedBy(Dimen16)) {
        if (state.loading) CircularProgressIndicator()
        state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
        val dose = state.dose
        if (!state.loading && dose == null) Text(stringResource(dose_unavailable))
        if (dose != null) {
            Text(dose.medicationName, style = MaterialTheme.typography.titleLarge)
            Text(stringResource(medication_quantity_label, dose.quantity.toDisplayString(),
                stringResource(if (dose.quantity.unit == DoseUnit.TABLET) unit_pills else unit_ml)), style = MaterialTheme.typography.headlineSmall)
            if (dose.pharmacologicalDose.isNotBlank()) {
                Text(stringResource(label_pharmacological_dose))
                Text(dose.pharmacologicalDose)
            }
            Text(stringResource(dose_scheduled_at, doseTimeLabel(dose.scheduledAt)))
            if (dose.notes.isNotBlank()) { Text(stringResource(label_treatment_notes)); Text(dose.notes) }
            Text(stringResource(when (dose.status) {
                DoseStatus.PENDING -> dose_pending
                DoseStatus.TAKEN -> dose_taken
                DoseStatus.SKIPPED -> dose_skipped
                DoseStatus.OBSOLETE -> dose_obsolete
            }), style = MaterialTheme.typography.titleMedium)
            dose.resolvedAt?.let { Text(stringResource(dose_resolved_at, doseTimeLabel(it))) }
            if (dose.status == DoseStatus.PENDING) {
                Text(if (!dose.stockTracked) stringResource(stock_dose_untracked)
                    else stringResource(stock_current, com.connectapp.domain.model.StockAmounts.format(dose.stockAvailableMinor!!)) + " " + com.connectapp.presentation.stock.stockUnitLabel(dose.quantity.unit))
                TextButton(onClick = { onStock(dose.medicationId) }, enabled = !state.loading) { Text(stringResource(stock_title)) }
                com.connectapp.presentation.component.PrimaryActionButton(
                    stringResource(dose_confirm), { viewModel.resolve(DoseStatus.TAKEN) }, busy = state.loading
                )
                OutlinedButton(onClick = { viewModel.resolve(DoseStatus.SKIPPED) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(dose_skip))
                }
                OutlinedButton(onClick = { viewModel.snooze() }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.dose_snooze))
                }
            }
        }
        TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text(stringResource(dose_back_home)) }
    }
}
