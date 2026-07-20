package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.SectionDao
import com.medlog.app.data.local.dao.SectionEntryDao
import com.medlog.app.data.local.entity.SectionEntity
import com.medlog.app.data.local.entity.SectionEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface SectionRepository {
    fun getSectionsByProfile(profileId: Long): Flow<List<SectionEntity>>
    fun getSectionById(id: Long): Flow<SectionEntity?>
    suspend fun createSection(profileId: Long, title: String, sortOrder: Int): Long
    suspend fun updateSection(section: SectionEntity)
    suspend fun deleteSection(section: SectionEntity)
    fun getEntriesForSection(sectionId: Long): Flow<List<SectionEntryEntity>>
    suspend fun addEntry(sectionId: Long, title: String, content: String?, sortOrder: Int): Long
    suspend fun updateEntry(entry: SectionEntryEntity)
    suspend fun deleteEntry(entry: SectionEntryEntity)
}

class SectionRepositoryImpl(
    private val sectionDao: SectionDao,
    private val sectionEntryDao: SectionEntryDao
) : SectionRepository {

    override fun getSectionsByProfile(profileId: Long): Flow<List<SectionEntity>> {
        return sectionDao.getByProfile(profileId)
    }

    override fun getSectionById(id: Long): Flow<SectionEntity?> {
        return sectionDao.getById(id)
    }

    override suspend fun createSection(profileId: Long, title: String, sortOrder: Int): Long {
        val section = SectionEntity(
            profileId = profileId,
            title = title,
            sortOrder = sortOrder,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return sectionDao.insert(section)
    }

    override suspend fun updateSection(section: SectionEntity) {
        sectionDao.update(section.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteSection(section: SectionEntity) {
        sectionDao.delete(section)
    }

    override fun getEntriesForSection(sectionId: Long): Flow<List<SectionEntryEntity>> {
        return sectionEntryDao.getBySection(sectionId)
    }

    override suspend fun addEntry(sectionId: Long, title: String, content: String?, sortOrder: Int): Long {
        val entry = SectionEntryEntity(
            sectionId = sectionId,
            title = title,
            content = content,
            sortOrder = sortOrder,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return sectionEntryDao.insert(entry)
    }

    override suspend fun updateEntry(entry: SectionEntryEntity) {
        sectionEntryDao.update(entry.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteEntry(entry: SectionEntryEntity) {
        sectionEntryDao.delete(entry)
    }
}
