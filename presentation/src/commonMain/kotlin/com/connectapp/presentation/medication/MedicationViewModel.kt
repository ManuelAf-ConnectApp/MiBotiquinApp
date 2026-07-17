package com.connectapp.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Medication
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

    // Wait, the original had:
    // private val _effects = MutableSharedFlow<MedicationEffect>()
    // val effects: SharedFlow<MedicationEffect> get() = _effects.asSharedFlow()

    // I will just use the correct one.

    fun onIntent(intent: MedicationIntent) {
        when (intent) {
            is MedicationIntent.OnInitUserData -> {
                _state.update { it.copy(user = intent.user) }
            }

            is MedicationIntent.DosesPerDayChanged -> {
                _state.update { it.copy(dosesPerDay = intent.doses) }
            }

            is MedicationIntent.QuantityChanged -> {
                _state.update { it.copy(quantity = intent.quantity) }
            }

            is MedicationIntent.QuantityUnitChanged -> {
                _state.update { it.copy(quantityUnit = intent.unit) }
            }

            is MedicationIntent.StartDateChanged -> {
                _state.update {
                    val newStartDate = intent.date
                    val currentEndDate = it.endDate
                    val correctedEndDate =
                        if (newStartDate != null && currentEndDate != null && currentEndDate < newStartDate) {
                            null
                        } else {
                            currentEndDate
                        }
                    it.copy(
                        startDate = newStartDate,
                        endDate = correctedEndDate,
                        durationDays = calculateDuration(newStartDate, correctedEndDate)
                    )
                }
            }

            is MedicationIntent.EndDateChanged -> {
                _state.update {
                    val currentStartDate = it.startDate
                    val newEndDate = intent.date
                    val correctedEndDate =
                        if (currentStartDate != null && newEndDate != null && newEndDate < currentStartDate) {
                            currentStartDate // Or null, but the picker should prevent this.
                        } else {
                            newEndDate
                        }
                    it.copy(
                        endDate = correctedEndDate,
                        durationDays = calculateDuration(it.startDate, correctedEndDate)
                    )
                }
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

            MedicationIntent.SaveClicked -> {
                val stateValue = _state.value
                val medication = Medication(
                    id = stateValue.id,
                    name = stateValue.name,
                    codUser = stateValue.user?.id.toString(), // Mock user id
                    dosesPerDay = stateValue.dosesPerDay,
                    quantity = stateValue.quantity.toDoubleOrNull() ?: 0.0,
                    quantityUnit = stateValue.quantityUnit,
                    durationDays = stateValue.durationDays.toIntOrNull() ?: 0,
                    startDate = stateValue.startDate,
                    endDate = stateValue.endDate,
                    reminderMinutes = stateValue.reminderMinutes,
                    startTimeHour = stateValue.startTimeHour,
                    startTimeMinute = stateValue.startTimeMinute
                )
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, errorMessage = null) }
                    saveMedicationUseCase(medication)
                        .onSuccess {
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

            else -> {}
        }
    }

    private fun calculateDuration(start: Long?, end: Long?): String {
        if (start == null || end == null) return ""
        val diff = end - start
        if (diff < 0) return "0"
        val days = (diff / (1000 * 60 * 60 * 24)) + 1
        return days.toString()
    }

    private fun emitEffect(effect: MedicationEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
