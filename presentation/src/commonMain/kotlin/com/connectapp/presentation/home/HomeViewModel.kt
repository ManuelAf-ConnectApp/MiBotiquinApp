package com.connectapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<HomeIntent>()
    val intents: SharedFlow<HomeIntent> = _intents.asSharedFlow()

    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }

            // Simulate loading user settings
            delay(1_500)

            _state.update {
                it.copy(
                    isLoading = false,
                )
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
                    _effects.emit(HomeEffect.OnShowToast(intent.item))
                }
            }

            else -> {
                // Handle other intents if needed
            }
        }
    }
}

