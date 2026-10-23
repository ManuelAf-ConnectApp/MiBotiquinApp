package com.connectapp.presentation.home

import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User
import com.connectapp.presentation.util.UiText

data class HomeState(
    val medications: List<Medication>,
    val user: User?,
    val isLoading: Boolean,
    val error: UiText?
) {
    companion object {
        val EMPTY_DATA = HomeState(
            medications = emptyList(),
            user = null,
            isLoading = false,
            error = null
        )
    }
}