package com.lumen.player.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.lumen.player.data.local.entity.MediaEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads audio + video from MediaStore and converts them into [MediaEntity] rows.
 * Filtering and exclusion is handled by the repository.
 */
@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val albumArtBase: Uri = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanAudio(): List<MediaEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<MediaEntity>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.IS_PODCAST,
            MediaStore.Audio.Media.IS_AUDIOBOOK
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}!=0 OR " +
            "${MediaStore.Audio.Media.IS_PODCAST}!=0 OR " +
            "${MediaStore.Audio.Media.IS_AUDIOBOOK}!=0"
        context.contentResolver.query(
            collection, projection, selection, null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val podcastCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)
            val audiobookCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_AUDIOBOOK)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val albumArt = ContentUris.withAppendedId(albumArtBase, c.getLong(albumIdCol))
                val path = c.getString(dataCol) ?: continue
                val type = when {
                    c.getInt(podcastCol) != 0 -> "PODCAST"
                    c.getInt(audiobookCol) != 0 -> "AUDIOBOOK"
                    else -> "AUDIO"
                }
                result += MediaEntity(
                    id = id,
                    uri = uri.toString(),
                    title = c.getString(titleCol) ?: "Unknown",
                    artist = c.getString(artistCol),
                    album = c.getString(albumCol),
                    albumArtUri = albumArt.toString(),
                    durationMs = c.getLong(durCol),
                    mimeType = c.getString(mimeCol) ?: "audio/*",
                    sizeBytes = c.getLong(sizeCol),
                    folderPath = File(path).parent ?: "",
                    type = type,
                    dateAdded = c.getLong(dateCol) * 1000L,
                    trackNumber = c.getInt(trackCol).takeIf { it > 0 },
                    isVideo = false
                )
            }
        }
        result
    }

    suspend fun scanVideo(): List<MediaEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<MediaEntity>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED
        )
        context.contentResolver.query(
            collection, projection, null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val mimeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val path = c.getString(dataCol) ?: continue
                result += MediaEntity(
                    id = id + VIDEO_ID_OFFSET,
                    uri = uri.toString(),
                    title = c.getString(titleCol).orEmpty().ifBlank { c.getString(nameCol) ?: "Unknown" },
                    artist = c.getString(artistCol),
                    album = c.getString(albumCol),
                    albumArtUri = uri.toString(),     // video frame thumbnail via ContentResolver
                    durationMs = c.getLong(durCol),
                    mimeType = c.getString(mimeCol) ?: "video/*",
                    sizeBytes = c.getLong(sizeCol),
                    folderPath = File(path).parent ?: "",
                    type = "VIDEO",
                    dateAdded = c.getLong(dateCol) * 1000L,
                    trackNumber = null,
                    isVideo = true
                )
            }
        }
        result
    }

    companion object {
        /** Keep video IDs disjoint from audio in our Room cache. */
        const val VIDEO_ID_OFFSET = 1_000_000_000_000L
    }
}
