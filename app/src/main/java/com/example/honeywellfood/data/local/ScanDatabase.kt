package com.example.honeywellfood.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.honeywellfood.domain.model.ScanItem

@Database(entities = [ScanItem::class], version = 2)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
}