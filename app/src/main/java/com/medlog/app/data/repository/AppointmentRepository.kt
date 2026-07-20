package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.AppointmentDao
import com.medlog.app.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface AppointmentRepository {
    fun getAppointmentsByProfile(profileId: Long): Flow<List<AppointmentEntity>>
    fun getUpcomingAppointments(profileId: Long): Flow<List<AppointmentEntity>>
    fun getAppointmentById(id: Long): Flow<AppointmentEntity?>
    suspend fun createAppointment(profileId: Long, title: String, doctorName: String?, location: String?, appointmentDate: LocalDateTime, duration: Int?, notes: String?, status: String, reminderEnabled: Boolean): Long
    suspend fun updateAppointment(appointment: AppointmentEntity)
    suspend fun deleteAppointment(appointment: AppointmentEntity)
}

class AppointmentRepositoryImpl(
    private val appointmentDao: AppointmentDao
) : AppointmentRepository {

    override fun getAppointmentsByProfile(profileId: Long): Flow<List<AppointmentEntity>> {
        return appointmentDao.getByProfile(profileId)
    }

    override fun getUpcomingAppointments(profileId: Long): Flow<List<AppointmentEntity>> {
        return appointmentDao.getUpcomingByProfile(profileId)
    }

    override fun getAppointmentById(id: Long): Flow<AppointmentEntity?> {
        return appointmentDao.getById(id)
    }

    override suspend fun createAppointment(
        profileId: Long,
        title: String,
        doctorName: String?,
        location: String?,
        appointmentDate: LocalDateTime,
        duration: Int?,
        notes: String?,
        status: String,
        reminderEnabled: Boolean
    ): Long {
        val appointment = AppointmentEntity(
            profileId = profileId,
            title = title,
            doctorName = doctorName,
            location = location,
            appointmentDate = appointmentDate,
            duration = duration,
            notes = notes,
            status = status,
            reminderEnabled = reminderEnabled,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return appointmentDao.insert(appointment)
    }

    override suspend fun updateAppointment(appointment: AppointmentEntity) {
        appointmentDao.update(appointment.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteAppointment(appointment: AppointmentEntity) {
        appointmentDao.delete(appointment)
    }
}
