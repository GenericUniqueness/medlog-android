package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries WHERE profileId = :profileId ORDER BY entryDate DESC")
    fun getByProfile(profileId: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    fun getById(id: Long): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE profileId = :profileId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun search(profileId: Long, query: String): Flow<List<JournalEntryEntity>>

    @Insert
    suspend fun insert(entry: JournalEntryEntity): Long

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Delete
    suspend fun delete(entry: JournalEntryEntity)
}
