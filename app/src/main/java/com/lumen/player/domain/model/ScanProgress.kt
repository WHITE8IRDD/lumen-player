package com.lumen.player.domain.model

sealed class ScanProgress {
    data class Scanning(val count: Int, val total: Int) : ScanProgress()
    data class Done(val total: Int) : ScanProgress()
    data class Error(val message: String) : ScanProgress()
}
