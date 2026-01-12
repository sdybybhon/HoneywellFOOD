package com.example.honeywellfood.di

import android.content.Context
import androidx.room.Room
import com.example.honeywellfood.data.local.ScanDatabase
import com.example.honeywellfood.data.local.ScanDao
import com.example.honeywellfood.data.repository.ScanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideScanDatabase(@ApplicationContext context: Context): ScanDatabase {
        return Room.databaseBuilder(
            context,
            ScanDatabase::class.java,
            "scan_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScanDao(database: ScanDatabase) = database.scanDao()

    @Provides
    @Singleton
    fun provideScanRepository(scanDao: ScanDao) = ScanRepository(scanDao)
}