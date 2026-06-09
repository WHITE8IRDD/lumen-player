package com.lumen.player.domain.usecase

import com.lumen.player.domain.model.ScanProgress
import com.lumen.player.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanMediaUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(): Flow<ScanProgress> = repository.rescan()
}
