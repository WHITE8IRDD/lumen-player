package com.lumen.player.core.util

import java.util.concurrent.TimeUnit

/** Formats millisecond durations into mm:ss or h:mm:ss. */
object TimeFormatter {
    fun format(ms: Long): String = msToClock(ms)

    fun msToClock(ms: Long): String {
        if (ms <= 0) return "0:00"
        val h = TimeUnit.MILLISECONDS.toHours(ms)
        val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%d:%02d".format(m, s)
    }
}
