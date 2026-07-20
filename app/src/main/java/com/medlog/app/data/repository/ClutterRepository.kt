package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.ClutterItemDao
import com.medlog.app.data.local.entity.ClutterItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface ClutterRepository {
    fun getClutterByProfile(profileId: Long): Flow<List<ClutterItemEntity>>
    suspend fun addClutterItem(profileId: Long, content: String): Long
    suspend fun updateClutterItem(item: ClutterItemEntity)
    suspend fun deleteClutterItem(item: ClutterItemEntity)
}

class ClutterRepositoryImpl(
    private val clutterItemDao: ClutterItemDao
) : ClutterRepository {

    override fun getClutterByProfile(profileId: Long): Flow<List<ClutterItemEntity>> {
        return clutterItemDao.getByProfile(profileId)
    }

    override suspend fun addClutterItem(profileId: Long, content: String): Long {
        val item = ClutterItemEntity(
            profileId = profileId,
            content = content,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return clutterItemDao.insert(item)
    }

    override suspend fun updateClutterItem(item: ClutterItemEntity) {
        clutterItemDao.update(item.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteClutterItem(item: ClutterItemEntity) {
        clutterItemDao.delete(item)
    }
}
