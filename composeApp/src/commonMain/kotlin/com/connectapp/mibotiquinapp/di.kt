package com.connectapp.mibotiquinapp

import com.connectapp.data.datasource.impl.DoseLocalDataSourceImpl
import com.connectapp.data.datasource.DoseLocalDataSource
import com.connectapp.data.repository.CatalogueRepositoryImpl
import com.connectapp.domain.repository.CatalogueRepository
import com.connectapp.data.repository.StockRepositoryImpl
import com.connectapp.domain.repository.StockRepository
import com.connectapp.data.repository.DoseRepositoryImpl
import com.connectapp.domain.repository.DoseRepository
import com.connectapp.domain.notification.ReminderNavigation
import com.connectapp.presentation.scan.ScanViewModel
import com.connectapp.presentation.stock.StockViewModel
import com.connectapp.presentation.dose.DoseViewModel
import com.connectapp.presentation.history.HistoryViewModel
import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.createRoomDatabase
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.datasource.*
import com.connectapp.data.datasource.impl.MedicationDataSourceImpl
import com.connectapp.data.datasource.impl.UserLocalDataSourceImpl
import com.connectapp.data.interceptor.AuthInterceptor
import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.repository.RecoveryRepositoryImpl
import com.connectapp.data.repository.BackupRepositoryImpl
import com.connectapp.data.backup.RoomBackupStore
import com.connectapp.domain.repository.RecoveryRepository
import com.connectapp.domain.repository.BackupRepository
import com.connectapp.domain.usecase.IssueRecoveryCodeUseCase
import com.connectapp.domain.usecase.ResetPasswordUseCase
import com.connectapp.domain.usecase.ExportBackupUseCase
import com.connectapp.domain.usecase.RestoreBackupUseCase
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
import com.connectapp.domain.usecase.RestoreSessionUseCase
import com.connectapp.domain.usecase.SaveMedicationUseCase
import com.connectapp.domain.usecase.SyncRemindersUseCase
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
    viewModelOf(::DoseViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::StockViewModel)
    viewModelOf(::ScanViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::MedicationViewModel)
}

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::IssueRecoveryCodeUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::ExportBackupUseCase)
    factoryOf(::RestoreBackupUseCase)
    factoryOf(::SaveMedicationUseCase)
    factoryOf(::GetUserMedicationByCodUserUseCase)
    factoryOf(::DeleteMedicationUseCase)
    single { SyncRemindersUseCase(get(), get(), get(), get()) }
    single { ReminderNavigation() }
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::RestoreSessionUseCase)
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
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get(), get()) }
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl(get(), get()) }
    single<RecoveryRepository> { RecoveryRepositoryImpl(get(), get()) }
    single { RoomBackupStore(get(), get()) }
    single<BackupRepository> { BackupRepositoryImpl(get()) }
    single<DoseLocalDataSource> { DoseLocalDataSourceImpl(get()) }
    single<CatalogueRepository> { CatalogueRepositoryImpl(get()) }
    single<StockRepository> { StockRepositoryImpl(get(), get()) }
    single<DoseRepository> { DoseRepositoryImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<MedicationRepository> { MedicationRepositoryImpl(get(), get()) }
    single<MedicationDataSource> { MedicationDataSourceImpl(get()) }
    single<UserLocalDataSource> { UserLocalDataSourceImpl(get()) }

    // Database and DAOs
    single<UserDao> { get<AppDatabase>().userDao() }
    single<MedicationDao> { get<AppDatabase>().medicationDao() }
}
