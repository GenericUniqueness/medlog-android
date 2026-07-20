package com.medlog.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.medlog.app.MedLogApp
import com.medlog.app.service.NotificationHelper

class RescheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)

        // Ensure notification channel exists after boot
        notificationHelper.createChannel()

        // In a production app, we would query all pending reminders from the database
        // and reschedule them via AlarmManager. For now, we ensure the channel is created
        // and the infrastructure is in place for future enhancement.
        // Example:
        // val app = applicationContext as MedLogApp
        // val medications = app.container.medicationRepository.getActiveMedications()
        // val appointments = app.container.appointmentRepository.getUpcomingAppointments()
        // ... reschedule each reminder

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
