package com.example.honeywellfood

import android.app.Application
import com.example.honeywellfood.utils.ReminderManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HoneywellFoodApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ReminderManager(this).scheduleDailyReminder()
    }
}