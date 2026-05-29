package com.connectapp.cleanarch

import com.connectapp.data.datasource.*
import com.connectapp.data.interceptor.AuthInterceptor
import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.repository.AuthRepositoryImpl
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.usecase.ForgotPasswordUseCase
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.forgot_password.ForgotPasswordViewModel
import com.connectapp.presentation.home.HomeViewModel
import com.connectapp.presentation.login.LoginViewModel
import com.connectapp.presentation.profile.ProfileViewModel
import com.connectapp.presentation.register.RegisterViewModel
import com.connectapp.presentation.settings.SettingsViewModel
import com.connectapp.presentation.splash.SplashViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProfileViewModel)
}

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::ForgotPasswordUseCase)
}

val dataModule = module {
    single<NetworkInterceptor> { AuthInterceptor(get()) }
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get()) }
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl() }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
