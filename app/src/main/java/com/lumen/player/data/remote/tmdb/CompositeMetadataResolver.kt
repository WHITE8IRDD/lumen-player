package com.lumen.player.data.remote.tmdb

import com.lumen.player.data.local.dao.TmdbCacheDao
import com.lumen.player.data.local.entity.TmdbCacheEntity
import com.lumen.player.data.metadata.MediaTitleParser
import com.lumen.player.domain.model.MediaMetadata
import com.lumen.player.domain.repository.MetadataResolver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeMetadataResolver @Inject constructor(
    private val tmdbApi: TmdbApi?,
    private val parser: MediaTitleParser,
    private val tmdbCacheDao: TmdbCacheDao
) : MetadataResolver {

    override suspend fun resolve(rawFilename: String): MediaMetadata {
        val parsed = parser.resolve(rawFilename)
        if (tmdbApi == null || parsed.cleanTitle.isEmpty()) return parsed

        val cacheKey = "${parsed.cleanTitle.lowercase()}|${parsed.year ?: ""}"
        val threshold = System.currentTimeMillis() - CACHE_DURATION_MS

        val cached = tmdbCacheDao.get(cacheKey)
        if (cached != null && cached.cachedAt > threshold) {
            return parsed.copy(
                posterUrl = cached.posterPath,
                overview = cached.overview,
                backdropUrl = cached.backdropPath
            )
        }

        val result = tmdbApi.searchMovie(parsed.cleanTitle, parsed.year)
        if (result != null) {
            tmdbCacheDao.upsert(
                TmdbCacheEntity(
                    cacheKey = cacheKey,
                    posterPath = result.posterPath,
                    overview = result.overview,
                    backdropPath = result.backdropPath,
                    cachedAt = System.currentTimeMillis()
                )
            )
            return parsed.copy(
                posterUrl = result.posterPath,
                overview = result.overview,
                backdropUrl = result.backdropPath
            )
        }

        return parsed
    }

    companion object {
        private const val CACHE_DURATION_MS = 30L * 24 * 60 * 60 * 1000
    }
}
