package com.connectapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.DoseDetail
import com.connectapp.domain.repository.DoseRepository
import com.connectapp.presentation.util.UiText
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryState(val doses: List<DoseDetail> = emptyList(), val loading: Boolean = true, val error: UiText? = null)

class HistoryViewModel(private val doses: DoseRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(HistoryState())
    val state = mutableState.asStateFlow()
    fun load() = viewModelScope.launch {
        mutableState.value = HistoryState()
        doses.doseHistory().onSuccess { mutableState.value = HistoryState(it, false) }
            .onFailure { mutableState.value = HistoryState(loading = false, error = it.asUiText()) }
    }
}
