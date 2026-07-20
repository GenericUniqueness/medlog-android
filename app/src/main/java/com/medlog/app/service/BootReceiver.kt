package com.medlog.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medlog.app.worker.RescheduleWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            RescheduleWorker.enqueue(context)
        }
    }
}
