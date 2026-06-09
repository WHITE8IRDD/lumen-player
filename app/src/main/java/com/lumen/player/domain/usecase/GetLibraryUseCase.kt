package com.lumen.player.domain.usecase

import com.lumen.player.domain.model.MediaItem
import com.lumen.player.domain.model.MediaType
import com.lumen.player.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLibraryUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(type: MediaType): Flow<List<MediaItem>> =
        repository.observeByType(type)
}
