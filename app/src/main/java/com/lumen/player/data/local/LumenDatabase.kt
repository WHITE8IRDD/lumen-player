package com.lumen.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lumen.player.data.local.dao.FolderDao
import com.lumen.player.data.local.dao.MediaDao
import com.lumen.player.data.local.dao.PlaylistDao
import com.lumen.player.data.local.dao.TmdbCacheDao
import com.lumen.player.data.local.entity.FolderEntity
import com.lumen.player.data.local.entity.MediaEntity
import com.lumen.player.data.local.entity.PlaylistEntity
import com.lumen.player.data.local.entity.PlaylistItemEntity
import com.lumen.player.data.local.entity.TmdbCacheEntity

@Database(
    entities = [
        MediaEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        FolderEntity::class,
        TmdbCacheEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class LumenDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun folderDao(): FolderDao
    abstract fun tmdbCacheDao(): TmdbCacheDao

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
    }
}
