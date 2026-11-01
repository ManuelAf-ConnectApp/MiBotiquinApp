package com.connectapp.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.Dimen12
import com.connectapp.commonresources.Dimen16
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.resources.dose_history_empty
import com.connectapp.commonresources.resources.dose_obsolete
import com.connectapp.commonresources.resources.dose_skipped
import com.connectapp.commonresources.resources.dose_taken
import com.connectapp.domain.model.DoseStatus
import com.connectapp.presentation.dose.doseTimeLabel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val viewModel: HistoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    when {
        state.loading -> CircularProgressIndicator(modifier.padding(Dimen16))
        state.error != null -> Text(state.error!!.asString(), modifier.padding(Dimen16), color = MaterialTheme.colorScheme.error)
        state.doses.isEmpty() -> Text(stringResource(Res.string.dose_history_empty), modifier.padding(Dimen16))
        else -> LazyColumn(modifier.fillMaxSize().padding(Dimen16), verticalArrangement = Arrangement.spacedBy(Dimen12)) {
            items(state.doses, key = { it.eventId }) { dose ->
                Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(Dimen16), verticalArrangement = Arrangement.spacedBy(Dimen12)) {
                        Text(dose.medicationName, style = MaterialTheme.typography.titleMedium)
                        Text(doseTimeLabel(dose.scheduledAt), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(when (dose.status) {
                            DoseStatus.TAKEN -> Res.string.dose_taken
                            DoseStatus.SKIPPED -> Res.string.dose_skipped
                            DoseStatus.OBSOLETE, DoseStatus.PENDING -> Res.string.dose_obsolete
                        }), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
