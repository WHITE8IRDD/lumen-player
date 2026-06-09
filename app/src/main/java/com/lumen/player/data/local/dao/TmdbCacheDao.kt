package com.lumen.player.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Query
import com.lumen.player.data.local.entity.TmdbCacheEntity

@Dao
interface TmdbCacheDao {

    @Upsert
    suspend fun upsert(entity: TmdbCacheEntity)

    @Query("SELECT * FROM tmdb_cache WHERE cacheKey = :key")
    suspend fun get(key: String): TmdbCacheEntity?

    @Query("DELETE FROM tmdb_cache WHERE cachedAt < :threshold")
    suspend fun deleteExpired(threshold: Long)
}
