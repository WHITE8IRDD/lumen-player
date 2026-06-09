package com.lumen.player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lumen.player.data.local.entity.MediaEntity
import com.lumen.player.data.local.entity.PlaylistEntity
import com.lumen.player.data.local.entity.PlaylistItemEntity
import com.lumen.player.data.local.entity.PlaylistWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun observeById(id: Long): Flow<PlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
        SELECT m.* FROM media m
        INNER JOIN playlist_items p ON m.id = p.mediaId
        WHERE p.playlistId = :playlistId
        ORDER BY p.orderIndex
    """)
    fun observeItems(playlistId: Long): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun removeItem(playlistId: Long, mediaId: Long)

    @Transaction
    suspend fun replaceItems(playlistId: Long, mediaIds: List<Long>) {
        clearItems(playlistId)
        mediaIds.forEachIndexed { index, id ->
            addItem(PlaylistItemEntity(playlistId, id, index))
        }
    }

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearItems(playlistId: Long)

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :id")
    suspend fun countItems(id: Long): Int

    @Query("SELECT p.id, p.name, p.createdAt, COUNT(pi.mediaId) AS itemCount FROM playlists p LEFT JOIN playlist_items pi ON p.id = pi.playlistId GROUP BY p.id ORDER BY p.createdAt DESC")
    fun observeAllWithCount(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%' ORDER BY name COLLATE NOCASE LIMIT :limit")
    suspend fun search(query: String, limit: Int = 5): List<PlaylistEntity>
}
