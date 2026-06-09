package com.lumen.player.ui.components

import androidx.compose.runtime.Composable

@Composable
fun PermissionDeniedDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    LumenConfirmationDialog(
        onDismiss = onDismiss,
        onConfirm = onOpenSettings,
        title = "Media access needed",
        body = "Lumen Player needs access to your audio and video files to show your library. Open system settings and grant the permissions to continue.",
        confirmLabel = "Open settings",
        variant = DialogVariant.NEUTRAL
    )
}
