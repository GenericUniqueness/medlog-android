package com.medlog.app.data.local.dao

import com.medlog.app.data.local.entity.*

data class SearchResults(
    val medications: List<MedicationEntity> = emptyList(),
    val conditions: List<ConditionEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList(),
    val journalEntries: List<JournalEntryEntity> = emptyList(),
    val clutterItems: List<ClutterItemEntity> = emptyList()
) {
    val isEmpty: Boolean get() = medications.isEmpty() && conditions.isEmpty() && appointments.isEmpty() && journalEntries.isEmpty() && clutterItems.isEmpty()
    val totalCount: Int get() = medications.size + conditions.size + appointments.size + journalEntries.size + clutterItems.size
}
