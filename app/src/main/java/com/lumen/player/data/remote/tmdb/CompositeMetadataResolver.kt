package com.lumen.player.data.remote.tmdb

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.lumen.player.data.local.dao.TmdbCacheDao
import com.lumen.player.data.local.entity.TmdbCacheEntity
import com.lumen.player.data.metadata.MediaTitleParser
import com.lumen.player.domain.model.MediaMetadata
import com.lumen.player.domain.repository.MetadataResolver
import com.lumen.player.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeMetadataResolver @Inject constructor(
    private val tmdbApi: TmdbApi?,
    private val parser: MediaTitleParser,
    private val tmdbCacheDao: TmdbCacheDao,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : MetadataResolver {

    override suspend fun resolve(rawFilename: String): MediaMetadata {
        val parsed = parser.resolve(rawFilename)
        if (tmdbApi == null || parsed.cleanTitle.isEmpty()) {
            Log.d(TAG, "tmdbApi=null=${tmdbApi == null}, cleanTitle.isEmpty=${parsed.cleanTitle.isEmpty()}")
            return parsed
        }

        val settings = settingsRepository.settings.value
        if (!settings.fetchOnlineMetadata) { Log.d(TAG, "metadata disabled"); return parsed }
        if (settings.metadataWifiOnly && !isOnWifi()) { Log.d(TAG, "skipped — no Wi-Fi"); return parsed }

        val cacheKey = "${parsed.cleanTitle.lowercase()}|${parsed.year ?: ""}|${parsed.seasonEpisode ?: ""}"
        val threshold = System.currentTimeMillis() - CACHE_DURATION_MS

        val cached = tmdbCacheDao.get(cacheKey)
        if (cached != null && cached.cachedAt > threshold) {
            Log.d(TAG, "Cache HIT for '$rawFilename' -> '${cached.posterPath}'")
            return parsed.copy(
                posterUrl = cached.posterPath?.let { "$TMDB_IMAGE_BASE_URL$it" },
                overview = cached.overview,
                backdropUrl = cached.backdropPath?.let { "$TMDB_IMAGE_BASE_URL$it" }
            )
        }

        val isTv = parsed.seasonEpisode != null
        Log.d(TAG, "Calling TMDB API for '$rawFilename' (isTv=$isTv)")
        val result = if (isTv) tmdbApi.searchTv(parsed.cleanTitle, parsed.year)
            else tmdbApi.searchMovie(parsed.cleanTitle, parsed.year)

        if (result != null) {
            Log.d(TAG, "TMDB result for '$rawFilename': poster=${result.posterPath}")
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
                posterUrl = result.posterPath?.let { "$TMDB_IMAGE_BASE_URL$it" },
                overview = result.overview,
                backdropUrl = result.backdropPath?.let { "$TMDB_IMAGE_BASE_URL$it" }
            )
        }

        Log.d(TAG, "No TMDB result for '$rawFilename'")
        return parsed
    }

    private fun isOnWifi(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    companion object {
        private const val TAG = "CompositeMetadataResolver"
        private const val CACHE_DURATION_MS = 30L * 24 * 60 * 60 * 1000
        const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    }
}
