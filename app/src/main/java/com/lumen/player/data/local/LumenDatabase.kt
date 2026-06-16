package com.lumen.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lumen.player.data.local.dao.FolderDao
import com.lumen.player.data.local.dao.MediaDao
import com.lumen.player.data.local.dao.PlaylistDao
import com.lumen.player.data.local.dao.StreamDao
import com.lumen.player.data.local.dao.TmdbCacheDao
import com.lumen.player.data.local.entity.FolderEntity
import com.lumen.player.data.local.entity.MediaEntity
import com.lumen.player.data.local.entity.PlaylistEntity
import com.lumen.player.data.local.entity.PlaylistItemEntity
import com.lumen.player.data.local.entity.StreamEntity
import com.lumen.player.data.local.entity.TmdbCacheEntity

@Database(
    entities = [
        MediaEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        FolderEntity::class,
        TmdbCacheEntity::class,
        StreamEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class LumenDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun folderDao(): FolderDao
    abstract fun tmdbCacheDao(): TmdbCacheDao
    abstract fun streamDao(): StreamDao

    companion object {
        val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("ALTER TABLE media ADD COLUMN isExcluded INTEGER NOT NULL DEFAULT 0")
        }

        val MIGRATION_2_3 = Migration(2, 3) { db ->
            db.execSQL("ALTER TABLE media ADD COLUMN isVideo INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE media SET isVideo = 1 WHERE type = 'VIDEO'")
        }

        val MIGRATION_3_4 = Migration(3, 4) { db ->
            db.execSQL("ALTER TABLE media ADD COLUMN lastPlayedAt INTEGER NOT NULL DEFAULT 0")
        }

        val MIGRATION_4_5 = Migration(4, 5) { db ->
            db.execSQL("ALTER TABLE media ADD COLUMN isHiddenFromHome INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE media ADD COLUMN isHiddenFromContinueWatching INTEGER NOT NULL DEFAULT 0")
        }

        val MIGRATION_5_6 = Migration(5, 6) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS tmdb_cache (cacheKey TEXT PRIMARY KEY NOT NULL, posterPath TEXT, overview TEXT, backdropPath TEXT, cachedAt INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("ALTER TABLE media ADD COLUMN posterUrl TEXT DEFAULT NULL")
        }

        val MIGRATION_6_7 = Migration(6, 7) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS streams (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url TEXT NOT NULL, title TEXT NOT NULL, dateAdded INTEGER NOT NULL DEFAULT 0, lastPositionMs INTEGER NOT NULL DEFAULT 0)")
        }

        val MIGRATION_7_8 = Migration(7, 8) { db ->
            db.execSQL("ALTER TABLE streams ADD COLUMN playlistName TEXT DEFAULT NULL")
        }

        val MIGRATION_8_9 = Migration(8, 9) { db ->
            db.execSQL("ALTER TABLE streams ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE streams ADD COLUMN category TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE streams ADD COLUMN logoUrl TEXT DEFAULT NULL")
        }

        val MIGRATION_9_10 = Migration(9, 10) { db ->
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_media_folderPath_title ON media(folderPath, title)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_media_type_folderPath_title ON media(type, folderPath, title)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_media_isVideo_dateAdded ON media(isVideo, dateAdded)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_media_isVideo_lastPlayedAt ON media(isVideo, lastPlayedAt)")
        }
    }
}
