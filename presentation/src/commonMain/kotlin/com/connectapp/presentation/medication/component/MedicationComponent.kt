package com.connectapp.presentation.medication.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import com.connectapp.domain.model.DoseQuantity
import com.connectapp.domain.model.DoseUnit
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.number
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.connectapp.commonresources.*
import kotlin.time.Instant
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosesPerDayDropdown(
    selectedDoses: Int,
    onDosesChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val dosesOptions = (1..10).toList()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (selectedDoses in 1..10) selectedDoses.toString() else stringResource(scan_choose_frequency),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(label_doses_per_day)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dosesOptions.forEach { dose ->
                DropdownMenuItem(
                    text = { Text(dose.toString()) },
                    onClick = {
                        onDosesChanged(dose)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityInput(
    quantity: String,
    unit: String,
    onQuantityChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val units = DoseUnit.entries
    val selectedUnit = DoseUnit.fromStored(unit)
    val invalid = quantity.isNotBlank() && (selectedUnit == null || DoseQuantity.parse(quantity, selectedUnit) == null)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimen8)
    ) {
        OutlinedTextField(
            value = quantity,
            onValueChange = { onQuantityChanged(it) },
            label = { Text(stringResource(label_quantity_per_dose)) },
            isError = invalid,
            supportingText = { Text(stringResource(if (selectedUnit == DoseUnit.TABLET) quantity_tablet_help else quantity_ml_help)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = when (selectedUnit) {
                    DoseUnit.TABLET -> stringResource(unit_pills)
                    DoseUnit.MILLILITER -> stringResource(unit_ml)
                    null -> unit
                },
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(com.connectapp.commonresources.label_unit)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    true
                ).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                units.forEach { unitOption ->
                    DropdownMenuItem(
                        text = { Text(stringResource(if (unitOption == DoseUnit.TABLET) unit_pills else unit_ml)) },
                        onClick = {
                            onUnitChanged(unitOption.code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeDropdown(
    selectedMinutes: Int,
    onMinutesChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(0, 10, 20, 30, 40, 50)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedMinutes.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(label_reminder_time)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onMinutesChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTimeInput(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val formattedTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    OutlinedTextField(
        value = formattedTime,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(label_start_time)) },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = stringResource(label_start_time)
                )
            }
        },
        interactionSource = remember { MutableInteractionSource() }
    )

    if (showDialog) {
        val dialogState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        TimePickerDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                onTimeSelected(dialogState.hour, dialogState.minute)
                showDialog = false
            }
        ) {
            TimePicker(state = dialogState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInput(
    label: StringResource,
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    selectableDates: SelectableDates = object : SelectableDates {},
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    // Basic date formatting for display
    val formattedDate = selectedDate?.let {
        // Medianoche UTC del día elegido en el DatePicker: se formatea en UTC.
        val date = Instant.fromEpochMilliseconds(it)
            .toLocalDateTime(TimeZone.UTC)
        "${date.day.toString().padStart(2, '0')}/${date.month.number.toString().padStart(2, '0')}/${date.year}"
    } ?: ""

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(label)) },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(label)
                )
            }
        },
        interactionSource = remember { MutableInteractionSource() }
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate, selectableDates = selectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDialog = false
                }) {
                    Text(stringResource(button_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(button_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(button_accept))
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    )
}
