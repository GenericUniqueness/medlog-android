package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE profileId = :profileId ORDER BY appointmentDate ASC")
    fun getByProfile(profileId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE profileId = :profileId AND appointmentDate > datetime('now') AND status = 'scheduled' ORDER BY appointmentDate ASC LIMIT 5")
    fun getUpcomingByProfile(profileId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    fun getById(id: Long): Flow<AppointmentEntity?>

    @Query("SELECT * FROM appointments WHERE profileId = :profileId AND (title LIKE '%' || :query || '%' OR doctorName LIKE '%' || :query || '%')")
    fun search(profileId: Long, query: String): Flow<List<AppointmentEntity>>

    @Insert
    suspend fun insert(appointment: AppointmentEntity): Long

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Delete
    suspend fun delete(appointment: AppointmentEntity)
}
