package com.connectapp.presentation.home.component

import androidx.compose.foundation.BorderStroke
import com.connectapp.presentation.component.BrandIcon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.connectapp.commonresources.*
import com.connectapp.domain.model.StockSummary
import com.connectapp.domain.model.StockAmounts
import androidx.compose.material3.Surface
import com.connectapp.domain.model.DoseUnit
import com.connectapp.domain.model.Medication
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    stock: StockSummary? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(Dimen1, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(Dimen16)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandIcon(Icons.Default.Medication)
            Spacer(modifier = Modifier.width(Dimen16))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        medication_quantity_label,
                        medication.doseQuantity?.toDisplayString() ?: medication.quantity.toString(),
                        when (DoseUnit.fromStored(medication.quantityUnit)) {
                            DoseUnit.TABLET -> stringResource(unit_pills)
                            DoseUnit.MILLILITER -> stringResource(unit_ml)
                            null -> medication.quantityUnit
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (medication.isIndefinite) stringResource(medication_indefinite_summary, medication.dosesPerDay.toString())
                    else stringResource(medication_doses_summary, medication.dosesPerDay.toString(), medication.durationDays.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (medication.isPaused) Text(stringResource(treatment_paused), color = MaterialTheme.colorScheme.primary)
                if (medication.doseQuantity == null) Text(stringResource(legacy_quantity_review), color = MaterialTheme.colorScheme.error)

                if (stock != null) {
                    val balanceMinor = stock.balanceMinor
                    Surface(shape = MaterialTheme.shapes.small,
                        color = if (stock.isLow) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            if (balanceMinor == null) stringResource(stock_not_configured)
                            else (if (stock.isLow) stringResource(stock_low) + " · " else "") +
                                StockAmounts.format(balanceMinor) + " " + com.connectapp.presentation.stock.stockUnitLabel(stock.unit),
                            modifier = Modifier.padding(horizontal = Dimen8, vertical = Dimen4),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                medication.startDate?.let { start ->
                    // Fechas del DatePicker (medianoche UTC): se muestran en UTC.
                    val startDate = Instant.fromEpochMilliseconds(start)
                        .toLocalDateTime(TimeZone.UTC).date
                    val endDateStr = medication.endDate?.let { end ->
                        val endDate = Instant.fromEpochMilliseconds(end)
                            .toLocalDateTime(TimeZone.UTC).date
                        stringResource(medication_date_until, endDate.toString())
                    } ?: ""

                    Text(
                        text = stringResource(medication_date_from, startDate.toString()) + endDateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

