package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.ConditionDao
import com.medlog.app.data.local.dao.ConditionNoteDao
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.data.local.entity.ConditionNoteEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface ConditionRepository {
    fun getConditionsByProfile(profileId: Long): Flow<List<ConditionEntity>>
    fun getActiveConditionsByProfile(profileId: Long): Flow<List<ConditionEntity>>
    fun getConditionById(id: Long): Flow<ConditionEntity?>
    suspend fun createCondition(profileId: Long, name: String, severity: String?, diagnosedDate: LocalDate?, status: String, notes: String?): Long
    suspend fun updateCondition(condition: ConditionEntity)
    suspend fun deleteCondition(condition: ConditionEntity)
    fun getNotesForCondition(conditionId: Long): Flow<List<ConditionNoteEntity>>
    suspend fun addNote(conditionId: Long, content: String): Long
    suspend fun updateNote(note: ConditionNoteEntity)
    suspend fun deleteNote(note: ConditionNoteEntity)
}

class ConditionRepositoryImpl(
    private val conditionDao: ConditionDao,
    private val conditionNoteDao: ConditionNoteDao
) : ConditionRepository {

    override fun getConditionsByProfile(profileId: Long): Flow<List<ConditionEntity>> {
        return conditionDao.getByProfile(profileId)
    }

    override fun getActiveConditionsByProfile(profileId: Long): Flow<List<ConditionEntity>> {
        return conditionDao.getActiveByProfile(profileId)
    }

    override fun getConditionById(id: Long): Flow<ConditionEntity?> {
        return conditionDao.getById(id)
    }

    override suspend fun createCondition(
        profileId: Long,
        name: String,
        severity: String?,
        diagnosedDate: LocalDate?,
        status: String,
        notes: String?
    ): Long {
        val condition = ConditionEntity(
            profileId = profileId,
            name = name,
            severity = severity,
            diagnosedDate = diagnosedDate,
            status = status,
            notes = notes,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return conditionDao.insert(condition)
    }

    override suspend fun updateCondition(condition: ConditionEntity) {
        conditionDao.update(condition.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteCondition(condition: ConditionEntity) {
        conditionDao.delete(condition)
    }

    override fun getNotesForCondition(conditionId: Long): Flow<List<ConditionNoteEntity>> {
        return conditionNoteDao.getByCondition(conditionId)
    }

    override suspend fun addNote(conditionId: Long, content: String): Long {
        val note = ConditionNoteEntity(
            conditionId = conditionId,
            content = content,
            noteDate = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return conditionNoteDao.insert(note)
    }

    override suspend fun updateNote(note: ConditionNoteEntity) {
        conditionNoteDao.update(note.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteNote(note: ConditionNoteEntity) {
        conditionNoteDao.delete(note)
    }
}
