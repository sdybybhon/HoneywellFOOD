package com.example.honeywellfood.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scan_history")
data class ScanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val data: String,
    val symbology: String,
    val timestamp: Long = Date().time
)