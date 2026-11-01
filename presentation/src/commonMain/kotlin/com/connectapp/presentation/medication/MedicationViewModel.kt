package com.connectapp.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.DoseUnit
import com.connectapp.domain.validator.MedicationRules
import com.connectapp.commonresources.error_invalid_medication
import com.connectapp.presentation.util.UiText
import com.connectapp.domain.usecase.SaveMedicationUseCase
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicationViewModel(
    private val saveMedicationUseCase: SaveMedicationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MedicationState())
    val state: StateFlow<MedicationState> get() = _state.asStateFlow()

    private val _intents = MutableSharedFlow<MedicationIntent>()
    val intents: SharedFlow<MedicationIntent> get() = _intents.asSharedFlow()

    private val _effects = MutableSharedFlow<MedicationEffect>()
    val effects: SharedFlow<MedicationEffect> get() = _effects.asSharedFlow()

    fun onIntent(intent: MedicationIntent) {
        when (intent) {
            is MedicationIntent.Prefill -> _state.update {
                MedicationState(user = it.user, name = intent.product.name, catalogueCode = intent.product.nationalCode,
                    catalogueName = intent.product.name, catalogueRetrievedAt = intent.product.retrievedAt,
                    quantityUnit = "", dosesPerDay = 0)
            }
            is MedicationIntent.OnInitUserData -> {
                _state.update { it.copy(user = intent.user) }
            }

            is MedicationIntent.LoadMedication -> {
                val med = intent.medication
                _state.update {
                    it.copy(
                        id = med.id,
                        name = med.name,
                        dosesPerDay = med.dosesPerDay,
                        quantity = med.doseQuantity?.toDecimalString() ?: med.quantity.toString(),
                        quantityUnit = DoseUnit.fromStored(med.quantityUnit)?.code ?: med.quantityUnit,
                        isIndefinite = med.isIndefinite,
                        isPaused = med.isPaused,
                        notes = med.notes,
                        pharmacologicalDose = med.pharmacologicalDose,
                        catalogueCode = med.catalogueCode, catalogueName = med.catalogueName, catalogueRetrievedAt = med.catalogueRetrievedAt,
                        durationDays = med.durationDays.toString(),
                        startDate = med.startDate,
                        endDate = med.endDate,
                        reminderMinutes = med.reminderMinutes,
                        startTimeHour = med.startTimeHour,
                        startTimeMinute = med.startTimeMinute,
                        customSchedule = med.customSchedule,
                        weekdaySchedule = med.weekdaySchedule
                    )
                }
            }

            MedicationIntent.ClearMedication -> {
                _state.update {
                    MedicationState(user = it.user)
                }
            }

            is MedicationIntent.NameChanged -> {
                _state.update { it.copy(name = intent.name) }
            }

            is MedicationIntent.DosesPerDayChanged -> {
                _state.update { it.copy(dosesPerDay = intent.doses) }
            }

            is MedicationIntent.QuantityChanged -> {
                _state.update { it.copy(quantity = intent.quantity) }
            }

            is MedicationIntent.QuantityUnitChanged -> {
                _state.update {
                    if (it.quantityUnit == intent.unit) it else it.copy(quantityUnit = intent.unit, quantity = "")
                }
            }

            is MedicationIntent.IndefiniteChanged -> _state.update {
                it.copy(isIndefinite = intent.enabled, endDate = null, durationDays = "")
            }
            is MedicationIntent.PausedChanged -> _state.update { it.copy(isPaused = intent.paused) }
            is MedicationIntent.NotesChanged -> _state.update { it.copy(notes = intent.notes) }
            is MedicationIntent.PharmacologicalDoseChanged -> _state.update { it.copy(pharmacologicalDose = intent.dose) }
            is MedicationIntent.StartDateChanged -> _state.update {
                it.copy(startDate = intent.date, durationDays = calculateDuration(intent.date, it.endDate))
            }
            is MedicationIntent.EndDateChanged -> _state.update {
                it.copy(endDate = intent.date, durationDays = calculateDuration(it.startDate, intent.date))
            }

            is MedicationIntent.ReminderMinutesChanged -> {
                _state.update { it.copy(reminderMinutes = intent.minutes) }
            }

            is MedicationIntent.StartTimeChanged -> {
                _state.update {
                    it.copy(
                        startTimeHour = intent.hour,
                        startTimeMinute = intent.minute
                    )
                }
            }
            is MedicationIntent.CustomScheduleChanged -> _state.update { it.copy(customSchedule = intent.value) }
            is MedicationIntent.WeekdayScheduleChanged -> _state.update { it.copy(weekdaySchedule = intent.value) }

            MedicationIntent.SaveClicked -> {
                val stateValue = _state.value
                if (stateValue.isLoading) return
                val medication = stateValue.toMedication()
                if (medication == null || !MedicationRules.isValid(medication)) {
                    _state.update { it.copy(errorMessage = UiText.StringRes(error_invalid_medication)) }
                    return
                }
                _state.update { it.copy(isLoading = true, errorMessage = null) }
                viewModelScope.launch {
                    saveMedicationUseCase(medication)
                        .onSuccess {
                            // SaveMedicationUseCase ya re-sincroniza los recordatorios.
                            _state.update { it.copy(isLoading = false) }
                            emitEffect(MedicationEffect.NavigateBack)
                        }
                        .onFailure { exception ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = exception.asUiText()
                                )
                            }
                        }
                }
            }
        }
    }

    private fun calculateDuration(start: Long?, end: Long?): String =
        MedicationRules.inclusiveDuration(start, end)?.toString() ?: ""

    private fun emitEffect(effect: MedicationEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
