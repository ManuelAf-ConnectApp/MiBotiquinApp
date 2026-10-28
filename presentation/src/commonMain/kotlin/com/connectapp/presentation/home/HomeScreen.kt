package com.connectapp.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Schedule
import com.connectapp.presentation.component.BrandIcon
import com.connectapp.presentation.component.PrimaryActionButton
import androidx.compose.material3.OutlinedButton
import com.connectapp.presentation.dose.doseTimeLabel
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.resources.dose_history_title
import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User
import com.connectapp.presentation.home.component.MedicationCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    user: User,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToEditMedication: (Medication) -> Unit,
    onOpenDose: (String) -> Unit,
    onStock: () -> Unit,
    onHistory: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    var medicationToDelete by remember { mutableStateOf<Medication?>(null) }

    LaunchedEffect(user) {
        viewModel.onIntent(HomeIntent.InitData(user))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToAddMedication -> {
                    onNavigateToAddMedication()
                }
                is HomeEffect.NavigateToEditMedication -> {
                    onNavigateToEditMedication(effect.medication)
                }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        HomeScreenContent(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = Dimen16),
            state = state,
            onIntent = onIntent,
            onDeleteClick = { medicationToDelete = it },
            onOpenDose = onOpenDose,
            onStock = onStock,
            onHistory = onHistory
        )
    }

    medicationToDelete?.let { medication ->
        AlertDialog(
            onDismissRequest = { medicationToDelete = null },
            title = { Text(stringResource(confirm_delete_title)) },
            text = { Text(stringResource(confirm_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onIntent(HomeIntent.DeleteMedicationConfirmed(medication))
                        medicationToDelete = null
                    }
                ) {
                    Text(stringResource(go))
                }
            },
            dismissButton = {
                TextButton(onClick = { medicationToDelete = null }) {
                    Text(stringResource(close))
                }
            }
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onDeleteClick: (Medication) -> Unit,
    onOpenDose: (String) -> Unit,
    onStock: () -> Unit,
    onHistory: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimen12),
    ) {
        item {
            val userName = state.user?.firstName?.takeIf { it.isNotBlank() }
            Text(
                text = if (userName != null) {
                    stringResource(welcome_back, userName)
                } else {
                    stringResource(welcome_back_generic)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimen20))

            PrimaryActionButton(stringResource(button_add_medication), { onIntent(HomeIntent.AddMedicationClicked) })
            Spacer(modifier = Modifier.height(Dimen8))
            OutlinedButton(onClick = onStock, modifier = Modifier.fillMaxWidth()) { Text(stringResource(stock_title)) }
            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.dose_history_title)) }
            Spacer(modifier = Modifier.height(Dimen16))

            state.error?.let { error ->
                Text(
                    text = error.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Dimen8))
            }
        }

        if (state.pendingDoses.isNotEmpty()) {
            item { Text(stringResource(dose_upcoming), style = MaterialTheme.typography.titleMedium) }
            items(state.pendingDoses, key = { it.eventId }) { dose ->
                Card(onClick = { onOpenDose(dose.eventId) }, modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = MaterialTheme.shapes.large) {
                    Row(Modifier.padding(Dimen16), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimen16)) {
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimen4)) {
                            Text(dose.medicationName, style = MaterialTheme.typography.titleMedium)
                            Text(doseTimeLabel(dose.scheduledAt), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (state.medications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimen32),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimen16)) {
                        BrandIcon(Icons.Default.Add, large = true)
                        Text(stringResource(home_empty_medications), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(home_empty_description), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            item {
                Text(
                    text = stringResource(home_medications_count, state.medications.size.toString()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(state.medications) { medication ->
                MedicationCard(
                    medication = medication,
                    stock = state.stockSummaries.firstOrNull { it.medicationId == medication.id },
                    onClick = { onIntent(HomeIntent.MedicationClicked(medication)) },
                    onDeleteClick = { onDeleteClick(medication) }
                )
            }
        }
    }
}
