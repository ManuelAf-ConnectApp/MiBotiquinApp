package com.connectapp.presentation.dose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.DoseDetail
import com.connectapp.domain.model.DoseStatus
import com.connectapp.domain.repository.DoseRepository
import com.connectapp.domain.usecase.SyncRemindersUseCase
import com.connectapp.presentation.util.UiText
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DoseState(val dose: DoseDetail? = null, val loading: Boolean = true, val error: UiText? = null)

class DoseViewModel(private val repository: DoseRepository, private val sync: SyncRemindersUseCase) : ViewModel() {
    private val mutableState = MutableStateFlow(DoseState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    fun load(eventId: String) {
        job?.cancel()
        mutableState.value = DoseState()
        job = viewModelScope.launch {
            repository.getDose(eventId)
                .onSuccess { mutableState.value = DoseState(it, false) }
                .onFailure { mutableState.value = DoseState(loading = false, error = it.asUiText()) }
        }
    }

    fun resolve(status: DoseStatus) {
        val current = state.value
        val dose = current.dose ?: return
        if (current.loading || dose.status != DoseStatus.PENDING) return
        mutableState.value = current.copy(loading = true, error = null)
        job = viewModelScope.launch {
            repository.resolveDose(dose.eventId, status)
                .onSuccess {
                    mutableState.value = DoseState(it, false)
                    sync()
                }
                .onFailure { error ->
                    val refreshed = repository.getDose(dose.eventId).getOrNull()
                    mutableState.value = DoseState(refreshed, false, error.asUiText())
                }
        }
    }

    fun snooze(minutes: Int = 10) {
        val current = state.value
        val dose = current.dose ?: return
        if (current.loading || dose.status != DoseStatus.PENDING) return
        mutableState.value = current.copy(loading = true, error = null)
        job = viewModelScope.launch {
            repository.snoozeDose(dose.eventId, minutes)
                .onSuccess {
                    mutableState.value = DoseState(it, false)
                    sync()
                }
                .onFailure { error ->
                    val refreshed = repository.getDose(dose.eventId).getOrNull()
                    mutableState.value = DoseState(refreshed, false, error.asUiText())
                }
        }
    }

    fun clear() { job?.cancel(); mutableState.value = DoseState() }
}
