package com.lumen.player.core.di

import android.content.Context
import com.lumen.player.data.mediastore.MediaStoreScanner
import com.lumen.player.data.metadata.MetadataExtractor
import com.lumen.player.data.prefs.SearchDataStore
import com.lumen.player.data.prefs.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier annotation class IoDispatcher
@Qualifier annotation class DefaultDispatcher
@Qualifier annotation class MainDispatcher
@Qualifier annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides @Singleton @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides @Singleton @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

    @Provides @Singleton @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

    @Provides @Singleton
    fun provideMediaStoreScanner(@ApplicationContext ctx: Context) =
        MediaStoreScanner(ctx)

    @Provides @Singleton
    fun provideMetadataExtractor(@ApplicationContext ctx: Context) =
        MetadataExtractor(ctx)

    @Provides @Singleton
    fun provideSettingsDataStore(@ApplicationContext ctx: Context) =
        SettingsDataStore(ctx)

    @Provides @Singleton
    fun provideSearchDataStore(@ApplicationContext ctx: Context) =
        SearchDataStore(ctx)
}
