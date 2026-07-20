package com.medlog.app.data.repository

import com.medlog.app.data.local.dao.ProfileDao
import com.medlog.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<ProfileEntity>>
    fun getActiveProfile(): Flow<ProfileEntity?>
    fun getProfileById(id: Long): Flow<ProfileEntity?>
    suspend fun createProfile(name: String, dateOfBirth: LocalDate?, bloodType: String?, allergies: String?, notes: String?): Long
    suspend fun updateProfile(profile: ProfileEntity)
    suspend fun deleteProfile(profile: ProfileEntity)
    suspend fun setActiveProfile(id: Long)
}

class ProfileRepositoryImpl(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<ProfileEntity>> {
        return profileDao.getAll()
    }

    override fun getActiveProfile(): Flow<ProfileEntity?> {
        return profileDao.getActiveProfile()
    }

    override fun getProfileById(id: Long): Flow<ProfileEntity?> {
        return profileDao.getById(id)
    }

    override suspend fun createProfile(
        name: String,
        dateOfBirth: LocalDate?,
        bloodType: String?,
        allergies: String?,
        notes: String?
    ): Long {
        val profile = ProfileEntity(
            name = name,
            dateOfBirth = dateOfBirth,
            bloodType = bloodType,
            allergies = allergies,
            notes = notes,
            isActive = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return profileDao.insert(profile)
    }

    override suspend fun updateProfile(profile: ProfileEntity) {
        profileDao.update(profile.copy(updatedAt = LocalDateTime.now()))
    }

    override suspend fun deleteProfile(profile: ProfileEntity) {
        profileDao.delete(profile)
    }

    override suspend fun setActiveProfile(id: Long) {
        profileDao.setActive(id)
    }
}
