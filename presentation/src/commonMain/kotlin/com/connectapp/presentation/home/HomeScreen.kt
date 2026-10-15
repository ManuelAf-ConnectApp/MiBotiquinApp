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
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        HomeScreenContent(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = Dimen16),
            state = state,
            onIntent = onIntent,
            onDeleteClick = { medicationToDelete = it }
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

            Button(
                onClick = { onIntent(HomeIntent.AddMedicationClicked) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(Dimen20)
                    )
                    Spacer(modifier = Modifier.width(Dimen8))
                    Text(text = stringResource(button_add_medication))
                }
            }
            Spacer(modifier = Modifier.height(Dimen16))

            state.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Dimen8))
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
                    Text(
                        text = stringResource(home_empty_medications),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
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
                    onClick = { onIntent(HomeIntent.MedicationClicked(medication)) },
                    onDeleteClick = { onDeleteClick(medication) }
                )
            }
        }
    }
}
