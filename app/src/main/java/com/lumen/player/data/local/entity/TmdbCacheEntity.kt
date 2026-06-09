package com.lumen.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tmdb_cache")
data class TmdbCacheEntity(
    @PrimaryKey val cacheKey: String,
    val posterPath: String?,
    val overview: String?,
    val backdropPath: String?,
    val cachedAt: Long
)
