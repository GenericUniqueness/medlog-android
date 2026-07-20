package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "condition_notes",
    foreignKeys = [
        ForeignKey(
            entity = ConditionEntity::class,
            parentColumns = ["id"],
            childColumns = ["conditionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conditionId"])]
)
data class ConditionNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conditionId: Long,
    val content: String,
    val noteDate: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
