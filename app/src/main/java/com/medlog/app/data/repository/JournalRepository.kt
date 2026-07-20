package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.JournalEntryDao
import com.medlog.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface JournalRepository {
    fun getJournalByProfile(profileId: Long): Flow<List<JournalEntryEntity>>
    fun getJournalEntryById(id: Long): Flow<JournalEntryEntity?>
    suspend fun createEntry(profileId: Long, title: String?, content: String, mood: String?): Long
    suspend fun updateEntry(entry: JournalEntryEntity)
    suspend fun deleteEntry(entry: JournalEntryEntity)
}

class JournalRepositoryImpl(
    private val journalEntryDao: JournalEntryDao
) : JournalRepository {

    override fun getJournalByProfile(profileId: Long): Flow<List<JournalEntryEntity>> {
        return journalEntryDao.getByProfile(profileId)
    }

    override fun getJournalEntryById(id: Long): Flow<JournalEntryEntity?> {
        return journalEntryDao.getById(id)
    }

    override suspend fun createEntry(profileId: Long, title: String?, content: String, mood: String?): Long {
        val entry = JournalEntryEntity(
            profileId = profileId,
            title = title,
            content = content,
            mood = mood,
            entryDate = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return journalEntryDao.insert(entry)
    }

    override suspend fun updateEntry(entry: JournalEntryEntity) {
        journalEntryDao.update(entry.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteEntry(entry: JournalEntryEntity) {
        journalEntryDao.delete(entry)
    }
}
