package com.medlog.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("reminder_type") ?: return
        val id = intent.getLongExtra("reminder_id", -1)
        val title = intent.getStringExtra("reminder_title") ?: return
        val message = intent.getStringExtra("reminder_message") ?: ""

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminderNotification(id, type, title, message)
    }
}
