package com.lumen.player.player.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/**
 * Default Chromecast options. The receiver app ID below is the generic
 * Default Media Receiver — replace with your own published receiver if needed.
 */
class LumenCastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null

    companion object {
        // Google Cast Default Media Receiver
        const val DEFAULT_MEDIA_RECEIVER_APPLICATION_ID = "CC1AD845"
    }
}
