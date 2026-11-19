package com.connectapp.presentation.dose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.resources.alarm_close
import com.connectapp.commonresources.resources.alarm_dose_unavailable
import com.connectapp.commonresources.resources.alarm_heading
import com.connectapp.commonresources.resources.dose_confirm
import com.connectapp.commonresources.resources.dose_skip
import com.connectapp.commonresources.resources.dose_snooze
import com.connectapp.domain.model.DoseStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private fun alarmTime(millis: Long): String {
    val local = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}

/**
 * Full-screen reminder shown when Android delivers an alarm.  Resolving or snoozing the dose
 * returns the person to the app and stops this interruption without assuming that it was taken.
 */
@Composable
fun AlarmDoseScreen(eventId: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: DoseViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) { viewModel.load(eventId) }
    LaunchedEffect(state.dose?.status, state.snoozed) {
        val status = state.dose?.status
        if (state.snoozed || (status != null && status != DoseStatus.PENDING)) onDismiss()
    }

    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxSize().background(colors.primary).padding(horizontal = 28.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.loading -> CircularProgressIndicator(color = colors.onPrimary)
            state.dose == null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.alarm_dose_unavailable), color = colors.onPrimary, textAlign = TextAlign.Center)
                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it.asString(), color = colors.error, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = onDismiss) { Text(stringResource(Res.string.alarm_close)) }
            }
            else -> {
                val dose = state.dose!!
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(48.dp)).background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Alarm, null, Modifier.size(48.dp), tint = colors.onPrimaryContainer)
                    }
                    Text(stringResource(Res.string.alarm_heading), color = colors.onPrimary, style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold)
                    Text(alarmTime(dose.scheduledAt), color = colors.onPrimary, style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Medication, null, Modifier.size(42.dp), tint = colors.primary)
                            Spacer(Modifier.height(12.dp))
                            Text(dose.medicationName, style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            if (dose.pharmacologicalDose.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(dose.pharmacologicalDose, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Button(
                        onClick = { viewModel.resolve(DoseStatus.TAKEN) },
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.primary)
                    ) { Text(stringResource(Res.string.dose_confirm), fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = { viewModel.snooze() }, enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onPrimary)
                    ) { Text(stringResource(Res.string.dose_snooze)) }
                    OutlinedButton(
                        onClick = { viewModel.resolve(DoseStatus.SKIPPED) }, enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onPrimary)
                    ) { Text(stringResource(Res.string.dose_skip)) }
                    state.error?.let { Text(it.asString(), color = colors.error, textAlign = TextAlign.Center) }
                }
            }
        }
    }
}
