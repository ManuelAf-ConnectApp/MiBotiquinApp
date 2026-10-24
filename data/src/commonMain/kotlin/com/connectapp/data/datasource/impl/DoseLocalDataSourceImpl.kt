package com.connectapp.data.datasource.impl

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.connectapp.data.database.AppDatabase
import com.connectapp.data.datasource.DoseLocalDataSource

class DoseLocalDataSourceImpl(private val database: AppDatabase) : DoseLocalDataSource {
    override val stock get() = database.stockDao()
    override val events get() = database.doseEventDao()
    override val medications get() = database.medicationDao()
    override suspend fun <T> transaction(block: suspend () -> T): T =
        database.useWriterConnection { it.immediateTransaction { block() } }
}
