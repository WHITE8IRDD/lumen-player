package com.lumen.player.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession

/**
 * Default media-session callback. We accept connections from any controller
 * (Android Auto, system UI, etc.) and let Media3 forward player commands.
 *
 * Custom commands (e.g. equalizer toggle, sleep timer) can be wired here.
 */
@UnstableApi
class LumenSessionCallback : MediaSession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailablePlayerCommands(playerCommands)
            .setAvailableSessionCommands(sessionCommands)
            .build()
    }

    override fun onDisconnected(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ) {
        // No-op
    }
}
