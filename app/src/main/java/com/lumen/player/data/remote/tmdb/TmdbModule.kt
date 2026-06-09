package com.lumen.player.data.remote.tmdb

import com.lumen.player.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TmdbModule {

    @Provides @Singleton
    fun provideTmdbApi(): TmdbApi? {
        if (BuildConfig.TMDB_API_KEY.isEmpty()) return null
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        return TmdbApi(client)
    }
}
