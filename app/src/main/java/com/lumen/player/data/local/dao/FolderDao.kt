package com.lumen.player.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lumen.player.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folder_settings")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT path FROM folder_settings WHERE isExcluded = 1")
    suspend fun excludedPaths(): List<String>

    @Upsert
    suspend fun upsert(folder: FolderEntity)

    @Query("DELETE FROM folder_settings WHERE path = :path")
    suspend fun delete(path: String)
}
