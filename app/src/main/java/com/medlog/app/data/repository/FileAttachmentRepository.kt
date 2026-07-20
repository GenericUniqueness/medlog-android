package com.medlog.app.data.repository

import android.content.Context
import android.net.Uri
import com.medlog.app.data.local.dao.FileAttachmentDao
import com.medlog.app.data.local.entity.FileAttachmentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

interface FileAttachmentRepository {
    fun getAttachmentsForEntity(entityType: String, entityId: Long): Flow<List<FileAttachmentEntity>>
    fun getAttachmentsForProfile(profileId: Long): Flow<List<FileAttachmentEntity>>
    suspend fun addAttachment(fileName: String, filePath: String, fileType: String?, fileSize: Long?, entityType: String, entityId: Long, profileId: Long): Long
    suspend fun deleteAttachment(attachment: FileAttachmentEntity)
    fun getAttachmentFile(relativePath: String): File
    suspend fun copyFileToStorage(sourceUri: Uri, entityType: String, entityId: Long, profileId: Long): Long
}

class FileAttachmentRepositoryImpl(
    private val fileAttachmentDao: FileAttachmentDao,
    private val context: Context
) : FileAttachmentRepository {

    override fun getAttachmentsForEntity(entityType: String, entityId: Long): Flow<List<FileAttachmentEntity>> {
        return fileAttachmentDao.getByEntity(entityType, entityId)
    }

    override fun getAttachmentsForProfile(profileId: Long): Flow<List<FileAttachmentEntity>> {
        return fileAttachmentDao.getByProfile(profileId)
    }

    override suspend fun addAttachment(
        fileName: String,
        filePath: String,
        fileType: String?,
        fileSize: Long?,
        entityType: String,
        entityId: Long,
        profileId: Long
    ): Long {
        val attachment = FileAttachmentEntity(
            fileName = fileName,
            filePath = filePath,
            fileType = fileType,
            fileSize = fileSize,
            entityType = entityType,
            entityId = entityId,
            profileId = profileId,
            createdAt = LocalDateTime.now()
        )
        return fileAttachmentDao.insert(attachment)
    }

    override suspend fun deleteAttachment(attachment: FileAttachmentEntity) {
        // Delete the physical file from internal storage
        val file = getAttachmentFile(attachment.filePath)
        if (file.exists()) {
            file.delete()
        }
        fileAttachmentDao.delete(attachment)
    }

    override fun getAttachmentFile(relativePath: String): File {
        return File(context.filesDir, relativePath)
    }

    override suspend fun copyFileToStorage(
        sourceUri: Uri,
        entityType: String,
        entityId: Long,
        profileId: Long
    ): Long = withContext(Dispatchers.IO) {
        // Determine the target directory
        val targetDir = File(context.filesDir, "attachments/$profileId/$entityType/$entityId")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        // Extract file name from URI or generate one
        val fileName = queryFileName(sourceUri) ?: "file_${System.currentTimeMillis()}"

        // Copy file content to internal storage
        val targetFile = File(targetDir, fileName)
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("Cannot open input stream for URI: $sourceUri")

        // Determine file type from URI
        val fileType = context.contentResolver.getType(sourceUri)

        // Create the database record
        val relativePath = "attachments/$profileId/$entityType/$entityId/$fileName"
        addAttachment(
            fileName = fileName,
            filePath = relativePath,
            fileType = fileType,
            fileSize = targetFile.length(),
            entityType = entityType,
            entityId = entityId,
            profileId = profileId
        )
    }

    private fun queryFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex >= 0) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        // Fallback: extract from URI path
        return uri.lastPathSegment
    }
}
