package com.connectapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.DeleteMedicationUseCase
import com.connectapp.domain.usecase.GetUserMedicationByCodUserUseCase
import com.connectapp.presentation.home.HomeEffect.*
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserMedicationByCodUserUseCase: GetUserMedicationByCodUserUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.EMPTY_DATA)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<HomeIntent>()
    val intents: SharedFlow<HomeIntent> get() = _intents.asSharedFlow()

    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    private fun loadMedications() {
        val user = state.value.user ?: return
        val codUser = user.id

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                getUserMedicationByCodUserUseCase(codUser).onSuccess { medications ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            medications = medications
                        )
                    }
                }.onFailure { exception ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            error = exception.asUiText()
                        )
                    }
                }

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.asUiText()
                    )
                }
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        reduce(intent)
    }

    private fun reduce(intent: HomeIntent) {
        when (intent) {
            HomeIntent.AddMedicationClicked -> {
                viewModelScope.launch {
                    _effects.emit(NavigateToAddMedication)
                }
            }

            is HomeIntent.MedicationClicked -> {
                viewModelScope.launch {
                    _effects.emit(NavigateToEditMedication(intent.medication))
                }
            }

            is HomeIntent.DeleteMedicationConfirmed -> {
                viewModelScope.launch {
                    deleteMedicationUseCase(intent.medication.id).onSuccess {
                        loadMedications()
                    }.onFailure { exception ->
                        _state.update { it.copy(error = exception.asUiText()) }
                    }
                }
            }

            is HomeIntent.InitData -> {
                _state.update {
                    it.copy(
                        user = intent.user
                    )
                }
                loadMedications()
            }
        }
    }
}

