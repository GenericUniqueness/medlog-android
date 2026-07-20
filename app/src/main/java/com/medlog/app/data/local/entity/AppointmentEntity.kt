package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "appointments",
    indices = [Index(value = ["profileId"])]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String,
    val doctorName: String? = null,
    val location: String? = null,
    val appointmentDate: LocalDateTime,
    val duration: Int? = null,
    val notes: String? = null,
    val status: String = "scheduled",
    val reminderEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
