package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.MedicationChangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationChangeDao {
    @Query("SELECT * FROM medication_changes WHERE medicationId = :medicationId ORDER BY changeDate DESC")
    fun getByMedication(medicationId: Long): Flow<List<MedicationChangeEntity>>

    @Insert
    suspend fun insert(change: MedicationChangeEntity): Long
}
