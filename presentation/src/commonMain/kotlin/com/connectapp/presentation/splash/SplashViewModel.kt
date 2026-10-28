package com.connectapp.presentation.splash

import com.connectapp.domain.usecase.RestoreSessionUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getCurrentUserUseCase: RestoreSessionUseCase
) : ViewModel() {

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect.asSharedFlow()

    init {
        checkSessionAndStartTimer()
    }

    private fun checkSessionAndStartTimer() {
        viewModelScope.launch {
            delay(2000)
            getCurrentUserUseCase()
                .onSuccess { user ->
                    if (user != null) {
                        _effect.emit(SplashEffect.NavigateToHome(user))
                    } else {
                        _effect.emit(SplashEffect.NavigateToLogin)
                    }
                }
                .onFailure {
                    _effect.emit(SplashEffect.NavigateToLogin)
                }
        }
    }
}
