package com.lumen.player.ui.subtitles

import android.net.Uri
import android.provider.OpenableColumns
import android.content.Context
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.LongBuffer

/**
 * OpenSubtitles hash algorithm.
 *
 * hash = file size (bytes) XOR
 *        sum of first 64KB as LE int64 array XOR
 *        sum of last 64KB as LE int64 array
 *
 * Result is zero-padded to 16 hex chars.
 */
object SubtitleHash {

    private const val CHUNK_SIZE = 65536 // 64 KB

    fun hashFile(filePath: String): String {
        val file = RandomAccessFile(filePath, "r")
        val fileSize = file.length()
        val chunkSize = CHUNK_SIZE.coerceAtMost(fileSize.toInt())

        var hash = fileSize

        // Read first chunk
        val firstChunk = ByteArray(chunkSize)
        file.readFully(firstChunk)
        hash = hash xor sumAsLEInt64(firstChunk)

        // Read last chunk (seek to fileSize - chunkSize)
        if (fileSize > CHUNK_SIZE) {
            file.seek(fileSize - CHUNK_SIZE)
            val lastChunk = ByteArray(CHUNK_SIZE)
            file.readFully(lastChunk)
            hash = hash xor sumAsLEInt64(lastChunk)
        }

        file.close()

        return hash.toULong().toString(16).padStart(16, '0')
    }

    /**
     * Compute hash from a content URI (e.g. from MediaStore).
     * Falls back to filename-only search if the file is not
     * accessible as a local path.
     */
    fun hashFromUri(context: Context, uri: Uri): String? {
        // Try to resolve to a local file path first
        val path = resolveLocalPath(context, uri)
        if (path != null) {
            return try {
                hashFile(path)
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
        return cursor.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) it.getString(nameIndex) else null
        }
    }

    private fun resolveLocalPath(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        // Try MediaStore DATA column first
        try {
            val proj = arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA)
            context.contentResolver.query(uri, proj, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val path = c.getString(0)
                    if (path != null) return path
                }
            }
        } catch (_: Exception) { }
        // Fallback: try DISPLAY_NAME and check cache
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0)
                if (name != null) {
                    val cached = java.io.File(context.cacheDir, "subtitles/$name")
                    if (cached.exists()) return cached.absolutePath
                }
            }
        }
        return null
    }

    private fun sumAsLEInt64(data: ByteArray): Long {
        val padded = if (data.size % 8 == 0) data else data.copyOf(data.size + (8 - data.size % 8))
        val longCount = padded.size / 8
        val lb = ByteBuffer.wrap(padded).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer()
        var sum = 0L
        for (i in 0 until longCount) {
            sum += lb.get(i)
        }
        return sum
    }
}
