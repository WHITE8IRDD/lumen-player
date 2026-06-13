package com.lumen.player.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumen.player.core.theme.LumenTextPrimary
import com.lumen.player.core.theme.LumenTextSecondary

@Composable
fun LumenEmptyState(
    blobColor: Color,
    blobMood: Mood,
    title: String,
    subtitle: String? = null,
    blobSize: Dp = 140.dp,
    accents: (@Composable BoxScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(blobSize * 1.8f),
            contentAlignment = Alignment.Center
        ) {
            LumenBlob(
                modifier = Modifier.size(blobSize),
                color = blobColor,
                mood = blobMood
            )
            accents?.invoke(this)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            color = LumenTextPrimary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = LumenTextSecondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        actions()
    }
}
