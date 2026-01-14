package com.example.honeywellfood.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HoneywellFood:ReminderWakeLock"
        ).apply {
            acquire(10 * 1000L)
        }

        try {
            ReminderManager(context).showReminderNotification()
            ReminderManager(context).scheduleNextReminder()
        } finally {
            wakeLock.release()
        }
    }
}