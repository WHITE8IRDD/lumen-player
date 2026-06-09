package com.lumen.player.domain.usecase

import com.lumen.player.domain.model.Folder
import com.lumen.player.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoldersUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<Folder>> = repository.observeFolders()
}
