package com.connectapp.domain.repository

/** Offline encrypted backup for the signed-in local account. */
interface BackupRepository {
    suspend fun export(passphrase: CharArray): Result<String>
    suspend fun restore(envelope: String, passphrase: CharArray): Result<Unit>
}
