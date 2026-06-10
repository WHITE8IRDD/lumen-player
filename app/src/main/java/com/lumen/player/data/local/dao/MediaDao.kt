package com.lumen.player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lumen.player.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Query("SELECT * FROM media WHERE type = :type ORDER BY title COLLATE NOCASE")
    fun observeByType(type: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE isVideo = 1 ORDER BY title COLLATE NOCASE")
    fun observeVideo(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE isVideo = 0 ORDER BY title COLLATE NOCASE")
    fun observeAudio(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE folderPath = :path ORDER BY title COLLATE NOCASE")
    fun observeByFolder(path: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE type = :type AND folderPath = :path ORDER BY title COLLATE NOCASE")
    fun observeByTypeAndFolder(type: String, path: String): Flow<List<MediaEntity>>

    @Query("SELECT DISTINCT folderPath FROM media ORDER BY folderPath COLLATE NOCASE")
    fun observeFolderPaths(): Flow<List<String>>

    @Query("""
        SELECT folderPath AS path, COUNT(*) AS mediaCount,
               (SELECT albumArtUri FROM media m2 WHERE m2.folderPath = m.folderPath
                AND m2.albumArtUri IS NOT NULL LIMIT 1) AS firstArt
        FROM media m
        GROUP BY folderPath
        ORDER BY folderPath COLLATE NOCASE
    """)
    fun observeFolderSummaries(): Flow<List<FolderSummary>>

    /** Richer folder summary used by the Videos tab — includes total size,
     *  duration, most-recent date and a preview of the first video so the
     *  card can show a real thumbnail and metadata. */
    @Query("""
        SELECT
            m.folderPath                                              AS path,
            COUNT(*)                                                  AS mediaCount,
            MAX(m.dateAdded)                                          AS dateAdded,
            SUM(m.durationMs)                                         AS totalDurationMs,
            SUM(m.sizeBytes)                                          AS totalSizeBytes,
            (SELECT id          FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.isVideo = 1 ORDER BY m2.dateAdded DESC, m2.title COLLATE NOCASE LIMIT 1) AS firstVideoId,
            (SELECT uri         FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.isVideo = 1 ORDER BY m2.dateAdded DESC, m2.title COLLATE NOCASE LIMIT 1) AS firstVideoUri,
            (SELECT title       FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.isVideo = 1 ORDER BY m2.dateAdded DESC, m2.title COLLATE NOCASE LIMIT 1) AS firstVideoTitle,
            (SELECT durationMs  FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.isVideo = 1 ORDER BY m2.dateAdded DESC, m2.title COLLATE NOCASE LIMIT 1) AS firstVideoDurationMs,
            (SELECT sizeBytes   FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.isVideo = 1 ORDER BY m2.dateAdded DESC, m2.title COLLATE NOCASE LIMIT 1) AS firstVideoSizeBytes,
            (SELECT lastPositionMs FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.isVideo = 1 ORDER BY m2.dateAdded DESC, m2.title COLLATE NOCASE LIMIT 1) AS firstVideoLastPositionMs,
            (SELECT posterUrl    FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.isVideo = 1 ORDER BY m2.dateAdded DESC, m2.title COLLATE NOCASE LIMIT 1) AS firstVideoPosterUrl
        FROM media m
        WHERE m.isVideo = 1
        GROUP BY m.folderPath
        ORDER BY MAX(m.dateAdded) DESC
    """)
    fun observeVideoFolderSummaries(): Flow<List<VideoFolderSummary>>

    @Query("SELECT * FROM media WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun observeFavorites(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun get(id: Long): MediaEntity?

    @Query("SELECT COUNT(*) FROM media")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertAll(items: List<MediaEntity>)

    @Query("UPDATE media SET lastPositionMs = :pos WHERE id = :id")
    suspend fun updatePosition(id: Long, pos: Long)

    @Query("UPDATE media SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE media SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: Long)

    @Query("DELETE FROM media WHERE id NOT IN (:existingIds)")
    suspend fun deleteRemoved(existingIds: List<Long>)

    @Query("""
        SELECT * FROM media
        WHERE isVideo = 1 AND lastPositionMs > 0 AND lastPositionMs < durationMs * :threshold
        AND isHiddenFromContinueWatching = 0
        ORDER BY lastPlayedAt DESC LIMIT :limit
    """)
    fun observeContinueWatching(threshold: Double = 0.95, limit: Int = 10): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE dateAdded > :thresholdDate AND isHiddenFromHome = 0 ORDER BY dateAdded DESC LIMIT :limit")
    fun observeRecentlyAdded(thresholdDate: Long, limit: Int = 15): Flow<List<MediaEntity>>

    @Query("""
        SELECT folderPath AS path, COUNT(*) AS mediaCount,
               (SELECT albumArtUri FROM media m2 WHERE m2.folderPath = m.folderPath AND m2.albumArtUri IS NOT NULL LIMIT 1) AS firstArt
        FROM media m
        GROUP BY folderPath
        ORDER BY mediaCount DESC LIMIT :limit
    """)
    fun observeTopFolders(limit: Int = 8): Flow<List<FolderSummary>>

    @Query("SELECT * FROM media WHERE isVideo = 0 AND lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun observeRecentAudio(limit: Int = 10): Flow<List<MediaEntity>>

    @Query("UPDATE media SET lastPlayedAt = :time WHERE id = :id")
    suspend fun updateLastPlayedAt(id: Long, time: Long)

    @Query("SELECT * FROM media WHERE isVideo = 1 ORDER BY title COLLATE NOCASE")
    suspend fun getAllVideos(): List<MediaEntity>

    @Query("SELECT * FROM media WHERE isVideo = 1 AND title LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE LIMIT :limit")
    suspend fun searchVideos(query: String, limit: Int = 5): List<MediaEntity>

    @Query("SELECT * FROM media WHERE isVideo = 0 AND title LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE LIMIT :limit")
    suspend fun searchAudio(query: String, limit: Int = 5): List<MediaEntity>

    @Query("SELECT DISTINCT folderPath FROM media WHERE folderPath LIKE '%' || :query || '%' ORDER BY folderPath COLLATE NOCASE LIMIT :limit")
    suspend fun searchFolderPaths(query: String, limit: Int = 5): List<String>

    @Query("UPDATE media SET isHiddenFromHome = :hidden WHERE id = :id")
    suspend fun setHiddenFromHome(id: Long, hidden: Boolean)

    @Query("UPDATE media SET isHiddenFromContinueWatching = :hidden WHERE id = :id")
    suspend fun setHiddenFromContinueWatching(id: Long, hidden: Boolean)

    @Query("SELECT * FROM media WHERE isHiddenFromHome = 1 OR isHiddenFromContinueWatching = 1 ORDER BY title COLLATE NOCASE")
    fun observeHiddenItems(): Flow<List<MediaEntity>>

    @Query("UPDATE media SET isHiddenFromHome = 0, isHiddenFromContinueWatching = 0")
    suspend fun clearAllHiddenFlags()

    @Query("SELECT * FROM media WHERE posterUrl IS NULL AND isVideo = 1")
    suspend fun getVideosWithNullPoster(): List<MediaEntity>

    @Query("UPDATE media SET posterUrl = :posterUrl WHERE id = :id")
    suspend fun updatePosterUrl(id: Long, posterUrl: String?)
}

data class FolderSummary(
    val path: String,
    val mediaCount: Int,
    val firstArt: String?
)

data class VideoFolderSummary(
    val path: String,
    val mediaCount: Int,
    val dateAdded: Long,
    val totalDurationMs: Long,
    val totalSizeBytes: Long,
    val firstVideoId: Long?,
    val firstVideoUri: String?,
    val firstVideoTitle: String?,
    val firstVideoDurationMs: Long,
    val firstVideoSizeBytes: Long,
    val firstVideoLastPositionMs: Long,
    val firstVideoPosterUrl: String?
)
