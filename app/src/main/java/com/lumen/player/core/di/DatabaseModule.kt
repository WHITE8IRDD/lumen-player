package com.lumen.player.core.di

import android.content.Context
import androidx.room.Room
import com.lumen.player.core.util.Constants
import com.lumen.player.data.local.LumenDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): LumenDatabase =
        Room.databaseBuilder(ctx, LumenDatabase::class.java, Constants.DATABASE_NAME)
            .addMigrations(
                LumenDatabase.MIGRATION_1_2,
                LumenDatabase.MIGRATION_2_3,
                LumenDatabase.MIGRATION_3_4,
                LumenDatabase.MIGRATION_4_5,
                LumenDatabase.MIGRATION_5_6,
                LumenDatabase.MIGRATION_6_7,
                LumenDatabase.MIGRATION_7_8,
                LumenDatabase.MIGRATION_8_9,
                LumenDatabase.MIGRATION_9_10
            )
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMediaDao(db: LumenDatabase) = db.mediaDao()
    @Provides fun providePlaylistDao(db: LumenDatabase) = db.playlistDao()
    @Provides fun provideFolderDao(db: LumenDatabase) = db.folderDao()
    @Provides fun provideTmdbCacheDao(db: LumenDatabase) = db.tmdbCacheDao()
    @Provides fun provideStreamDao(db: LumenDatabase) = db.streamDao()
}
