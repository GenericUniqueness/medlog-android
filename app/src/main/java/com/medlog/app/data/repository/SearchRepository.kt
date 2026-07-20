package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.AppointmentDao
import com.medlog.app.data.local.dao.ClutterItemDao
import com.medlog.app.data.local.dao.ConditionDao
import com.medlog.app.data.local.dao.JournalEntryDao
import com.medlog.app.data.local.dao.MedicationDao
import com.medlog.app.data.local.dao.SearchResults
import kotlinx.coroutines.flow.first

interface SearchRepository {
    suspend fun searchAll(profileId: Long, query: String): SearchResults
}

class SearchRepositoryImpl(
    private val medicationDao: MedicationDao,
    private val conditionDao: ConditionDao,
    private val appointmentDao: AppointmentDao,
    private val journalEntryDao: JournalEntryDao,
    private val clutterItemDao: ClutterItemDao
) : SearchRepository {

    override suspend fun searchAll(profileId: Long, query: String): SearchResults {
        val searchQuery = "%$query%"

        val medications = medicationDao.search(profileId, searchQuery).first()
        val conditions = conditionDao.search(profileId, searchQuery).first()
        val appointments = appointmentDao.search(profileId, searchQuery).first()
        val journalEntries = journalEntryDao.search(profileId, searchQuery).first()
        val clutterItems = clutterItemDao.search(profileId, searchQuery).first()

        return SearchResults(
            medications = medications,
            conditions = conditions,
            appointments = appointments,
            journalEntries = journalEntries,
            clutterItems = clutterItems
        )
    }
}
