package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE profileId = :profileId ORDER BY sortOrder ASC")
    fun getByProfile(profileId: Long): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE id = :id")
    fun getById(id: Long): Flow<SectionEntity?>

    @Insert
    suspend fun insert(section: SectionEntity): Long

    @Update
    suspend fun update(section: SectionEntity)

    @Delete
    suspend fun delete(section: SectionEntity)
}
