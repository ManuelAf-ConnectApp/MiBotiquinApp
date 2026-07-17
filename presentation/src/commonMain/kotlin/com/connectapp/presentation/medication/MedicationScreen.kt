package com.connectapp.presentation.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.domain.model.User
import com.connectapp.presentation.medication.component.DateInput
import com.connectapp.presentation.medication.component.DosesPerDayDropdown
import com.connectapp.presentation.medication.component.QuantityInput
import com.connectapp.presentation.medication.component.ReminderTimeDropdown
import com.connectapp.presentation.medication.component.StartTimeInput
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
    onNavigateBack: () -> Unit,
) {
    val viewModel: MedicationViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(user) {
        onIntent(MedicationIntent.OnInitUserData(user))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { event ->
            when (event) {
                is MedicationEffect.NavigateBack -> onNavigateBack()
                is MedicationEffect.ShowToast -> TODO()
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            MedicationScreenContent(
                modifier = modifier,
                state = state,
                onIntent = onIntent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreenContent(
    modifier: Modifier,
    state: MedicationState,
    onIntent: (MedicationIntent) -> Unit,
) {
    val todayStart = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    val startDateSelectable = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayStart
            }
        }
    }

    val endDateSelectable = remember(state.startDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val minDate = state.startDate ?: todayStart
                return utcTimeMillis >= minDate
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(stringResource(title_new_medication), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.fillMaxWidth().height(8.dp))

        // Medication Name
        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(MedicationIntent.NameChanged(it)) },
            label = { Text(stringResource(label_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Doses per day (Dropdown)
        DosesPerDayDropdown(
            selectedDoses = state.dosesPerDay,
            onDosesChanged = { onIntent(MedicationIntent.DosesPerDayChanged(it)) }
        )

        // Quantity (Text field + unit)
        QuantityInput(
            quantity = state.quantity,
            unit = state.quantityUnit,
            onQuantityChanged = { onIntent(MedicationIntent.QuantityChanged(it)) },
            onUnitChanged = { onIntent(MedicationIntent.QuantityUnitChanged(it)) }
        )

        // Reminder time (Dropdown)
        ReminderTimeDropdown(
            selectedMinutes = state.reminderMinutes,
            onMinutesChanged = { onIntent(MedicationIntent.ReminderMinutesChanged(it)) }
        )


        // Start Date
        DateInput(
            label = label_start_date,
            selectedDate = state.startDate,
            onDateSelected = { onIntent(MedicationIntent.StartDateChanged(it)) },
            selectableDates = startDateSelectable
        )

        // End Date
        DateInput(
            label = label_end_date,
            selectedDate = state.endDate,
            onDateSelected = { onIntent(MedicationIntent.EndDateChanged(it)) },
            selectableDates = endDateSelectable
        )

        // Start Time (Clickable field)
        StartTimeInput(
            hour = state.startTimeHour,
            minute = state.startTimeMinute,
            onTimeSelected = { h, m -> onIntent(MedicationIntent.StartTimeChanged(h, m)) }
        )


        // Duration in days (Text field)
        OutlinedTextField(
            value = state.durationDays,
            onValueChange = {},
            label = { Text(stringResource(label_duration_days)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            singleLine = true
        )


        Spacer(modifier = Modifier.weight(1f))

        state.errorMessage?.let {
            Text(
                text = it.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Button(
            onClick = { onIntent(MedicationIntent.SaveClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isSaveButtonEnabled,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(button_save))
        }
    }
}

