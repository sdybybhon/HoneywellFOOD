package com.example.honeywellfood.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore
import java.util.Date

@Entity(tableName = "scan_history")
data class ScanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val barcode: String,
    val productName: String? = null,
    val category: String? = null,
    val expiryDate: Long? = null,
    val symbology: String,
    val timestamp: Long = Date().time
) {
    @Ignore
    val remainingDays: Int? = expiryDate?.let {
        val diff = it - Date().time
        val days = diff / (1000 * 60 * 60 * 24)
        days.toInt()
    }
}