package com.connectapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GetUserMedicationByCodUserUseCase
import com.connectapp.presentation.home.HomeEffect.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserMedicationByCodUserUseCase: GetUserMedicationByCodUserUseCase
) : ViewModel() {

    private val _state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.EMPTY_DATA)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<HomeIntent>()
    val intents: SharedFlow<HomeIntent> get() = _intents.asSharedFlow()

    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    init {
        loadMedications()
    }

    private fun loadMedications() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }

            // Simulate getting the logged user ID. In a real app, this would come from a Session/Auth manager.
            val codUser = "1"

            try {
                getUserMedicationByCodUserUseCase(codUser).onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            medications = it.medications
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = it.error
                        )
                    }
                }

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
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
            is HomeIntent.ItemClicked -> {
                viewModelScope.launch {
                    _effects.emit(OnShowToast(intent.item))
                }
            }

            HomeIntent.AddMedicationClicked -> {
                viewModelScope.launch {
                    _effects.emit(NavigateToAddMedication)
                }
            }

            is HomeIntent.InitData -> {
                _state.update {
                    it.copy(
                        user = intent.user
                    )
                }
            }
        }
    }
}

