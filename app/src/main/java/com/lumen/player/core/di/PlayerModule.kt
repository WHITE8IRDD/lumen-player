package com.lumen.player.core.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.lumen.player.player.PlayerController
import com.lumen.player.player.PlayerManager
import com.lumen.player.player.audio.VolumeBoostController
import com.lumen.player.player.equalizer.EqualizerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@UnstableApi
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides @Singleton
    fun providePlayerManager(
        @ApplicationContext ctx: Context,
        volumeBoost: VolumeBoostController
    ) = PlayerManager(ctx, volumeBoost)

    @Provides @Singleton
    fun provideEqualizerController() = EqualizerController()

    @Provides @Singleton
    fun providePlayerController(
        @ApplicationContext ctx: Context,
        volumeBoost: VolumeBoostController
    ) = PlayerController(ctx, volumeBoost)
}
