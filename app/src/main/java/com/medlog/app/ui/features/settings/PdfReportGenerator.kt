package com.medlog.app.ui.features.settings

import android.content.Context
import com.medlog.app.data.local.entity.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PdfReportGenerator(private val context: Context) {

    fun generateReport(
        profile: ProfileEntity?,
        medications: List<MedicationEntity>,
        medLogsMap: Map<Long, List<MedicationLogEntity>>,
        conditions: List<ConditionEntity>,
        appointments: List<AppointmentEntity>,
        journals: List<JournalEntryEntity>
    ): File {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

        val fileName = "medlog_report_${System.currentTimeMillis()}.txt"
        val exportsDir = File(context.cacheDir, "exports")
        exportsDir.mkdirs()
        val file = File(exportsDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("═".repeat(50))
        sb.appendLine("          MedLog Health Report")
        sb.appendLine("═".repeat(50))
        sb.appendLine()

        // Profile Info
        sb.appendLine("━━ PROFILE ━━")
        if (profile != null) {
            sb.appendLine("Name: ${profile.name}")
            profile.dateOfBirth?.let { sb.appendLine("Date of Birth: ${it.format(dateFormatter)}") }
            profile.bloodType?.let { sb.appendLine("Blood Type: $it") }
            profile.allergies?.let { sb.appendLine("Allergies: $it") }
        } else {
            sb.appendLine("No active profile")
        }
        sb.appendLine()

        // Active Medications
        sb.appendLine("━━ ACTIVE MEDICATIONS (${medications.size}) ━━")
        if (medications.isEmpty()) {
            sb.appendLine("No active medications")
        } else {
            medications.forEach { med ->
                sb.appendLine("• ${med.name}")
                med.dosage?.let { sb.appendLine("  Dosage: $it") }
                med.frequency?.let { sb.appendLine("  Frequency: $it") }
                med.startDate?.let { sb.appendLine("  Start Date: ${it.format(dateFormatter)}") }
                med.notes?.let { sb.appendLine("  Notes: $it") }

                val logs = medLogsMap[med.id].orEmpty()
                if (logs.isNotEmpty()) {
                    sb.appendLine("  Recent Logs:")
                    logs.take(3).forEach { log ->
                        sb.appendLine("    - ${log.takenAt.format(dateTimeFormatter)}${log.dosageTaken?.let { " ($it)" } ?: ""}")
                    }
                }
                sb.appendLine()
            }
        }

        // Active Conditions
        sb.appendLine("━━ ACTIVE CONDITIONS (${conditions.size}) ━━")
        if (conditions.isEmpty()) {
            sb.appendLine("No active conditions")
        } else {
            conditions.forEach { cond ->
                sb.appendLine("• ${cond.name}")
                cond.severity?.let { sb.appendLine("  Severity: $it") }
                cond.diagnosedDate?.let { sb.appendLine("  Diagnosed: ${it.format(dateFormatter)}") }
                cond.notes?.let { sb.appendLine("  Notes: $it") }
                sb.appendLine()
            }
        }

        // Upcoming Appointments
        sb.appendLine("━━ UPCOMING APPOINTMENTS (${appointments.size}) ━━")
        if (appointments.isEmpty()) {
            sb.appendLine("No upcoming appointments")
        } else {
            appointments.forEach { appt ->
                sb.appendLine("• ${appt.title}")
                sb.appendLine("  Date: ${appt.appointmentDate.format(dateTimeFormatter)}")
                appt.doctorName?.let { sb.appendLine("  Doctor: $it") }
                appt.location?.let { sb.appendLine("  Location: $it") }
                appt.notes?.let { sb.appendLine("  Notes: $it") }
                sb.appendLine()
            }
        }

        // Recent Journal Entries
        sb.appendLine("━━ RECENT JOURNAL ENTRIES (${journals.size}) ━━")
        if (journals.isEmpty()) {
            sb.appendLine("No journal entries")
        } else {
            journals.forEach { entry ->
                sb.appendLine("• ${entry.title ?: "Untitled"}")
                sb.appendLine("  Date: ${entry.entryDate.format(dateTimeFormatter)}")
                entry.mood?.let { sb.appendLine("  Mood: $it") }
                sb.appendLine("  ${entry.content.take(200)}${if (entry.content.length > 200) "…" else ""}")
                sb.appendLine()
            }
        }

        sb.appendLine("═".repeat(50))
        sb.appendLine("Generated: ${LocalDateTime.now().format(dateTimeFormatter)}")
        sb.appendLine("MedLog — Your Private Health Journal")

        file.writeText(sb.toString())
        return file
    }
}
