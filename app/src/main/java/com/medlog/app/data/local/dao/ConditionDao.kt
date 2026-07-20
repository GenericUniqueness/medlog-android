package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.ConditionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionDao {
    @Query("SELECT * FROM conditions WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getByProfile(profileId: Long): Flow<List<ConditionEntity>>

    @Query("SELECT * FROM conditions WHERE profileId = :profileId AND status = 'active' ORDER BY createdAt DESC")
    fun getActiveByProfile(profileId: Long): Flow<List<ConditionEntity>>

    @Query("SELECT * FROM conditions WHERE id = :id")
    fun getById(id: Long): Flow<ConditionEntity?>

    @Query("SELECT * FROM conditions WHERE profileId = :profileId AND name LIKE '%' || :query || '%'")
    fun search(profileId: Long, query: String): Flow<List<ConditionEntity>>

    @Insert
    suspend fun insert(condition: ConditionEntity): Long

    @Update
    suspend fun update(condition: ConditionEntity)

    @Delete
    suspend fun delete(condition: ConditionEntity)
}
