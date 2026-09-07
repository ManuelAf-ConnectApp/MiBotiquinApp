package com.connectapp.presentation.home

import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User

data class HomeState(
    val medications: List<Medication>,
    val user: User?,
    val isLoading: Boolean,
    val error: String?
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