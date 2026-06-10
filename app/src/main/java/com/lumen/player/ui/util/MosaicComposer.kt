package com.lumen.player.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MosaicComposer {
    private const val TAG = "MosaicComposer"

    suspend fun compose(thumbnails: List<Bitmap>, outputSize: Int = 512): Bitmap = withContext(Dispatchers.Default) {
        val cellSize = (outputSize - 2) / 2
        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(android.graphics.Color.BLACK)

        val positions = listOf(
            Pair(0, 0), Pair(cellSize + 2, 0),
            Pair(0, cellSize + 2), Pair(cellSize + 2, cellSize + 2)
        )

        thumbnails.take(4).forEachIndexed { i, bmp ->
            try {
                val scaled = Bitmap.createScaledBitmap(bmp, cellSize, cellSize, true)
                canvas.drawBitmap(scaled, positions[i].first.toFloat(), positions[i].second.toFloat(), null)
                if (i > 0) scaled.recycle()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to composite thumbnail $i: ${e.message}")
            }
        }

        Log.d(TAG, "Composed mosaic from ${thumbnails.size} thumbnails (${outputSize}x$outputSize)")
        output
    }
}
