package com.lumen.player.player.cast

import android.content.Context
import androidx.media3.cast.CastPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lazy helper that constructs a [CastPlayer] when Google Play Services
 * (and therefore Cast) is available. Returns null on devices without Cast.
 */
@UnstableApi
@Singleton
class CastManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val isCastSupported: Boolean by lazy {
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    private var cachedCastPlayer: CastPlayer? = null

    fun isAvailable(): Boolean = isCastSupported

    fun obtainCastPlayer(): Player? {
        if (!isCastSupported) return null
        cachedCastPlayer?.let { return it }
        return runCatching {
            val ctx = CastContext.getSharedInstance(context)
            CastPlayer(ctx).also { cachedCastPlayer = it }
        }.getOrNull()
    }

    fun release() {
        cachedCastPlayer?.release()
        cachedCastPlayer = null
    }
}
