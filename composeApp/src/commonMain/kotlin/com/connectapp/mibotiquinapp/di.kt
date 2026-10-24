package com.connectapp.mibotiquinapp

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.createRoomDatabase
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.datasource.*
import com.connectapp.data.datasource.impl.MedicationDataSourceImpl
import com.connectapp.data.datasource.impl.UserLocalDataSourceImpl
import com.connectapp.data.interceptor.AuthInterceptor
import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.repository.AuthRepositoryImpl
import com.connectapp.data.repository.MedicationRepositoryImpl
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.MedicationRepository
import com.connectapp.domain.usecase.DeleteAccountUseCase
import com.connectapp.domain.usecase.DeleteMedicationUseCase
import com.connectapp.domain.usecase.ForgotPasswordUseCase
import com.connectapp.domain.usecase.GetUserMedicationByCodUserUseCase
import com.connectapp.domain.usecase.GetCurrentUserUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.domain.usecase.SaveMedicationUseCase
import com.connectapp.domain.usecase.SaveNotificationUseCase
import com.connectapp.domain.validator.FormValidator
import com.connectapp.domain.validator.impl.FormValidatorImpl
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.forgot_password.ForgotPasswordViewModel
import com.connectapp.presentation.home.HomeViewModel
import com.connectapp.presentation.login.LoginViewModel
import com.connectapp.presentation.medication.MedicationViewModel
import com.connectapp.presentation.profile.ProfileViewModel
import com.connectapp.presentation.register.RegisterViewModel
import com.connectapp.presentation.settings.SettingsViewModel
import com.connectapp.presentation.splash.SplashViewModel
import com.connectapp.presentation.notification.NotificationsController
import com.connectapp.presentation.theme.ThemeController
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(platformModule, presentationModule, domainModule, dataModule)
    }

val presentationModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::MedicationViewModel)
}

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::SaveMedicationUseCase)
    factoryOf(::GetUserMedicationByCodUserUseCase)
    factoryOf(::DeleteMedicationUseCase)
    factoryOf(::SaveNotificationUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::DeleteAccountUseCase)
    single<FormValidator> { FormValidatorImpl() }
    single { ThemeController(get()) }
    single { NotificationsController(get()) }
}

val dataModule = module {
    single<AppDatabase> {
        createRoomDatabase(builder = get())
    }

    single<NetworkInterceptor> { AuthInterceptor(get()) }
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get()) }
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<MedicationRepository> { MedicationRepositoryImpl(get()) }
    single<MedicationDataSource> { MedicationDataSourceImpl(get()) }
    single<UserLocalDataSource> { UserLocalDataSourceImpl(get()) }

    // Database and DAOs
    single<UserDao> { get<AppDatabase>().userDao() }
    single<MedicationDao> { get<AppDatabase>().medicationDao() }
}
