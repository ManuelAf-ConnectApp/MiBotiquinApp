package com.connectapp.presentation.medication

import androidx.compose.foundation.layout.*
import com.connectapp.presentation.component.PrimaryActionButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.resources.custom_schedule_hint
import com.connectapp.commonresources.resources.custom_schedule_label
import com.connectapp.domain.model.CatalogPresentation
import com.connectapp.domain.model.DoseUnit
import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User
import com.connectapp.presentation.medication.component.DateInput
import com.connectapp.presentation.medication.component.DosesPerDayDropdown
import com.connectapp.presentation.medication.component.QuantityInput
import com.connectapp.presentation.medication.component.ReminderTimeDropdown
import com.connectapp.presentation.medication.component.StartTimeInput
import com.connectapp.presentation.util.UiText
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    modifier: Modifier,
    user: User,
    medication: Medication? = null,
    prefill: CatalogPresentation? = null,
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val viewModel: MedicationViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(user) {
        onIntent(MedicationIntent.OnInitUserData(user))
    }

    LaunchedEffect(medication, prefill) {
        if (medication != null) {
            onIntent(MedicationIntent.LoadMedication(medication))
        } else {
            onIntent(MedicationIntent.ClearMedication)
            if (prefill != null) onIntent(MedicationIntent.Prefill(prefill))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { event ->
            when (event) {
                is MedicationEffect.NavigateBack -> onNavigateBack()
                is MedicationEffect.ShowToast -> onShowMessage(event.message)
            }
        }
    }

    MedicationScreenContent(
        modifier = modifier,
        state = state,
        onIntent = onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationScreenContent(
    modifier: Modifier,
    state: MedicationState,
    onIntent: (MedicationIntent) -> Unit,
) {
    val todayStart = remember {
        // El DatePicker trabaja con la medianoche UTC de cada día: se compara "hoy" (local)
        // en esa misma escala, o al oeste de UTC el día de hoy no sería seleccionable.
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    }

    val startDateSelectable = remember { object : SelectableDates {} }

    val endDateSelectable = remember(state.startDate, todayStart) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val minDate = state.startDate ?: todayStart
                return utcTimeMillis >= minDate
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimen16, vertical = Dimen20),
            verticalArrangement = Arrangement.spacedBy(Dimen20)
        ) {
            FormSection(title = stringResource(section_medication)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onIntent(MedicationIntent.NameChanged(it)) },
                    label = { Text(stringResource(label_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                state.catalogueCode?.takeIf { state.name == state.catalogueName }?.let { code ->
                    Text(stringResource(scan_cn_value, code), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(scan_source_attribution), style = MaterialTheme.typography.bodySmall)
                    state.catalogueRetrievedAt?.let { timestamp ->
                        Text(stringResource(scan_retrieved_at, com.connectapp.presentation.dose.doseTimeLabel(timestamp)), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(stringResource(scan_prefill_limits), style = MaterialTheme.typography.bodySmall)
                }
                DosesPerDayDropdown(
                    selectedDoses = state.dosesPerDay,
                    onDosesChanged = { onIntent(MedicationIntent.DosesPerDayChanged(it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                QuantityInput(
                    quantity = state.quantity,
                    unit = state.quantityUnit,
                    onQuantityChanged = { onIntent(MedicationIntent.QuantityChanged(it)) },
                    onUnitChanged = { onIntent(MedicationIntent.QuantityUnitChanged(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (DoseUnit.fromStored(state.quantityUnit) == DoseUnit.TABLET) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimen8)) {
                        listOf("¼" to "0.25", "½" to "0.5", "¾" to "0.75", "1" to "1").forEach { (label, value) ->
                            FilterChip(
                                selected = state.parsedQuantity?.toDecimalString() == value,
                                onClick = { onIntent(MedicationIntent.QuantityChanged(value)) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Text(stringResource(tablet_split_warning), style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = state.pharmacologicalDose,
                    onValueChange = { onIntent(MedicationIntent.PharmacologicalDoseChanged(it)) },
                    label = { Text(stringResource(label_pharmacological_dose)) },
                    supportingText = { Text(stringResource(pharmacological_dose_help)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onIntent(MedicationIntent.NotesChanged(it)) },
                    label = { Text(stringResource(label_treatment_notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FormSection(title = stringResource(section_schedule)) {
                Text(stringResource(schedule_evenly_spaced_help), style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(treatment_indefinite), modifier = Modifier.weight(1f))
                    Switch(checked = state.isIndefinite, onCheckedChange = { onIntent(MedicationIntent.IndefiniteChanged(it)) })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimen8)
                ) {
                    DateInput(
                        label = label_start_date,
                        selectedDate = state.startDate,
                        onDateSelected = { onIntent(MedicationIntent.StartDateChanged(it)) },
                        selectableDates = startDateSelectable,
                        modifier = Modifier.weight(1f)
                    )

                    if (!state.isIndefinite) DateInput(
                        label = label_end_date,
                        selectedDate = state.endDate,
                        onDateSelected = { onIntent(MedicationIntent.EndDateChanged(it)) },
                        selectableDates = endDateSelectable,
                        modifier = Modifier.weight(1f)
                    )
                }

                StartTimeInput(
                    hour = state.startTimeHour,
                    minute = state.startTimeMinute,
                    onTimeSelected = { h, m -> onIntent(MedicationIntent.StartTimeChanged(h, m)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.customSchedule,
                    onValueChange = { onIntent(MedicationIntent.CustomScheduleChanged(it)) },
                    label = { Text(stringResource(Res.string.custom_schedule_label)) },
                    supportingText = { Text(stringResource(Res.string.custom_schedule_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.weekdaySchedule,
                    onValueChange = { onIntent(MedicationIntent.WeekdayScheduleChanged(it)) },
                    label = { Text(stringResource(weekday_schedule_label)) },
                    supportingText = { Text(stringResource(weekday_schedule_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ReminderTimeDropdown(
                    selectedMinutes = state.reminderMinutes,
                    onMinutesChanged = { onIntent(MedicationIntent.ReminderMinutesChanged(it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.isIndefinite) Text(stringResource(treatment_indefinite))
                else DurationSummary(days = state.durationDays)
                Text(stringResource(treatment_dates_help), style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(treatment_paused), modifier = Modifier.weight(1f))
                    Switch(checked = state.isPaused, onCheckedChange = { onIntent(MedicationIntent.PausedChanged(it)) })
                }
                Text(stringResource(treatment_pause_help), style = MaterialTheme.typography.bodySmall)
            }
        }

        MedicationFooter(
            errorMessage = state.errorMessage,
            isSaveEnabled = state.isSaveButtonEnabled,
            isSaving = state.isLoading,
            onSaveClick = { onIntent(MedicationIntent.SaveClicked) }
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(Dimen16),
            verticalArrangement = Arrangement.spacedBy(Dimen12)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun DurationSummary(days: String) {
    if (days.isBlank()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimen12, vertical = Dimen10),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen8)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(medication_duration_summary, (days.toIntOrNull() ?: 0).toString()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MedicationFooter(
    errorMessage: UiText?,
    isSaveEnabled: Boolean,
    isSaving: Boolean,
    onSaveClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = Dimen8,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(Dimen16),
            verticalArrangement = Arrangement.spacedBy(Dimen8)
        ) {
            errorMessage?.let {
                Text(
                    text = it.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PrimaryActionButton(stringResource(button_save), onSaveClick, enabled = isSaveEnabled, busy = isSaving)
        }
    }
}
