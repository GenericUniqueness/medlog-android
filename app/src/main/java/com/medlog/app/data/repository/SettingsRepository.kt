package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.AppSettingDao
import com.medlog.app.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

interface SettingsRepository {
    fun getAllSettings(): Flow<List<AppSettingEntity>>
    fun getSetting(key: String): Flow<AppSettingEntity?>
    suspend fun setSetting(key: String, value: String)
    suspend fun deleteSetting(key: String)
    suspend fun getActiveProfileId(): Long?
}

class SettingsRepositoryImpl(
    private val appSettingDao: AppSettingDao
) : SettingsRepository {

    override fun getAllSettings(): Flow<List<AppSettingEntity>> {
        return appSettingDao.getAll()
    }

    override fun getSetting(key: String): Flow<AppSettingEntity?> {
        return appSettingDao.getByKey(key)
    }

    override suspend fun setSetting(key: String, value: String) {
        val existing = appSettingDao.getByKey(key).first()
        if (existing != null) {
            appSettingDao.upsert(existing.copy(value = value, updatedAt = LocalDateTime.now()))
        } else {
            val setting = AppSettingEntity(
                key = key,
                value = value,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            appSettingDao.upsert(setting)
        }
    }

    override suspend fun deleteSetting(key: String) {
        val existing = appSettingDao.getByKey(key).first()
        if (existing != null) {
            appSettingDao.delete(existing)
        }
    }

    override suspend fun getActiveProfileId(): Long? {
        val setting = appSettingDao.getByKey("active_profile_id").first()
        return setting?.value?.toLongOrNull()
    }
}
