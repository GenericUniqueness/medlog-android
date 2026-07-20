package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.MedicationChangeDao
import com.medlog.app.data.local.dao.MedicationDao
import com.medlog.app.data.local.dao.MedicationLogDao
import com.medlog.app.data.local.entity.MedicationChangeEntity
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface MedicationRepository {
    fun getMedicationsByProfile(profileId: Long): Flow<List<MedicationEntity>>
    fun getActiveMedicationsByProfile(profileId: Long): Flow<List<MedicationEntity>>
    fun getMedicationById(id: Long): Flow<MedicationEntity?>
    suspend fun createMedication(profileId: Long, name: String, dosage: String?, frequency: String?, startDate: LocalDate?, endDate: LocalDate?, status: String, notes: String?): Long
    suspend fun updateMedication(medication: MedicationEntity)
    suspend fun deleteMedication(medication: MedicationEntity)
    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLogEntity>>
    fun getLogsForProfile(profileId: Long): Flow<List<MedicationLogEntity>>
    fun getTodayLogCount(profileId: Long): Flow<Int>
    suspend fun logMedication(medicationId: Long, profileId: Long, dosageTaken: String?, notes: String?): Long
    fun getChangesForMedication(medicationId: Long): Flow<List<MedicationChangeEntity>>
    suspend fun recordChange(medicationId: Long, changeType: String, previousValue: String?, newValue: String?, reason: String?): Long
}

class MedicationRepositoryImpl(
    private val medicationDao: MedicationDao,
    private val medicationLogDao: MedicationLogDao,
    private val medicationChangeDao: MedicationChangeDao
) : MedicationRepository {

    override fun getMedicationsByProfile(profileId: Long): Flow<List<MedicationEntity>> {
        return medicationDao.getByProfile(profileId)
    }

    override fun getActiveMedicationsByProfile(profileId: Long): Flow<List<MedicationEntity>> {
        return medicationDao.getActiveByProfile(profileId)
    }

    override fun getMedicationById(id: Long): Flow<MedicationEntity?> {
        return medicationDao.getById(id)
    }

    override suspend fun createMedication(
        profileId: Long,
        name: String,
        dosage: String?,
        frequency: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        status: String,
        notes: String?
    ): Long {
        val medication = MedicationEntity(
            profileId = profileId,
            name = name,
            dosage = dosage,
            frequency = frequency,
            startDate = startDate,
            endDate = endDate,
            status = status,
            notes = notes,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return medicationDao.insert(medication)
    }

    override suspend fun updateMedication(medication: MedicationEntity) {
        medicationDao.update(medication.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.delete(medication)
    }

    override fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLogEntity>> {
        return medicationLogDao.getByMedication(medicationId)
    }

    override fun getLogsForProfile(profileId: Long): Flow<List<MedicationLogEntity>> {
        return medicationLogDao.getByProfile(profileId)
    }

    override fun getTodayLogCount(profileId: Long): Flow<Int> {
        return medicationLogDao.getTodayCount(profileId)
    }

    override suspend fun logMedication(medicationId: Long, profileId: Long, dosageTaken: String?, notes: String?): Long {
        val log = MedicationLogEntity(
            medicationId = medicationId,
            profileId = profileId,
            takenAt = LocalDateTime.now(),
            dosageTaken = dosageTaken,
            notes = notes,
            createdAt = LocalDateTime.now()
        )
        return medicationLogDao.insert(log)
    }

    override fun getChangesForMedication(medicationId: Long): Flow<List<MedicationChangeEntity>> {
        return medicationChangeDao.getByMedication(medicationId)
    }

    override suspend fun recordChange(medicationId: Long, changeType: String, previousValue: String?, newValue: String?, reason: String?): Long {
        val change = MedicationChangeEntity(
            medicationId = medicationId,
            changeType = changeType,
            previousValue = previousValue,
            newValue = newValue,
            reason = reason,
            changeDate = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )
        return medicationChangeDao.insert(change)
    }
}
