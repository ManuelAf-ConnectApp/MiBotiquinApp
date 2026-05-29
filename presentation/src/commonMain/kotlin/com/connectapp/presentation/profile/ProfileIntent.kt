package com.connectapp.presentation.profile

sealed interface ProfileIntent {
    data object LogoutClicked : ProfileIntent
    data object BackClicked : ProfileIntent
    data object LoadProfile : ProfileIntent
}
