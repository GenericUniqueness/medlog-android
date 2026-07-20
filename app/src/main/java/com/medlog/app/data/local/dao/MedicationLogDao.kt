package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {
    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY takenAt DESC LIMIT 100")
    fun getByMedication(medicationId: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE profileId = :profileId ORDER BY takenAt DESC")
    fun getByProfile(profileId: Long): Flow<List<MedicationLogEntity>>

    @Query("SELECT COUNT(*) FROM medication_logs WHERE profileId = :profileId AND date(takenAt) = date('now')")
    fun getTodayCount(profileId: Long): Flow<Int>

    @Insert
    suspend fun insert(log: MedicationLogEntity): Long

    @Delete
    suspend fun delete(log: MedicationLogEntity)
}
