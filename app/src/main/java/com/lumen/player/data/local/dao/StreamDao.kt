package com.lumen.player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumen.player.data.local.entity.StreamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {

    @Query("SELECT * FROM streams ORDER BY isFavorite DESC, category ASC, title ASC")
    fun observeAll(): Flow<List<StreamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stream: StreamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(streams: List<StreamEntity>)

    @Query("DELETE FROM streams WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE streams SET lastPositionMs = :pos WHERE id = :id")
    suspend fun updatePosition(id: Long, pos: Long)

    @Query("UPDATE streams SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("SELECT * FROM streams WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun observeFavorites(): Flow<List<StreamEntity>>
}
