package com.connectapp.domain.usecase

import com.connectapp.domain.repository.BackupRepository

class RestoreBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(envelope: String, passphrase: CharArray) = repository.restore(envelope, passphrase)
}
