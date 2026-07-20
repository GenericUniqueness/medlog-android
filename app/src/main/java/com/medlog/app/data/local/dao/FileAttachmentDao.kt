package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.FileAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileAttachmentDao {
    @Query("SELECT * FROM file_attachments WHERE entityType = :entityType AND entityId = :entityId")
    fun getByEntity(entityType: String, entityId: Long): Flow<List<FileAttachmentEntity>>

    @Query("SELECT * FROM file_attachments WHERE profileId = :profileId")
    fun getByProfile(profileId: Long): Flow<List<FileAttachmentEntity>>

    @Insert
    suspend fun insert(attachment: FileAttachmentEntity): Long

    @Delete
    suspend fun delete(attachment: FileAttachmentEntity)
}
