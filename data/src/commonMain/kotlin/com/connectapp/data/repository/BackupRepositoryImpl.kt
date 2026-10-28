package com.connectapp.data.repository

import com.connectapp.data.backup.RoomBackupStore
import com.connectapp.domain.model.AppError
import com.connectapp.domain.repository.BackupRepository
import kotlinx.coroutines.CancellationException

class BackupRepositoryImpl(private val store: RoomBackupStore) : BackupRepository {
    override suspend fun export(passphrase: CharArray): Result<String> = safely { store.export(passphrase) }
    override suspend fun restore(envelope: String, passphrase: CharArray): Result<Unit> = safely { store.restore(envelope, passphrase) }

    private suspend fun <T> safely(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IllegalArgumentException) {
        Result.failure(AppError.CustomError(e.message ?: "Invalid backup."))
    } catch (e: IllegalStateException) {
        Result.failure(AppError.CustomError(e.message ?: "Backup cannot be restored."))
    } catch (e: Exception) {
        Result.failure(AppError.UnknownError())
    }
}
