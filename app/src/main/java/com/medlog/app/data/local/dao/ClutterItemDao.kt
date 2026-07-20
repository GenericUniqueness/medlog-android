package com.medlog.app.data.local.dao

import androidx.room.*
import com.medlog.app.data.local.entity.ClutterItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClutterItemDao {
    @Query("SELECT * FROM clutter_items WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getByProfile(profileId: Long): Flow<List<ClutterItemEntity>>

    @Query("SELECT * FROM clutter_items WHERE profileId = :profileId AND content LIKE '%' || :query || '%'")
    fun search(profileId: Long, query: String): Flow<List<ClutterItemEntity>>

    @Insert
    suspend fun insert(item: ClutterItemEntity): Long

    @Update
    suspend fun update(item: ClutterItemEntity)

    @Delete
    suspend fun delete(item: ClutterItemEntity)
}
