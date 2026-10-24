package com.connectapp.presentation.stock

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.domain.model.*
import com.connectapp.presentation.component.*
import com.connectapp.presentation.dose.doseTimeLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun stockUnitLabel(unit: DoseUnit?) = when (unit) {
    DoseUnit.TABLET -> stringResource(unit_pills)
    DoseUnit.MILLILITER -> stringResource(unit_ml)
    null -> stringResource(stock_unknown_unit)
}

@Composable
fun stockForecastLabel(forecast: StockForecast): String = when (forecast.kind) {
    StockForecastKind.ESTIMATED -> stringResource(stock_estimated, doseTimeLabel(forecast.firstUncoveredDoseAt!!))
    StockForecastKind.ENOUGH_UNTIL_END -> stringResource(stock_enough_until_end)
    StockForecastKind.NO_FUTURE_DOSES -> stringResource(stock_no_future_doses)
    StockForecastKind.PAUSED -> stringResource(stock_estimate_paused)
    StockForecastKind.UNKNOWN_SCHEDULE -> stringResource(stock_not_estimable)
    StockForecastKind.OUTSIDE_HORIZON -> stringResource(stock_outside_horizon)
    StockForecastKind.NOT_CONFIGURED -> stringResource(stock_not_configured)
}

@Composable
fun StockScreen(initialMedicationId: Long?, modifier: Modifier = Modifier, onOpenDose: (String) -> Unit) {
    val viewModel: StockViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(initialMedicationId) { viewModel.open(initialMedicationId) }
    DisposableEffect(viewModel) { onDispose { viewModel.clear() } }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = Dimen560).fillMaxWidth().imePadding(),
            contentPadding = PaddingValues(Dimen24), verticalArrangement = Arrangement.spacedBy(Dimen16)) {
            if (state.loading) item { CircularProgressIndicator() }
            state.error?.let { error -> item { Text(error.asString(), color = MaterialTheme.colorScheme.error) } }
            if (state.saved) item { Text(stringResource(stock_saved), color = MaterialTheme.colorScheme.primary) }
            if (!state.loading && state.summaries.isEmpty()) item { Text(stringResource(stock_empty)) }
            val selected = state.selected
            if (selected == null) {
                items(state.summaries, key = { it.medicationId }) { summary ->
                    Card(onClick = { viewModel.select(summary.medicationId) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimen20), verticalArrangement = Arrangement.spacedBy(Dimen8)) {
                            Text(summary.medicationName, style = MaterialTheme.typography.titleMedium)
                            Text(if (summary.balanceMinor == null) stringResource(stock_not_configured)
                                else "${StockAmounts.format(summary.balanceMinor)} ${stockUnitLabel(summary.unit)}")
                            if (summary.isLow) Text(stringResource(stock_low), color = MaterialTheme.colorScheme.error)
                            Text(stockForecastLabel(summary.forecast), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                item {
                    TextButton(onClick = { viewModel.select(null) }, enabled = !state.saving) { Text(stringResource(stock_all_medications)) }
                    Text(selected.medicationName, style = MaterialTheme.typography.headlineSmall)
                    Text(stockUnitLabel(selected.unit), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    if (selected.balanceMinor != null) {
                        Text(stringResource(stock_current, StockAmounts.format(selected.balanceMinor)), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(stock_initial, StockAmounts.format(selected.initialMinor!!)))
                        Text(stringResource(stock_threshold_value, StockAmounts.format(selected.lowThresholdMinor!!)))
                        selected.referenceAt?.let { Text(stringResource(stock_reference, doseTimeLabel(it))) }
                        if (selected.isLow) Text(stringResource(stock_low), color = MaterialTheme.colorScheme.error)
                    }
                    Text(stockForecastLabel(selected.forecast))
                    selected.plannedDailyMinor?.let { Text(stringResource(stock_daily_plan, StockAmounts.format(it), stockUnitLabel(selected.unit))) }
                    Text(stringResource(stock_forecast_assumptions), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (selected.configured) item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen8)) {
                        OutlinedButton(onClick = { viewModel.edit(StockAction.REPLENISH) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(stringResource(stock_replenish)) }
                        OutlinedButton(onClick = { viewModel.edit(StockAction.ADJUST) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(stringResource(stock_adjust)) }
                        TextButton(onClick = { viewModel.edit(StockAction.THRESHOLD) }, enabled = !state.saving) { Text(stringResource(stock_threshold)) }
                    }
                }
                if (state.action != null && selected.unit != null) item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen16)) {
                        if (state.action == StockAction.CONFIGURE) InformationPanel(stringResource(stock_initial_help), androidx.compose.material.icons.Icons.Default.Info)
                        AppTextField(state.amount, viewModel::amount, stringResource(when (state.action) {
                            StockAction.REPLENISH -> stock_replenish_quantity
                            StockAction.THRESHOLD -> stock_threshold
                            else -> stock_counted_quantity
                        }), keyboardType = KeyboardType.Decimal, enabled = !state.saving)
                        Text(stockUnitLabel(selected.unit), style = MaterialTheme.typography.bodySmall)
                        if (state.action == StockAction.CONFIGURE) AppTextField(state.threshold, viewModel::threshold,
                            stringResource(stock_threshold), keyboardType = KeyboardType.Decimal, enabled = !state.saving)
                        if (state.action == StockAction.ADJUST || state.action == StockAction.REPLENISH) AppTextField(state.reason, viewModel::reason,
                            stringResource(if (state.action == StockAction.ADJUST) stock_adjust_reason else stock_optional_reason), enabled = !state.saving)
                        PrimaryActionButton(stringResource(button_save), viewModel::submit, busy = state.saving)
                    }
                }
                if (selected.configured) {
                    item { Text(stringResource(stock_history), style = MaterialTheme.typography.titleMedium) }
                    items(state.movements, key = { it.id }) { movement ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(Dimen16), verticalArrangement = Arrangement.spacedBy(Dimen4)) {
                                Text(stringResource(when (movement.kind) {
                                    StockMovementKind.INITIAL -> stock_movement_initial
                                    StockMovementKind.REPLENISHMENT -> stock_replenish
                                    StockMovementKind.ADJUSTMENT -> stock_adjust
                                    StockMovementKind.DOSE -> dose_taken
                                }), style = MaterialTheme.typography.titleSmall)
                                Text("${if (movement.deltaMinor > 0) "+" else ""}${StockAmounts.format(movement.deltaMinor)} ${stockUnitLabel(movement.unit)}")
                                Text(doseTimeLabel(movement.createdAt), style = MaterialTheme.typography.bodySmall)
                                if (movement.reason.isNotBlank()) Text(movement.reason)
                                movement.eventId?.let { id -> TextButton(onClick = { onOpenDose(id) }) { Text(stringResource(stock_open_dose)) } }
                            }
                        }
                    }
                }
            }
        }
    }
}
