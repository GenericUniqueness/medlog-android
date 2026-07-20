package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.SectionEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionEntryDao {
    @Query("SELECT * FROM section_entries WHERE sectionId = :sectionId ORDER BY sortOrder ASC")
    fun getBySection(sectionId: Long): Flow<List<SectionEntryEntity>>

    @Insert
    suspend fun insert(entry: SectionEntryEntity): Long

    @Update
    suspend fun update(entry: SectionEntryEntity)

    @Delete
    suspend fun delete(entry: SectionEntryEntity)
}
