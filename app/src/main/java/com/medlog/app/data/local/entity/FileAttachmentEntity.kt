package com.medlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "file_attachments",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["entityType", "entityId"])
    ]
)
data class FileAttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileType: String? = null,
    val fileSize: Long? = null,
    val entityType: String,
    val entityId: Long,
    val profileId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
