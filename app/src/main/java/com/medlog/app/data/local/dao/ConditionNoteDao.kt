package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.ConditionNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionNoteDao {
    @Query("SELECT * FROM condition_notes WHERE conditionId = :conditionId ORDER BY noteDate DESC")
    fun getByCondition(conditionId: Long): Flow<List<ConditionNoteEntity>>

    @Insert
    suspend fun insert(note: ConditionNoteEntity): Long

    @Update
    suspend fun update(note: ConditionNoteEntity)

    @Delete
    suspend fun delete(note: ConditionNoteEntity)
}
