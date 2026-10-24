package com.connectapp.presentation.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.*
import com.connectapp.domain.repository.StockRepository
import com.connectapp.presentation.util.UiText
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class StockAction { CONFIGURE, REPLENISH, ADJUST, THRESHOLD }
data class StockState(
    val summaries: List<StockSummary> = emptyList(), val selectedId: Long? = null,
    val movements: List<StockMovement> = emptyList(), val action: StockAction? = null,
    val amount: String = "", val threshold: String = "0", val reason: String = "",
    val operationId: String = "", val loading: Boolean = true, val saving: Boolean = false,
    val saved: Boolean = false, val error: UiText? = null
) { val selected get() = summaries.firstOrNull { it.medicationId == selectedId } }

class StockViewModel(private val repository: StockRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(StockState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    fun open(id: Long?) { mutableState.value = StockState(selectedId = id); load(id) }
    fun select(id: Long?) { if (!state.value.saving) load(id) }

    private fun load(id: Long?) {
        job?.cancel()
        mutableState.value = state.value.copy(loading = true, error = null, selectedId = id, action = null, movements = emptyList())
        job = viewModelScope.launch {
            repository.summaries().onSuccess { summaries ->
                val selected = summaries.firstOrNull { it.medicationId == id }
                val movements = if (selected != null) repository.movements(selected.medicationId) else Result.success(emptyList())
                mutableState.value = state.value.copy(summaries = summaries, selectedId = selected?.medicationId,
                    movements = movements.getOrDefault(emptyList()), loading = false, saving = false,
                    error = movements.exceptionOrNull()?.asUiText())
                if (selected != null && !selected.configured) edit(StockAction.CONFIGURE)
            }.onFailure { mutableState.value = state.value.copy(loading = false, saving = false, error = it.asUiText()) }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun edit(action: StockAction) {
        if (state.value.saving) return
        mutableState.value = state.value.copy(action = action, amount = "", threshold = "0", reason = "",
            operationId = Uuid.random().toString(), error = null, saved = false)
    }
    fun amount(value: String) { mutableState.value = state.value.copy(amount = value) }
    fun threshold(value: String) { mutableState.value = state.value.copy(threshold = value) }
    fun reason(value: String) { mutableState.value = state.value.copy(reason = value) }

    fun submit() {
        val form = state.value
        val selected = form.selected ?: return
        val unit = selected.unit ?: return
        val action = form.action ?: return
        if (form.saving) return
        val amount = StockAmounts.parse(form.amount, unit)
        val threshold = StockAmounts.parse(form.threshold, unit)
        if (amount == null || (action == StockAction.CONFIGURE && threshold == null)) {
            mutableState.value = form.copy(error = AppError.InvalidStock().asUiText()); return
        }
        mutableState.value = form.copy(saving = true, error = null, saved = false)
        job = viewModelScope.launch {
            val result = when (action) {
                StockAction.CONFIGURE -> repository.configure(selected.medicationId, amount, threshold!!, form.operationId)
                StockAction.REPLENISH -> repository.replenish(selected.medicationId, amount, form.reason, form.operationId)
                StockAction.ADJUST -> repository.adjust(selected.medicationId, amount, form.reason, form.operationId)
                StockAction.THRESHOLD -> repository.setThreshold(selected.medicationId, amount)
            }
            result.onSuccess {
                mutableState.value = state.value.copy(saving = false, saved = true)
                // Launch the refresh in a separate job; do not cancel the committing operation.
                viewModelScope.launch { load(selected.medicationId) }
            }.onFailure { mutableState.value = state.value.copy(saving = false, error = it.asUiText()) }
        }
    }

    fun clear() { job?.cancel(); mutableState.value = StockState() }
}
