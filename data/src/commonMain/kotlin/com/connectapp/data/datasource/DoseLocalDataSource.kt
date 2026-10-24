package com.connectapp.data.datasource

import com.connectapp.data.database.dao.StockDao
import com.connectapp.data.database.dao.DoseEventDao
import com.connectapp.data.database.dao.MedicationDao

interface DoseLocalDataSource {
    val stock: StockDao
    val events: DoseEventDao
    val medications: MedicationDao
    suspend fun <T> transaction(block: suspend () -> T): T
}
