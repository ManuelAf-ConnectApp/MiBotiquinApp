package com.connectapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect.asSharedFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            delay(3000)
            _effect.emit(SplashEffect.NavigateToLogin)
        }
    }
}
