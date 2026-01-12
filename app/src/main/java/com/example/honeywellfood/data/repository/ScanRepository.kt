package com.example.honeywellfood.data.repository

import com.example.honeywellfood.data.local.ScanDao
import com.example.honeywellfood.domain.model.ScanItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanRepository @Inject constructor(
    private val scanDao: ScanDao
) {
    fun getAllScans(): Flow<List<ScanItem>> = scanDao.getAll()

    suspend fun addScan(scan: ScanItem) = scanDao.insert(scan)

    suspend fun clearHistory() = scanDao.clearAll()
}