package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE profileId = :profileId ORDER BY name ASC")
    fun getByProfile(profileId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE profileId = :profileId AND status = 'active' ORDER BY name ASC")
    fun getActiveByProfile(profileId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getById(id: Long): Flow<MedicationEntity?>

    @Query("SELECT * FROM medications WHERE profileId = :profileId AND name LIKE '%' || :query || '%'")
    fun search(profileId: Long, query: String): Flow<List<MedicationEntity>>

    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)
}
