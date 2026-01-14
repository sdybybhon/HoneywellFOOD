package com.example.honeywellfood.data.local

import androidx.room.*
import com.example.honeywellfood.domain.model.ScanItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScanItem>>

    @Insert
    suspend fun insert(scan: ScanItem)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()

    @Delete
    suspend fun delete(scan: ScanItem)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: Int)
}