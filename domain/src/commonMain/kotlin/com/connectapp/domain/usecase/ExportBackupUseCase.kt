package com.connectapp.domain.usecase

import com.connectapp.domain.repository.BackupRepository

class ExportBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(passphrase: CharArray) = repository.export(passphrase)
}
