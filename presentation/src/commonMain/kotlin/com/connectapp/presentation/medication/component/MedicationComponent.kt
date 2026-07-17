package com.connectapp.presentation.medication.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.close
import com.connectapp.commonresources.go
import com.connectapp.commonresources.label_doses_per_day
import com.connectapp.commonresources.label_end_date
import com.connectapp.commonresources.label_quantity_per_dose
import com.connectapp.commonresources.label_reminder_time
import com.connectapp.commonresources.label_start_date
import com.connectapp.commonresources.label_start_time
import com.connectapp.commonresources.unit_ml
import com.connectapp.commonresources.unit_pills
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosesPerDayDropdown(
    selectedDoses: Int,
    onDosesChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dosesOptions = (1..10).toList()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDoses.toString(),
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
    onUnitChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val units = listOf(stringResource(unit_pills), stringResource(unit_ml))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = quantity,
            onValueChange = { onQuantityChanged(it) },
            label = { Text(stringResource(label_quantity_per_dose)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.width(120.dp)
        ) {
            OutlinedTextField(
                value = unit,
                onValueChange = {},
                readOnly = true,
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
                        text = { Text(unitOption) },
                        onClick = {
                            onUnitChanged(unitOption)
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
    onMinutesChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(10, 20, 30, 40, 50)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
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
    onTimeSelected: (Int, Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    val formattedTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    OutlinedTextField(
        value = formattedTime,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(label_start_time)) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null
                )
            }
        },
        interactionSource = remember { MutableInteractionSource() }
    )

    if (showDialog) {
        TimePickerDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                onTimeSelected(timePickerState.hour, timePickerState.minute)
                showDialog = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInput(
    label: StringResource,
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    selectableDates: SelectableDates = object : SelectableDates {}
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        selectableDates = selectableDates
    )

    // Basic date formatting for display
    val formattedDate = selectedDate?.let {
        val date = Instant.fromEpochMilliseconds(it)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
    } ?: ""

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(label)) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null
                )
            }
        },
        interactionSource = remember { MutableInteractionSource() }
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDialog = false
                }) {
                    Text(stringResource(go))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(close))
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
                Text(stringResource(close))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(go)) // Using "Go" as "OK" or similar
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
