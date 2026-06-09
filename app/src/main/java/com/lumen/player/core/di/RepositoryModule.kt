package com.lumen.player.core.di

import com.lumen.player.data.metadata.MediaTitleParser
import com.lumen.player.data.remote.tmdb.CompositeMetadataResolver
import com.lumen.player.data.repository.MediaRepositoryImpl
import com.lumen.player.data.repository.SettingsRepositoryImpl
import com.lumen.player.domain.repository.MediaRepository
import com.lumen.player.domain.repository.MetadataResolver
import com.lumen.player.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindMetadataResolver(impl: CompositeMetadataResolver): MetadataResolver
}
