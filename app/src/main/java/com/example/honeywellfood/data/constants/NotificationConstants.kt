package com.example.honeywellfood.data.constants

object NotificationConstants {
    const val CHANNEL_ID = "expiry_reminder_channel"
    const val CHANNEL_NAME = "Напоминания о сроке годности"
    const val NOTIFICATION_ID = 1001
    const val DESCRIPTION = "Напоминания о проверке сроков годности продуктов"
    const val CONTENT_TITLE = "Проверьте сроки годности"
    const val CONTENT_TEXT = "Возможно что-то просрочилось!"
}

object ReminderConstants {
    const val ALARM_REQUEST_CODE = 2001
    const val REMINDER_HOUR = 12
    const val REMINDER_MINUTE = 0
    val VIBRATION_PATTERN = longArrayOf(0, 500, 200, 500)
}